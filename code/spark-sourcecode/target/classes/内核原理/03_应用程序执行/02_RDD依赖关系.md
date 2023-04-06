
[TOC]

以 WordCount 中的核心代码为例

```scala
   // 第一步
   val lines: RDD[String] = sc.textFile("datas")
   // 第二步
   val words: RDD[String] = lines.flatMap(_.split(" "))
   // 第三步
   val wordToOne = words.map(word=>(word,1))
   // 第四步
   val wordToSum: RDD[(String, Int)] = wordToOne.reduceByKey(_+_)
```

## 第一步

先进入 `val lines: RDD[String] = sc.textFile("datas")`

SparkContext.scala

```scala
def textFile(
      path: String,
      minPartitions: Int = defaultMinPartitions): RDD[String] = withScope {
    assertNotStopped()
    hadoopFile(path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text],
      minPartitions) // 创建了一个 HadoopRDD 对象
      .map(pair => pair._2.toString) // 这里
      .setName(path)
  }

def hadoopFile[K, V](
  path: String, inputFormatClass: Class[_ <: InputFormat[K, V]],
  keyClass: Class[K], valueClass: Class[V],
  minPartitions: Int = defaultMinPartitions): RDD[(K, V)] = withScope {
    new HadoopRDD(
      // 这里的 this 就是当前的SparkContext 
      this, confBroadcast, Some(setInputPathsFunc), inputFormatClass,
      keyClass, valueClass, minPartitions).setName(path)
}
```

RDD.scala

```scala
def map[U: ClassTag](f: T => U): RDD[U] = withScope {
  val cleanF = sc.clean(f)
  // 这个this就是前面创建 HadoopRDD 对象
  new MapPartitionsRDD[U, T](this, (_, _, iter) => iter.map(cleanF))
}
```

MapPartitionsRDD.scala

```scala
private[spark] class MapPartitionsRDD[U: ClassTag, T: ClassTag](
    var prev: RDD[T],
    f: (TaskContext, Int, Iterator[T]) => Iterator[U],  // (TaskContext, partition index, iterator)
    preservesPartitioning: Boolean = false,
    isFromBarrier: Boolean = false,
    isOrderSensitive: Boolean = false)
  // 这个 MapPartitionsRDD 继承至 RDD[U](prev)
  // prev 就是 HadoopRDD
  extends RDD[U](prev){}
```
RDD.scala

```scala
abstract class RDD[T: ClassTag](
    @transient private var _sc: SparkContext, 
    @transient private var deps: Seq[Dependency[_]]) extends Serializable with Logging {
  /** Construct an RDD with just a one-to-one dependency on one parent */
  // oneParent 就是 HadoopRDD
  def this(@transient oneParent: RDD[_]) =
    this(oneParent.context, List(new OneToOneDependency(oneParent)))
}
```

所以:

- 先通过 hadoopFile 方法创建了一个 HadoopRDD 对象，
- 然后这个 HadoopRDD 对象再调用 map 方法，创建了一个 MapPartitionsRDD 对象 
  - 在 map 方法中，基于这个 HadoopRDD 对象，通过 new 创建了一个 MapPartitionsRDD 对象
    - 而 MapPartitionsRDD 类继承了 RDD 类，使用了这个 RDD 类中的构造方法创建对象
    - 这个 RDD 类的构造函数中，将 HadoopRDD 对象作为参数传进去，
    - 基于这个 HadoopRDD 对象创建了一个 OneToOneDependency 依赖，
    - 并基于这个 OneToOneDependency 依赖，创建了 MapPartitionsRDD 对象

整体流程：

**HadoopRDD <---- new OneToOneDependency(HadoopRDD) <---- MapPartitionsRDD**

## 第二步

再进入`val words: RDD[String] = lines.flatMap(_.split(" "))`

```scala
def flatMap[U: ClassTag](f: T => TraversableOnce[U]): RDD[U] = withScope {
    val cleanF = sc.clean(f)
    // 这里的this就是 前面textfile创建的MapPartitionsRDD
    new MapPartitionsRDD[U, T](this, (_, _, iter) => iter.flatMap(cleanF))
  }
```

MapPartitionsRDD.scala

```scala
private[spark] class MapPartitionsRDD[U: ClassTag, T: ClassTag](
     var prev: RDD[T],
     f: (TaskContext, Int, Iterator[T]) => Iterator[U],  // (TaskContext, partition index, iterator)
     preservesPartitioning: Boolean = false,
     isFromBarrier: Boolean = false,isOrderSensitive: Boolean = false)
// 这个 MapPartitionsRDD 继承至 RDD[U](prev)
// prev 就是 前面textfile创建的MapPartitionsRDD
  extends RDD[U](prev){}
```

