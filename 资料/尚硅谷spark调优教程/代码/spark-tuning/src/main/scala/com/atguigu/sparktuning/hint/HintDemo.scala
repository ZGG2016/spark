package com.atguigu.sparktuning.hint

import com.atguigu.sparktuning.utils.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object HintDemo {


  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("HintDemo")
      .setMaster("local[*]")
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)

    sparkSession.sql("use sparktuning")

    // TODO 广播Join的hint
//    sparkSession.sql("select /*+ BROADCAST(school) */ *  from test_student student left join test_school school on student.id=school.id").show()
//    sparkSession.sql("select /*+ BROADCASTJOIN(school) */ *  from test_student student left join test_school school on student.id=school.id").show()
//    sparkSession.sql("select /*+ MAPJOIN(school) */ *  from test_student student left join test_school school on student.id=school.id").show()


    // TODO SortMergeJoin的hint
//    sparkSession.sql("select /*+ SHUFFLE_MERGE(school) */ *  from test_student student left join test_school school on student.id=school.id").show()
//    sparkSession.sql("select /*+ MERGEJOIN(school) */ *  from test_student student left join test_school school on student.id=school.id").show()
//    sparkSession.sql("select /*+ MERGE(school) */ *  from test_student student left join test_school school on student.id=school.id").show()

    // TODO shuffle hash join 的hint
//    sparkSession.sql("select /*+ SHUFFLE_HASH(school) */ *  from test_student student left join test_school school on student.id=school.id").show()

    // TODO SHUFFLE_REPLICATE_NL join 的hint
//    sparkSession.sql("select /*+ SHUFFLE_REPLICATE_NL(school) */ *  from test_student student inner join test_school school on student.id=school.id").show()

    while(true){}

  }


}
