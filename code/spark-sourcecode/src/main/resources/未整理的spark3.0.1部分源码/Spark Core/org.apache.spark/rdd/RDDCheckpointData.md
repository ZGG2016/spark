# RDDCheckpointData

```java
package org.apache.spark.rdd

import scala.reflect.ClassTag

import org.apache.spark.Partition

/**
 * 枚举，通过 checkpoint 管理RDD的状态转换
 * Enumeration to manage state transitions of an RDD through checkpointing
 *
 * [ Initialized --{@literal >} checkpointing in progress --{@literal >} checkpointed ]
 */
private[spark] object CheckpointState extends Enumeration {

  //abstract class Value extends Ordered[Value] with Serializable
  //创建一个枚举的值的类型
  type CheckpointState = Value
 
 // Checkpoint 过程的三个状态
  // 创建一个新值
  val Initialized, CheckpointingInProgress, Checkpointed = Value

}

/**
 * 这个类包含了和 RDD checkpoint 相关的所有信息。
 * 这个类的每个实例都和一个RDD相关，管理这个相关的RDD checkpoint 进程，
 *    通过提供checkpointed RDD的更新分区、迭代器和首选位置来管理checkpoint的状态。
 *
 * This class contains all the information related to RDD checkpointing. Each instance of this
 * class is associated with an RDD. It manages process of checkpointing of the associated RDD,
 * as well as, manages the post-checkpoint state by providing the updated partitions,
 * iterator and preferred locations of the checkpointed RDD.
 */
private[spark] abstract class RDDCheckpointData[T: ClassTag](@transient private val rdd: RDD[T])
  extends Serializable {

  import CheckpointState._

  // The checkpoint state of the associated RDD.
  // 相关的 RDD 的 checkpoint 状态
  protected var cpState = Initialized

  // The RDD that contains our checkpointed data
  // 包含checkpoint数据的RDD
  private var cpRDD: Option[CheckpointRDD[T]] = None

  // TODO: are we sure we need to use a global lock in the following methods?

  /**
   * 这个RDD的checkpoint数据是否已经持久化了。 
   * Return whether the checkpoint data for this RDD is already persisted.
   */
  def isCheckpointed: Boolean = RDDCheckpointData.synchronized {
    cpState == Checkpointed
  }

  /**
   * 物化这个RDD，并持久化它的内容
   * 在这个RDD上调用action操作后，立马被调用。
   * Materialize this RDD and persist its content.
   * This is called immediately after the first action invoked on this RDD has completed.
   */
  final def checkpoint(): Unit = {
    // Guard against multiple threads checkpointing the same RDD by
    // atomically flipping the state of this RDDCheckpointData
    //通过自动调整这个RDDCheckpointData的状态，来防止多个线程checkpoint同一个RDD
    RDDCheckpointData.synchronized {
      if (cpState == Initialized) {
        cpState = CheckpointingInProgress
      } else {
        return
      }
    }

    //进行Checkpoint
    val newRDD = doCheckpoint()

    // Update our state and truncate the RDD lineage
    //更新状态，截断RDD的血统
    RDDCheckpointData.synchronized {
      cpRDD = Some(newRDD)
      cpState = Checkpointed
      rdd.markCheckpointed()  //见文末
    }
  }


  /**
   * Materialize this RDD and persist its content.
   *
   * Subclasses should override this method to define custom checkpointing behavior.  子类应重写这个方法，并定义个性化的checkpoint行为
   * @return the checkpoint RDD created in the process.
   */
  protected def doCheckpoint(): CheckpointRDD[T]

  /**
   * 返回包含checkpoint数据的RDD.
   * 仅会在状态为`Checkpointed`，才会被定义
   * Return the RDD that contains our checkpointed data.
   * This is only defined if the checkpoint state is `Checkpointed`.
   */
  def checkpointRDD: Option[CheckpointRDD[T]] = RDDCheckpointData.synchronized { cpRDD }

  /**
   * Return the partitions of the resulting checkpoint RDD.
   * For tests only.
   */
  def getPartitions: Array[Partition] = RDDCheckpointData.synchronized {
    cpRDD.map(_.partitions).getOrElse { Array.empty }
  }

}

/**
 * 用于同步checkpoint操作的全局锁。
 * Global lock for synchronizing checkpoint operations.
 */
private[spark] object RDDCheckpointData


//-------------------------------------------------------------------------

//Enumeration#Value

   /** Creates a fresh value, part of this enumeration. */
  //创建一个新值，作为这个枚举的一部分
  protected final def Value: Value = Value(nextId)

  /** The integer to use to identify the next created value. */
  protected var nextId: Int = initial

   /** Creates a fresh value, part of this enumeration, identified by the
   *  integer `i`.  用参数 i 标识这个新值，要在枚举中唯一
   *
   *  @param i An integer that identifies this value at run-time. It must be
   *           unique amongst all values of the enumeration.
   *  @return  Fresh value identified by `i`.
   */
  protected final def Value(i: Int): Value = Value(i, nextNameOrNull)

   /** Creates a fresh value, part of this enumeration, called `name`
   *  and identified by the integer `i`. 这个新值叫`name`，用参数 i 标识
   *
   * @param i    An integer that identifies this value at run-time. It must be
   *             unique amongst all values of the enumeration.
   * @param name A human-readable name for that value.
   * @return     Fresh value with the provided identifier `i` and name `name`.
   */
  protected final def Value(i: Int, name: String): Value = new Val(i, name)

    /** A class implementing the [[scala.Enumeration.Value]] type. This class
   *  can be overridden to change the enumeration's naming and integer
   *  identification behaviour.
   *
   * 类scala.Enumeration.Value的实现，可以重写这个类，来改变枚举的名称和标识
   */
  @SerialVersionUID(0 - 3501153230598116017L)
  protected class Val(i: Int, name: String) extends Value with Serializable {
    def this(i: Int)       = this(i, nextNameOrNull)
    def this(name: String) = this(nextId, name)
    def this()             = this(nextId)
    ...

  // The type of the enumerated values.枚举的值的类型
  @SerialVersionUID(7091335633555234129L)
  abstract class Value extends Ordered[Value] with Serializable {
    // the id and bit location of this enumeration value 所代表的枚举值的bit位置
    def id: Int

    // a marker so we can tell whose values belong to whom come reflective-naming time
    //标记某个值属于谁
    private[Enumeration] val outerEnum = thisenum

    override def compare(that: Value): Int =
      if (this.id < that.id) -1
      else if (this.id == that.id) 0
      else 1
    override def equals(other: Any) = other match {
      case that: Enumeration#Value  => (outerEnum eq that.outerEnum) && (id == that.id)
      case _                        => false
    }
    override def hashCode: Int = id.##

    // Create a ValueSet which contains this value and another one
    def + (v: Value) = ValueSet(this, v)
  }

//-------------------------------------------------

  /**
   * 将这个RDD的依赖的指向由原父RDD改变为 从checkpoint文件中创建的新的RDD
   * 忘掉旧的依赖和分区
   * Changes the dependencies of this RDD from its original parents to a new RDD (`newRDD`)
   * created from the checkpoint file, and forget its old dependencies and partitions.
   */
  private[spark] def markCheckpointed(): Unit = stateLock.synchronized {
    clearDependencies()
    partitions_ = null
    deps = null    // Forget the constructor argument for dependencies too
  }

  /**
   * 清理这个RDD的依赖。
   * 此方法必须确保 所有对原父RDDs的引用被移除，为了能使原父RDDs被垃圾收集
   *
   * RDD的子类可以重写这个方法，来实现自己的清理逻辑。
   *
   * Clears the dependencies of this RDD. This method must ensure that all references
   * to the original parent RDDs are removed to enable the parent RDDs to be garbage
   * collected. Subclasses of RDD may override this method for implementing their own cleaning
   * logic. See [[org.apache.spark.rdd.UnionRDD]] for an example.
   */
  protected def clearDependencies(): Unit = stateLock.synchronized {
    dependencies_ = null
  }
```