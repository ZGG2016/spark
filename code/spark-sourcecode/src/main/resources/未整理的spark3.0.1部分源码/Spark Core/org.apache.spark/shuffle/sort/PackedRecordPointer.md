# PackedRecordPointer

```java
package org.apache.spark.shuffle.sort;

/**
 * 封装了一个8字节的字，包含了一个24bit的分区数字和40bit的记录指针。
 *
 * Wrapper around an 8-byte word that holds a 24-bit partition number and 40-bit record pointer.
 *
 * 在long类型内，数据的排列如下:
 *   [24 bit partition number][13 bit memory page number][27 bit offset in page]
 *
 * <p>
 * Within the long, the data is laid out as follows:
 * <pre>
 *   [24 bit partition number][13 bit memory page number][27 bit offset in page]
 * </pre>
 *
 * 这表明最大可寻址的页面大小是 2^27 bits = 128 megabytes，假设页面偏移量不是8字节字对齐的。
 *
 * 由于我们有2^13页，这表明，对每个任务，我们可以寻址RAM的2^13 * 128 megabytes = 1 terabyte
 * 2^13页是基于org.apache.spark.memory.TaskMemoryManager分配的13bit的页面数字
 *
 * This implies that the maximum addressable page size is 2^27 bits = 128 megabytes, assuming that
 * our offsets in pages are not 8-byte-word-aligned. Since we have 2^13 pages (based off the
 * 13-bit page numbers assigned by {@link org.apache.spark.memory.TaskMemoryManager}), this
 * implies that we can address 2^13 * 128 megabytes = 1 terabyte of RAM per task.
 * <p>
 *
 * 假设词对齐允许最大1gb的页面大小，但是我们将这个优化留给以后的工作，因为它将需要更仔细的设计来确保地址正确对齐
 *
 * Assuming word-alignment would allow for a 1 gigabyte maximum page size, but we leave this
 * optimization to future work as it will require more careful design to ensure that addresses are
 * properly aligned (e.g. by padding records).
 */
final class PackedRecordPointer {

  static final int MAXIMUM_PAGE_SIZE_BYTES = 1 << 27;  // 128 megabytes

  /**
   * The maximum partition identifier that can be encoded. Note that partition ids start from 0.
   */
  //可以被编码的最大分区标识符。分区id从0开始。
  static final int MAXIMUM_PARTITION_ID = (1 << 24) - 1;  // 16777215

  /**
   * The index of the first byte of the partition id, counting from the least significant byte.
   */
  //分区id的第一个字节的索引，从最低有效字节开始计算。
  static final int PARTITION_ID_START_BYTE_INDEX = 5;

  /**
   * The index of the last byte of the partition id, counting from the least significant byte.
   */
  //分区id的最后一个字节的索引，从最低有效字节开始计算。
  static final int PARTITION_ID_END_BYTE_INDEX = 7;

  /** Bit mask for the lower 40 bits of a long. */
  private static final long MASK_LONG_LOWER_40_BITS = (1L << 40) - 1;

  /** Bit mask for the upper 24 bits of a long */
  private static final long MASK_LONG_UPPER_24_BITS = ~MASK_LONG_LOWER_40_BITS;

  /** Bit mask for the lower 27 bits of a long. */
  private static final long MASK_LONG_LOWER_27_BITS = (1L << 27) - 1;

  /** Bit mask for the lower 51 bits of a long. */
  private static final long MASK_LONG_LOWER_51_BITS = (1L << 51) - 1;

  /** Bit mask for the upper 13 bits of a long */
  private static final long MASK_LONG_UPPER_13_BITS = ~MASK_LONG_LOWER_51_BITS;

  /**
   * 打包一个记录地址和分区id成一个单个字。
   * Pack a record address and partition id into a single word.
   *
   * @param recordPointer a record pointer encoded by TaskMemoryManager.
   *                        TaskMemoryManager编码的记录指针。
   * @param partitionId a shuffle partition id (maximum value of 2^24).
   *                       一个shuffle分区id
   * @return a packed pointer that can be decoded using the {@link PackedRecordPointer} class. 返回一个可用使用PackedRecordPointer类解码的packed pointer
   */
  public static long packPointer(long recordPointer, int partitionId) {
    assert (partitionId <= MAXIMUM_PARTITION_ID);
    // Note that without word alignment we can address 2^27 bytes = 128 megabytes per page.
    // Also note that this relies on some internals of how TaskMemoryManager encodes its addresses.
    // 注意：没有字对齐，我们可以每页寻址2^27字节= 128mb。
    // 这依赖于TaskMemoryManager如何编码其地址的一些内部机制。

    //页数
    final long pageNumber = (recordPointer & MASK_LONG_UPPER_13_BITS) >>> 24; 
    //压缩的地址
    final long compressedAddress = pageNumber | (recordPointer & MASK_LONG_LOWER_27_BITS);
    return (((long) partitionId) << 40) | compressedAddress;
  }

  private long packedRecordPointer;

  public void set(long packedRecordPointer) {
    this.packedRecordPointer = packedRecordPointer;
  }

  public int getPartitionId() {
    return (int) ((packedRecordPointer & MASK_LONG_UPPER_24_BITS) >>> 40);
  }

  public long getRecordPointer() {
    final long pageNumber = (packedRecordPointer << 24) & MASK_LONG_UPPER_13_BITS;
    final long offsetInPage = packedRecordPointer & MASK_LONG_LOWER_27_BITS;
    return pageNumber | offsetInPage;
  }

}

```