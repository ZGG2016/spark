package rbo


import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import util.InitUtil
// 谓词下推
object PredicateTuning {

  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("PredicateTunning")
      .setMaster("local[*]") //TODO 要打包提交集群执行，注释掉
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)

    sparkSession.sql("use sparktuning;")

    println("=======================================Inner on 左表=======================================")
    // 过滤条件在 on 子句上
    // 优化前，在 inner join 上过滤；
    // 优化后，在扫描表时过滤（两表都下推）
    val innerStr1 =
    """
        |select
        |  l.courseid,
        |  l.coursename,
        |  r.courseid,
        |  r.coursename
        |from sale_course l join course_shopping_cart r
        |  on l.courseid=r.courseid and l.dt=r.dt and l.dn=r.dn
        |  and l.courseid<2
      """.stripMargin
    sparkSession.sql(innerStr1).show()
    sparkSession.sql(innerStr1).explain(mode = "extended")

    println("=======================================Inner where 左表=======================================")
    // 过滤条件在 where 子句上
    // 优化前，在 inner join 后过滤；
    // 优化后，在扫描表时过滤（两表都下推）
    val innerStr2 =
      """
        |select
        |  l.courseid,
        |  l.coursename,
        |  r.courseid,
        |  r.coursename
        |from sale_course l join course_shopping_cart r
        |  on l.courseid=r.courseid and l.dt=r.dt and l.dn=r.dn
        |where l.courseid<2
      """.stripMargin
    sparkSession.sql(innerStr2).show()
    sparkSession.sql(innerStr2).explain(mode = "extended")


    println("=======================================left on 左表=======================================")
    // 优化后，只下推右表，左表在join时过滤
    val leftStr1 =
      """
        |select
        |  l.courseid,
        |  l.coursename,
        |  r.courseid,
        |  r.coursename
        |from sale_course l left join course_shopping_cart r
        |  on l.courseid=r.courseid and l.dt=r.dt and l.dn=r.dn
        |  and l.courseid<2
      """.stripMargin
    sparkSession.sql(leftStr1).show()
    sparkSession.sql(leftStr1).explain(mode = "extended")

    println("=======================================left where 左表=======================================")
    // 优化后，下推左表和右表
    // 要注意： where过滤和上面的on过滤结果不同
    val leftStr2 =
      """
        |select
        |  l.courseid,
        |  l.coursename,
        |  r.courseid,
        |  r.coursename
        |from sale_course l left join course_shopping_cart r
        |  on l.courseid=r.courseid and l.dt=r.dt and l.dn=r.dn
        |where l.courseid<2
      """.stripMargin
    sparkSession.sql(leftStr2).show()
    sparkSession.sql(leftStr2).explain(mode = "extended")


    println("=======================================left on 右表=======================================")
    // 优化后，只下推右表，左表在join时过滤
    val leftStr3 =
      """
        |select
        |  l.courseid,
        |  l.coursename,
        |  r.courseid,
        |  r.coursename
        |from sale_course l left join course_shopping_cart r
        |  on l.courseid=r.courseid and l.dt=r.dt and l.dn=r.dn
        |  and r.courseid<2
      """.stripMargin
    sparkSession.sql(leftStr3).show()
    sparkSession.sql(leftStr3).explain(mode = "extended")

    println("=======================================left where 右表=======================================")
    // 优化后，下推左表和右表
    // 下推时使用了常量替换
    val leftStr4 =
      """
        |select
        |  l.courseid,
        |  l.coursename,
        |  r.courseid,
        |  r.coursename
        |from sale_course l left join course_shopping_cart r
        |  on l.courseid=r.courseid and l.dt=r.dt and l.dn=r.dn
        |where r.courseid<2 + 3
      """.stripMargin
    sparkSession.sql(leftStr4).show()
    sparkSession.sql(leftStr4).explain(mode = "extended")

  }


}