RDD.scala

```scala
abstract class RDD[T: ClassTag](
    @transient private var _sc: SparkContext, 
    @transient private var deps: Seq[Dependency[_]]) extends Serializable with Logging {
  /** Construct an RDD with just a one-to-one dependency on one parent */
  // oneParent 就是 前面textfile创建的MapPartitionsRDD
  def this(@transient oneParent: RDD[_]) =
    this(oneParent.context, List(new OneToOneDependency(oneParent)))
}
```

和第一步同样道理

第一步: **HadoopRDD <---- new OneToOneDependency(HadoopRDD) <---- MapPartitionsRDD**

第二步: **MapPartitionsRDD <---- new OneToOneDependency(HadoopRDD) <---- MapPartitionsRDD**

## 第三步

进入 `val wordToOne = words.map(word=>(word,1))`

RDD.scala

```scala
def map[U: ClassTag](f: T => U): RDD[U] = withScope {
  val cleanF = sc.clean(f)
  // 这个this就是前面创建 HadoopRDD 对象
  new MapPartitionsRDD[U, T](this, (_, _, iter) => iter.map(cleanF))
}
```

MapPartitionsRDD.scala

```scala
private[spark] class MapPartitionsRDD[U: ClassTag, T: ClassTag](
     var prev: RDD[T],
     f: (TaskContext, Int, Iterator[T]) => Iterator[U],  // (TaskContext, partition index, iterator)
     preservesPartitioning: Boolean = false,
     isFromBarrier: Boolean = false,isOrderSensitive: Boolean = false)
// 这个 MapPartitionsRDD 继承至 RDD[U](prev)
// prev 就是 前面textfile创建的MapPartitionsRDD
  extends RDD[U](prev){}
```

RDD.scala

```scala
abstract class RDD[T: ClassTag](
    @transient private var _sc: SparkContext, 
    @transient private var deps: Seq[Dependency[_]]) extends Serializable with Logging {
  /** Construct an RDD with just a one-to-one dependency on one parent */
  // oneParent 就是 前面textfile创建的MapPartitionsRDD
  def this(@transient oneParent: RDD[_]) =
    this(oneParent.context, List(new OneToOneDependency(oneParent)))
}
```

和第一步同样道理

第一步: **HadoopRDD <---- new OneToOneDependency(HadoopRDD) <---- MapPartitionsRDD**

第二步: **MapPartitionsRDD <---- new OneToOneDependency(HadoopRDD) <---- MapPartitionsRDD**

第三步: **MapPartitionsRDD <---- new OneToOneDependency(HadoopRDD) <---- MapPartitionsRDD**


## 第四步

进入 `val wordToSum: RDD[(String, Int)] = wordToOne.reduceByKey(_+_)`

PairRDDFunctions.scala

```scala
def reduceByKey(partitioner: Partitioner, func: (V, V) => V): RDD[(K, V)] = self.withScope {
    combineByKeyWithClassTag[V]((v: V) => v, func, func, partitioner)
  }
```

PairRDDFunctions.scala

```scala
def combineByKeyWithClassTag[C](
      createCombiner: V => C,
      mergeValue: (C, V) => C,
      mergeCombiners: (C, C) => C,
      partitioner: Partitioner,
      mapSideCombine: Boolean = true,
      serializer: Serializer = null)(implicit ct: ClassTag[C]): RDD[(K, C)] = self.withScope {
      //....
      // self 是第三步创建的 MapPartitionsRDD
      new ShuffledRDD[K, V, C](self, partitioner)
        .setSerializer(serializer)
        .setAggregator(aggregator)
        .setMapSideCombine(mapSideCombine)
      }
```

ShuffledRDD.scala

```scala
class ShuffledRDD[K: ClassTag, V: ClassTag, C: ClassTag](
    @transient var prev: RDD[_ <: Product2[K, V]],  // 第三步创建的 MapPartitionsRDD
    part: Partitioner)
  // 这里第二个依赖的参数是Nil, 所以在内部应该是使用的默认值
  extends RDD[(K, C)](prev.context, Nil) {

      override def getDependencies: Seq[Dependency[_]] = {
        val serializer = userSpecifiedSerializer.getOrElse {
          val serializerManager = SparkEnv.get.serializerManager
          if (mapSideCombine) {
            serializerManager.getSerializer(implicitly[ClassTag[K]], implicitly[ClassTag[C]])
          } else {
            serializerManager.getSerializer(implicitly[ClassTag[K]], implicitly[ClassTag[V]])
          }
        }
        // prev是第三步创建的 MapPartitionsRDD
        List(new ShuffleDependency(prev, part, serializer, keyOrdering, aggregator, mapSideCombine))
      }
  }
```

