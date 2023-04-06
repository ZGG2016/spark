package join

import util.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * 大小表join测试：强行广播
 */
object ForceBroadcastJoinTuning {

  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("ForceBroadcastJoinTuning")
      .set("spark.sql.autoBroadcastJoinThreshold","-1") // 关闭自动广播
      .setMaster("local[*]") //TODO 要打包提交集群执行，注释掉
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)


    //TODO SQL Hint方式
    val sqlstr1 =
      """
        |select /*+  BROADCASTJOIN(sc) */
        |  sc.courseid,
        |  csc.courseid
        |from sale_course sc join course_shopping_cart csc
        |on sc.courseid=csc.courseid
      """.stripMargin

    val sqlstr2 =
      """
        |select /*+  BROADCAST(sc) */
        |  sc.courseid,
        |  csc.courseid
        |from sale_course sc join course_shopping_cart csc
        |on sc.courseid=csc.courseid
      """.stripMargin

    val sqlstr3 =
      """
        |select /*+  MAPJOIN(sc) */
        |  sc.courseid,
        |  csc.courseid
        |from sale_course sc join course_shopping_cart csc
        |on sc.courseid=csc.courseid
      """.stripMargin



    sparkSession.sql("use sparktuning;")
    println("=======================BROADCASTJOIN Hint=============================")
    sparkSession.sql(sqlstr1).explain()
    println("=======================BROADCAST Hint=============================")
    sparkSession.sql(sqlstr2).explain()
    println("=======================MAPJOIN Hint=============================")
    sparkSession.sql(sqlstr3).explain()

    // TODO API的方式
    val sc: DataFrame = sparkSession.sql("select * from sale_course").toDF()
    val csc: DataFrame = sparkSession.sql("select * from course_shopping_cart").toDF()
    println("=======================DF API=============================")
    import org.apache.spark.sql.functions._
    broadcast(sc)
      .join(csc,Seq("courseid"))
      .select("courseid")
      .explain()
  }
}

/*

== Physical Plan ==
AdaptiveSparkPlan isFinalPlan=false
+- Project [courseid#2L]
   +- BroadcastHashJoin [courseid#2L], [courseid#16L], Inner, BuildLeft, false
      :- BroadcastExchange HashedRelationBroadcastMode(List(input[0, bigint, true]),false), [id=#93]
      :  +- Project [courseid#2L]
      :     +- Filter isnotnull(courseid#2L)
      :        +- FileScan parquet sparktuning.sale_course[courseid#2L,dt#14,dn#15] Batched: true, DataFilters: [isnotnull(courseid#2L)], Format: Parquet, Location: CatalogFileIndex(1 paths)[hdfs://bigdata101:9000/user/hive/warehouse/sparktuning.db/sale_course], PartitionFilters: [], PushedFilters: [IsNotNull(courseid)], ReadSchema: struct<courseid:bigint>
      +- Project [courseid#16L]
         +- Filter isnotnull(courseid#16L)
            +- FileScan parquet sparktuning.course_shopping_cart[courseid#16L,dt#22,dn#23] Batched: true, DataFilters: [isnotnull(courseid#16L)], Format: Parquet, Location: CatalogFileIndex(1 paths)[hdfs://bigdata101:9000/user/hive/warehouse/sparktuning.db/course_shoppi..., PartitionFilters: [], PushedFilters: [IsNotNull(courseid)], ReadSchema: struct<courseid:bigint>

 */