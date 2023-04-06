# MemoryConsumer

```java
package org.apache.spark.memory;

import java.io.IOException;

import org.apache.spark.unsafe.array.LongArray;
import org.apache.spark.unsafe.memory.MemoryBlock;

/**
 * 支持溢写的 TaskMemoryManager 的内存消费者。
 *
 * A memory consumer of {@link TaskMemoryManager} that supports spilling.
 *
 * 注意：这仅支持Tungsten内存的分配和溢写
 * Note: this only supports allocation / spilling of Tungsten memory.
 */
public abstract class MemoryConsumer {

  protected final TaskMemoryManager taskMemoryManager;
  private final long pageSize;
  private final MemoryMode mode;
  protected long used;

  protected MemoryConsumer(TaskMemoryManager taskMemoryManager, long pageSize, MemoryMode mode) {
    this.taskMemoryManager = taskMemoryManager;
    this.pageSize = pageSize;
    this.mode = mode;
  }

  protected MemoryConsumer(TaskMemoryManager taskMemoryManager) {
    this(taskMemoryManager, taskMemoryManager.pageSizeBytes(), MemoryMode.ON_HEAP);
  }

  /**
   * Returns the memory mode, {@link MemoryMode#ON_HEAP} or {@link MemoryMode#OFF_HEAP}.
   */
  public MemoryMode getMode() {
    return mode;
  }

  /**
   * Returns the size of used memory in bytes. 返回已使用的内存大小。
   */
  public long getUsed() {
    return used;
  }

  /**
   * Force spill during building.
   */
  public void spill() throws IOException {
    spill(Long.MAX_VALUE, this);
  }

  /**
   * Spill some data to disk to release memory, which will be called by TaskMemoryManager
   * when there is not enough memory for the task.
   *
   * This should be implemented by subclass.
   *
   * Note: In order to avoid possible deadlock, should not call acquireMemory() from spill().
   *
   * Note: today, this only frees Tungsten-managed pages.
   *
   * @param size the amount of memory should be released
   * @param trigger the MemoryConsumer that trigger this spilling
   * @return the amount of released memory in bytes
   */
  public abstract long spill(long size, MemoryConsumer trigger) throws IOException;

  /**
   * 分配一个 `size` 大小的LongArray。
   * 注意：如果spark为这次分配没有足够的内存，这个方法可能会抛出`SparkOutOfMemoryError`，
   *   或者，如果这个LongArray太大，以至于无法放入一个小的page中，就会抛出`TooLargePageException`
   * 调用者应该小心这两个异常，或者确保这个`size`足够的小，以至于不会触发异常。
   *
   * Allocates a LongArray of `size`. Note that this method may throw `SparkOutOfMemoryError`
   * if Spark doesn't have enough memory for this allocation, or throw `TooLargePageException`
   * if this `LongArray` is too large to fit in a single page. The caller side should take care of these two exceptions, or make sure the `size` is small enough that won't trigger exceptions.
   *
   * @throws SparkOutOfMemoryError
   * @throws TooLargePageException
   */
  public LongArray allocateArray(long size) {
    long required = size * 8L;
    //分配一个内存块，它将在 MemoryManager 的 page 中被追踪。
    MemoryBlock page = taskMemoryManager.allocatePage(required, this);
    if (page == null || page.size() < required) {
      throwOom(page, required);
    }
    used += required;
    return new LongArray(page);
  }

  /**
   * Frees a LongArray.  释放一个LongArray
   */
  public void freeArray(LongArray array) {
    freePage(array.memoryBlock());
  }

  /**
   * Allocate a memory block with at least `required` bytes.
   *
   * @throws SparkOutOfMemoryError
   */
  protected MemoryBlock allocatePage(long required) {
    MemoryBlock page = taskMemoryManager.allocatePage(Math.max(pageSize, required), this);
    if (page == null || page.size() < required) {
      throwOom(page, required);
    }
    used += page.size();
    return page;
  }

  /**
   * Free a memory block. 释放一个内存块
   */
  protected void freePage(MemoryBlock page) {
    //已用的内存减去要释放的
    used -= page.size();
    //释放TaskMemoryManager#allocatePage分配的内存块。
    taskMemoryManager.freePage(page, this);
  }

  /**
   * Allocates memory of `size`.
   */
  public long acquireMemory(long size) {
    long granted = taskMemoryManager.acquireExecutionMemory(size, this);
    used += granted;
    return granted;
  }

  /**
   * Release N bytes of memory.
   */
  public void freeMemory(long size) {
    taskMemoryManager.releaseExecutionMemory(size, this);
    used -= size;
  }

  private void throwOom(final MemoryBlock page, final long required) {
    long got = 0;
    if (page != null) {
      got = page.size();
      taskMemoryManager.freePage(page, this);
    }
    taskMemoryManager.showMemoryUsage();
    // checkstyle.off: RegexpSinglelineJava
    throw new SparkOutOfMemoryError("Unable to acquire " + required + " bytes of memory, got " +
      got);
    // checkstyle.on: RegexpSinglelineJava
  }
}

```