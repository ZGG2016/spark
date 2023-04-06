# ReliableRDDCheckpointData

```java
package org.apache.spark.rdd

import scala.reflect.ClassTag

import org.apache.hadoop.fs.Path

import org.apache.spark._
import org.apache.spark.internal.Logging
import org.apache.spark.internal.config.CLEANER_REFERENCE_TRACKING_CLEAN_CHECKPOINTS

/**
 * 将RDD数据写入到可靠存储的checkpoint的实现
 * 这允许 drivers 在计算之前的状态失败时重新启动。
 *
 * An implementation of checkpointing that writes the RDD data to reliable storage.
 * This allows drivers to be restarted on failure with previously computed state.
 */
private[spark] class ReliableRDDCheckpointData[T: ClassTag](@transient private val rdd: RDD[T])
  extends RDDCheckpointData[T](rdd) with Logging {

  // The directory to which the associated RDD has been checkpointed to
  // This is assumed to be a non-local path that points to some reliable storage
  //RDD被checkpoint到的目录。假定为指向某个可靠存储的非本地路径。
  private val cpDir: String =
    ReliableRDDCheckpointData.checkpointPath(rdd.context, rdd.id)
      .map(_.toString)
      .getOrElse { throw new SparkException("Checkpoint dir must be specified.") }

  /**
   * 返回这个RDD checkpoint 的目录
   * Return the directory to which this RDD was checkpointed.
   * If the RDD is not checkpointed yet, return None.
   */
  def getCheckpointDir: Option[String] = RDDCheckpointData.synchronized {
    if (isCheckpointed) {
      Some(cpDir.toString)
    } else {
      None
    }
  }

/** 
 * Return whether the checkpoint  data for this RDD is already persisted.
 *  这个RDD的checkpoint数据是否已经持久化了。 
 * def isCheckpointed: Boolean = RDDCheckpointData.synchronized {
 *   cpState == Checkpointed
 * }
 */

  /**
   * 物化这个RDD，把它的内容写入到可靠的DFS
   * 在这个RDD上调用action操作后，立马被调用。
   * Materialize this RDD and write its content to a reliable DFS.
   * This is called immediately after the first action invoked on this RDD has completed.
   */
  protected override def doCheckpoint(): CheckpointRDD[T] = {
  	//把这个RDD写入到Checkpoint文件,返回一个ReliableCheckpointRDD表示这个RDD
    val newRDD = ReliableCheckpointRDD.writeRDDToCheckpointDirectory(rdd, cpDir)

    // Optionally clean our checkpoint files if the reference is out of scope
    //如果引用超出范围,清理checkpoint文件 ？？？
    if (rdd.conf.get(CLEANER_REFERENCE_TRACKING_CLEAN_CHECKPOINTS)) {
      rdd.context.cleaner.foreach { cleaner =>
      // Register a RDDCheckpointData for cleanup when it is garbage collected.
        cleaner.registerRDDCheckpointDataForCleanup(newRDD, rdd.id)
      }
    }

    logInfo(s"Done checkpointing RDD ${rdd.id} to $cpDir, new parent is RDD ${newRDD.id}")
    newRDD
  }

}

private[spark] object ReliableRDDCheckpointData extends Logging {

  /** Return the path of the directory to which this RDD's checkpoint data is written. */
  //返回这个RDD的checkpoint数据写入的目录路径。
  def checkpointPath(sc: SparkContext, rddId: Int): Option[Path] = {
    sc.checkpointDir.map { dir => new Path(dir, s"rdd-$rddId") } //拼接rddid
  }

//private[spark] var checkpointDir: Option[String] = None

  /**
   * 清理和这个RDD checkpoint数据相关的文件
   * Clean up the files associated with the checkpoint data for this RDD.    
   */
  def cleanCheckpoint(sc: SparkContext, rddId: Int): Unit = {
    checkpointPath(sc, rddId).foreach { path =>
      path.getFileSystem(sc.hadoopConfiguration).delete(path, true)
    }
  }
}

```