RDD.scala

```scala
abstract class RDD[T: ClassTag](
    @transient private var _sc: SparkContext,
    @transient private var deps: Seq[Dependency[_]]
  ) extends Serializable with Logging {}
```

第一步: **HadoopRDD <---- new OneToOneDependency(HadoopRDD) <---- MapPartitionsRDD**

第二步: **MapPartitionsRDD <---- new OneToOneDependency(HadoopRDD) <---- MapPartitionsRDD**

第三步: **MapPartitionsRDD <---- new OneToOneDependency(HadoopRDD) <---- MapPartitionsRDD**

第四步: **MapPartitionsRDD <---- new ShuffleDependency(HadoopRDD) <---- ShuffledRDD**


## Dependency类

Dependency.scala
 
                 -->   NarrowDependency   -->  OneToOneDependency 、 RangeDependency
    Dependency  
                 -->  ShuffleDependency

```scala
/**
 * :: DeveloperApi ::
 * Base class for dependencies.
 * 依赖的基类
 */
abstract class Dependency[T] extends Serializable {
  def rdd: RDD[T]
}

/**
 * :: DeveloperApi ::
 * Base class for dependencies where each partition of the child RDD depends on a small number
 * of partitions of the parent RDD. Narrow dependencies allow for pipelined execution.
 * 子 RDD 的每个分区依赖于父 RDD 的少量分区
 * 窄依赖允许管道化执行
 */
abstract class NarrowDependency[T](_rdd: RDD[T])  // _rdd: 父RDD
  extends Dependency[T] {}

/**
 * :: DeveloperApi ::
 * Represents a dependency on the output of a shuffle stage. Note that in the case of shuffle,
 * the RDD is transient since we don't need it on the executor side.
 * 表示对 shuffle 阶段的输出的依赖。
 * 注意，在 shuffle 的情况下，RDD 是 transient(不用传输，不用序列化), 因为我们在 executor 端不需要它。
 * @param _rdd the parent RDD   父RDD
 * @param partitioner partitioner used to partition the shuffle output
 * @param serializer [[org.apache.spark.serializer.Serializer Serializer]] to use. If not set
 *                   explicitly then the default serializer, as specified by `spark.serializer`
 *                   config option, will be used.
 * @param keyOrdering key ordering for RDD's shuffles
 * @param aggregator map/reduce-side aggregator for RDD's shuffle
 * @param mapSideCombine whether to perform partial aggregation (also known as map-side combine)
 * @param shuffleWriterProcessor the processor to control the write behavior in ShuffleMapTask
 */
class ShuffleDependency[K: ClassTag, V: ClassTag, C: ClassTag](
      @transient private val _rdd: RDD[_ <: Product2[K, V]], val partitioner: Partitioner,
      val serializer: Serializer = SparkEnv.get.serializer, val keyOrdering: Option[Ordering[K]] = None,
      val aggregator: Option[Aggregator[K, V, C]] = None,val mapSideCombine: Boolean = false,
      val shuffleWriterProcessor: ShuffleWriteProcessor = new ShuffleWriteProcessor)
  extends Dependency[Product2[K, V]] with Logging {}

/**
 * :: DeveloperApi ::
 * Represents a one-to-one dependency between partitions of the parent and child RDDs.
 * 子 RDD 的一个分区依赖于父 RDD 的一个分区
 */
class OneToOneDependency[T](rdd: RDD[T])
  extends NarrowDependency[T](rdd) {}

/**
 * :: DeveloperApi ::
 * Represents a one-to-one dependency between ranges of partitions in the parent and child RDDs.
 * 子 RDD 的一个分区依赖于父 RDD 的一定范围内的分区
 * 
 * @param rdd the parent RDD
 * @param inStart the start of the range in the parent RDD
 * @param outStart the start of the range in the child RDD
 * @param length the length of the range
 */
class RangeDependency[T](rdd: RDD[T], inStart: Int, outStart: Int, length: Int)
  extends NarrowDependency[T](rdd){}
```