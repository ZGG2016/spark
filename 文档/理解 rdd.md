# 理解 rdd

[https://www.bilibili.com/video/BV11A411L7CK?p=26](https://www.bilibili.com/video/BV11A411L7CK?p=26)

[https://www.bilibili.com/video/BV11A411L7CK?p=27](https://www.bilibili.com/video/BV11A411L7CK?p=27)

[https://www.bilibili.com/video/BV11A411L7CK?p=28](https://www.bilibili.com/video/BV11A411L7CK?p=28)

[https://www.bilibili.com/video/BV11A411L7CK?p=29](https://www.bilibili.com/video/BV11A411L7CK?p=29)

-------------------------------------------------------------

![理解IO-01](./image/理解IO-01.png)

-------------------------------------------------------------

![理解IO-02](./image/理解IO-02.png)

-------------------------------------------------------------

![理解IO-03](./image/理解IO-03.png)

-------------------------------------------------------------

![理解rdd-01](./image/理解rdd-01.png)

-------------------------------------------------------------

![理解rdd-02](./image/理解rdd-02.png)


-------------------------------------------------------------

![理解rdd-03](./image/理解rdd-03.png)

-------------------------------------------------------------

RDD（Resilient Distributed Dataset）叫做弹性分布式数据集，是 Spark 中最基本的数据处理模型。

代码中是一个抽象类，它代表一个弹性的、不可变、可分区、里面的元素可并行计算的集合。

特性:

- 弹性

	- 存储的弹性：内存与磁盘的自动切换

	- 容错的弹性：数据丢失可以自动恢复

	- 计算的弹性：计算出错重试机制

	- 分片的弹性：可根据需要重新分片

- 分布式：数据存储在大数据集群不同节点上

- 数据集：RDD 封装了计算逻辑，并不保存数据

- 数据抽象：RDD 是一个抽象类，需要子类具体实现

- 不可变：RDD 封装了计算逻辑，是不可以改变的，想要改变，只能产生新的 RDD，在新的 RDD 里面封装计算逻辑

- 可分区、并行计算


核心属性:

- 分区列表

	```scala
	/**
	 * RDD 数据结构中存在分区列表，用于执行任务时并行计算，是实现分布式计算的重要属性
	 * Implemented by subclasses to return the set of partitions in this RDD. This method will only be called once, so it is safe to implement a time-consuming computation in it.
	 * The partitions in this array must satisfy the following property:
	 * `rdd.partitions.zipWithIndex.forall { case (partition, index) => partition.index == index }`
   	 */
  	protected def getPartitions: Array[Partition]
	```

- 分区计算函数

	```scala
	/**
	 * Spark 在计算时，是使用分区函数对每一个分区进行计算
	 * :: DeveloperApi ::
	 * Implemented by subclasses to compute a given partition.
	 */
	  @DeveloperApi
	  def compute(split: Partition, context: TaskContext): Iterator[T]
	```

- RDD 之间的依赖关系

	```scala
	/**
	 * RDD 是计算模型的封装，当需求中需要将多个计算模型进行组合时，就需要将多个 RDD 建立依赖关系
	 * Implemented by subclasses to return how this RDD depends on parent RDDs. This method will only
	 * be called once, so it is safe to implement a time-consuming computation in it.
	 */
	  protected def getDependencies: Seq[Dependency[_]] = deps
	```

- 分区器（可选）

	```scala
 	/** 
 	 * 当数据为 KV 类型数据时，可以通过设定分区器自定义数据的分区
 	 * Optionally overridden by subclasses to specify how they are partitioned.  
 	 * */
  	@transient val partitioner: Option[Partitioner] = None
	```

- 首选位置（可选）

	```scala
  	/**
  	 * 计算数据时，可以根据计算节点的状态选择不同的节点位置进行计算
  	 * Optionally overridden by subclasses to specify placement preferences.
   	 */
  	protected def getPreferredLocations(split: Partition): Seq[String] = Nil
	```

	