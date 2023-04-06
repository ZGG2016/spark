
RDD.scala

```scala
  /**
   * Mark this RDD for persisting using the specified level.
   * 使用指定的存储级别，标记这个 RDD 持久化
   * 
   * @param newLevel the target storage level
   * @param allowOverride whether to override any existing level with the new one
   *                      当已设置了一个存储级别，判断是否允许使用新的一个存储级别覆盖旧的
   */
  private def persist(newLevel: StorageLevel, allowOverride: Boolean): this.type = {
    // TODO: Handle changes of StorageLevel
    // 当前存储级别不是NONE，且不等于newLevel，且不允许覆盖时，抛出异常。
    if (storageLevel != StorageLevel.NONE && newLevel != storageLevel && !allowOverride) {
      throw SparkCoreErrors.cannotChangeStorageLevelError()
    }
    
    // If this is the first time this RDD is marked for persisting, register it
    // with the SparkContext for cleanups and accounting. Do this only once.
    // 如果这个RDD是第一次被标记，就注册它。这个操作仅做一次
    if (storageLevel == StorageLevel.NONE) {
      // registerRDDForCleanup：注册一个RDD以便在垃圾收集时进行清理
      sc.cleaner.foreach(_.registerRDDForCleanup(this))
      // 注册RDD
      sc.persistRDD(this)
    }
    storageLevel = newLevel
    this
  }

  /**
   * Set this RDD's storage level to persist its values across operations after the first time
   * it is computed. This can only be used to assign a new storage level if the RDD does not
   * have a storage level set yet. Local checkpointing is an exception.
   */
  def persist(newLevel: StorageLevel): this.type = {
    // 判断这个RDD被标记为 是否进行本地checkpoint
    if (isLocallyCheckpointed) {
      // This means the user previously called localCheckpoint(), which should have already
      // marked this RDD for persisting. Here we should override the old storage level with
      // one that is explicitly requested by the user (after adapting it to use disk).
      // 在调用localCheckpoint方法时，已经标记了这个RDD进行持久化。这里只需覆盖旧的存储级别
      // transformStorageLevel：把指定的一个存储级别转换成 使用磁盘的一种存储级别。
      persist(LocalRDDCheckpointData.transformStorageLevel(newLevel), allowOverride = true)
    } else {
      persist(newLevel, allowOverride = false)
    }
  }

  /**
   * Persist this RDD with the default storage level (`MEMORY_ONLY`).
   */
  def persist(): this.type = persist(StorageLevel.MEMORY_ONLY)

    /**
     * Mark the RDD as non-persistent, and remove all blocks for it from memory and disk.
     * 标记此RDD为非持久化，并移除内存和磁盘上的所有blocks
     * 
     * @param blocking Whether to block until all blocks are deleted (default: false)
     *                  block没被删完，是否阻塞
     * @return This RDD.
     */
    def unpersist(blocking: Boolean = false): this.type = {
      logInfo(s"Removing RDD $id from persistence list")
      sc.unpersistRDD(id, blocking)
      storageLevel = StorageLevel.NONE
      this
    }
```

SparkContext.scala

```scala
  /**
   * Register an RDD to be persisted in memory and/or disk storage
   * 注册一个 rdd，它将被持久化在内存或磁盘存储中 
   */
  private[spark] def persistRDD(rdd: RDD[_]): Unit = {
    // 将这个 rdd 放进 persistentRdds
    persistentRdds(rdd.id) = rdd
  }

    // Keeps track of all persisted RDDs
    // 追踪所有持久化的 RDDs
    private[spark] val persistentRdds = {
      val map: ConcurrentMap[Int, RDD[_]] = new MapMaker().weakValues().makeMap[Int, RDD[_]]()
      map.asScala
    }

    /**
     * Unpersist an RDD from memory and/or disk storage
     */
    private[spark] def unpersistRDD(rddId: Int, blocking: Boolean): Unit = {
      // 删除属于给定RDD的所有块
      env.blockManager.master.removeRdd(rddId, blocking)
      // 从 persistentRdds 移出
      persistentRdds.remove(rddId)
      listenerBus.post(SparkListenerUnpersistRDD(rddId))
    }
```