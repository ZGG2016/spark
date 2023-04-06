# ShuffleInMemorySorter

```java
package org.apache.spark.shuffle.sort;

import java.util.Comparator;

import org.apache.spark.memory.MemoryConsumer;
import org.apache.spark.unsafe.Platform;
import org.apache.spark.unsafe.array.LongArray;
import org.apache.spark.unsafe.memory.MemoryBlock;
import org.apache.spark.util.collection.Sorter;
import org.apache.spark.util.collection.unsafe.sort.RadixSort;

final class ShuffleInMemorySorter {

  //排序分区ids
  private static final class SortComparator implements Comparator<PackedRecordPointer> {
    @Override
    public int compare(PackedRecordPointer left, PackedRecordPointer right) {
      int leftId = left.getPartitionId();
      int rightId = right.getPartitionId();
      return leftId < rightId ? -1 : (leftId > rightId ? 1 : 0);
    }
  }
  private static final SortComparator SORT_COMPARATOR = new SortComparator();

  private final MemoryConsumer consumer;

  /**
   * PackedRecordPointer 编码的记录指针和分区ids的数组。
   * 先排序这个数组，而不是直接操作记录。
   *
   *
   * An array of record pointers and partition ids that have been encoded by
   * {@link PackedRecordPointer}. The sort operates on this array instead of directly manipulating
   * records.
   *
   * 数组的一部分存储指针，剩余部分用于排序的临时缓存保留。
   *
   * Only part of the array will be used to store the pointers, the rest part is preserved as
   * temporary buffer for sorting.
   */
  private LongArray array;

  /**
   * 是否使用基数排序，来对内存中的分区ids排序。
   *
   * 基数排序更快，但当添加指针时，需要保留额外的内存。
   *
   * Whether to use radix sort for sorting in-memory partition ids. Radix sort is much faster
   * but requires additional memory to be reserved memory as pointers are added.
   */
  private final boolean useRadixSort;

  /**
   * 新纪录插入的指针数组中的位置
   *
   * The position in the pointer array where new records can be inserted.
   */
  private int pos = 0;

  /**
   * 多少记录可以被插入，因为数组的一部分需要留给排序。
   * How many records could be inserted, because part of the array should be left for sorting.
   */
  private int usableCapacity = 0;

  private final int initialSize;

  ShuffleInMemorySorter(MemoryConsumer consumer, int initialSize, boolean useRadixSort) {
    this.consumer = consumer;
    assert (initialSize > 0);
    this.initialSize = initialSize;
    this.useRadixSort = useRadixSort;
    //分配一个 `size` 大小的LongArray。
    this.array = consumer.allocateArray(initialSize);
    this.usableCapacity = getUsableCapacity();
  }

  //可用容量
  private int getUsableCapacity() {
    // Radix sort requires same amount of used memory as buffer, Tim sort requires
    // half of the used memory as buffer.
    // 基数排序需要相同数量的内存作为缓冲区，Tim排序需要一半的内存作为缓冲区。
    return (int) (array.size() / (useRadixSort ? 2 : 1.5));
  }

  public void free() {
    if (array != null) {
      consumer.freeArray(array); //释放一个LongArray
      array = null;
    }
  }

  public int numRecords() {
    return pos;
  }

  //重置array
  public void reset() {
    // Reset `pos` here so that `spill` triggered by the below `allocateArray` will be no-op.
    //重置pos新纪录插入的指针数组中的位置 ，为了下面的`allocateArray`触发的溢写是 no-op
    pos = 0;
    if (consumer != null) {
      consumer.freeArray(array);
      // As `array` has been released, we should set it to  `null` to avoid accessing it before `allocateArray` returns. `usableCapacity` is also set to `0` to avoid any codes writing data to `ShuffleInMemorySorter` when `array` is `null` (e.g., in ShuffleExternalSorter.growPointerArrayIfNecessary, we may try to access  `ShuffleInMemorySorter` when `allocateArray` throws SparkOutOfMemoryError).
      //当`array`被释放，我们应该设置它为null，以避免在`allocateArray`返回前访问它。
//`usableCapacity`设为0，以避免，当`array`为null时，任意的代码将数据写入`ShuffleInMemorySorter`
//(如，在ShuffleExternalSorter.growPointerArrayIfNecessary中，
//当`allocateArray`抛出SparkOutOfMemoryError时，我们可能尝试访问`ShuffleInMemorySorter`)
      array = null;
      usableCapacity = 0;
      array = consumer.allocateArray(initialSize); //分配一个初始容量
      usableCapacity = getUsableCapacity();
    }
  }

  //扩展原array
  public void expandPointerArray(LongArray newArray) {
    assert(newArray.size() > array.size());
    //copyMemory：由src往dst复制length长的一段
    //由array往newArray复制 pos * 8L 长
    Platform.copyMemory(
      array.getBaseObject(),
      array.getBaseOffset(),
      newArray.getBaseObject(),
      newArray.getBaseOffset(),
      pos * 8L
    );
    //把原array释放掉
    consumer.freeArray(array); 
    array = newArray;
    usableCapacity = getUsableCapacity();
  }

  //判断是否还有额外的空间来放新纪录
  public boolean hasSpaceForAnotherRecord() {
    return pos < usableCapacity;
  }

  //返回使用的内存
  public long getMemoryUsage() {
    return array.size() * 8;
  }

  /**
   * 插入一条将被排序的记录
   * Inserts a record to be sorted.
   *
   * @param recordPointer a pointer to the record, encoded by the task memory manager. Due to  certain pointer compression techniques used by the sorter, the sort can only operate on pointers that point to locations in the first {@link PackedRecordPointer#MAXIMUM_PAGE_SIZE_BYTES} bytes of a data page.
   *  一个指向记录的指针，由task memory manager编码。
   *  由于sorter使用的特定的指针压缩技术，
   *  排序仅在指向数据页的第一个MAXIMUM_PAGE_SIZE_BYTES字节中的位置的指针操作。
   *
   * @param partitionId the partition id, which must be less than or equal to {@link PackedRecordPointer#MAXIMUM_PARTITION_ID}. 分区id，必须小于等于MAXIMUM_PARTITION_ID
   */
  public void insertRecord(long recordPointer, int partitionId) {
    //是否还有额外的空间来放新纪录，没有的话，抛异常。
    if (!hasSpaceForAnotherRecord()) {
      throw new IllegalStateException("There is no space for new record");
    }
    //有的话，在array的pos位置，放一个packed pointer
    array.set(pos, PackedRecordPointer.packPointer(recordPointer, partitionId));
    pos++; //移动位置
  }

  /**
   * 使用的像迭代器的一个类，而没有使用java迭代器。
   *
   * An iterator-like class that's used instead of Java's Iterator in order to facilitate inlining.
   */
  public static final class ShuffleSorterIterator {

    //PackedRecordPointer 编码的记录指针和分区ids的数组。
    private final LongArray pointerArray;
    private final int limit;
    //封装了一个8字节的字，包含了一个24bit的分区数字和40bit的记录指针。
    final PackedRecordPointer packedRecordPointer = new PackedRecordPointer();
    private int position = 0;

    ShuffleSorterIterator(int numRecords, LongArray pointerArray, int startingPosition) {
      this.limit = numRecords + startingPosition; //起始位置加上记录数
      this.pointerArray = pointerArray;
      this.position = startingPosition;
    }

    public boolean hasNext() {
      return position < limit; //小于极限值
    }

    public void loadNext() {
      packedRecordPointer.set(pointerArray.get(position));
      position++;  //位置+1
    }
  }

  /**
   * 按顺序，返回记录指针上的迭代器
   *
   * Return an iterator over record pointers in sorted order.
   */
  public ShuffleSorterIterator getSortedIterator() {
    int offset = 0;
    //useRadixSort：是否使用基数排序，来对内存中的分区ids排序。
    if (useRadixSort) { 
      //直接使用基数排序
      offset = RadixSort.sort(
        array, pos,
        PackedRecordPointer.PARTITION_ID_START_BYTE_INDEX,
        PackedRecordPointer.PARTITION_ID_END_BYTE_INDEX, false, false);
    } else {
      //MemoryBlock：连续的内存块
      MemoryBlock unused = new MemoryBlock(
        array.getBaseObject(),
        array.getBaseOffset() + pos * 8L,
        (array.size() - pos) * 8L);
      LongArray buffer = new LongArray(unused);
      Sorter<PackedRecordPointer, LongArray> sorter =
        new Sorter<>(new ShuffleSortDataFormat(buffer));
//Sorter：对java TimSort 的简单封装
//SortDataFormat：排序任意的数据输入缓存的一个抽象。
      sorter.sort(array, 0, pos, SORT_COMPARATOR); 
    }
    return new ShuffleSorterIterator(pos, array, offset);
  }
}

```