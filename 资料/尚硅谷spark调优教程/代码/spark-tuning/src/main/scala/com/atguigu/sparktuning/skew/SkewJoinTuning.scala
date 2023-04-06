package com.atguigu.sparktuning.skew

import com.atguigu.sparktuning.bean.{CourseShoppingCart, SaleCourse}
import com.atguigu.sparktuning.utils.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql._

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object SkewJoinTuning {
  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("SkewJoinTuning")
      .set("spark.sql.autoBroadcastJoinThreshold", "-1")
      .set("spark.sql.shuffle.partitions", "36")
          .setMaster("local[*]")
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)

    scatterBigAndExpansionSmall(sparkSession)

    //    while(true){}
  }


  /**
    * 打散大表  扩容小表 解决数据倾斜
    *
    * @param sparkSession
    */
  def scatterBigAndExpansionSmall( sparkSession: SparkSession ): Unit = {
    import sparkSession.implicits._
    val saleCourse = sparkSession.sql("select *from sparktuning.sale_course")
    val coursePay = sparkSession.sql("select * from sparktuning.course_pay")
      .withColumnRenamed("discount", "pay_discount")
      .withColumnRenamed("createtime", "pay_createtime")
    val courseShoppingCart = sparkSession.sql("select * from sparktuning.course_shopping_cart")
      .withColumnRenamed("discount", "cart_discount")
      .withColumnRenamed("createtime", "cart_createtime")

    // TODO 1、拆分 倾斜的key
    val commonCourseShoppingCart: Dataset[Row] = courseShoppingCart.filter(item => item.getAs[Long]("courseid") != 101 && item.getAs[Long]("courseid") != 103)
    val skewCourseShoppingCart: Dataset[Row] = courseShoppingCart.filter(item => item.getAs[Long]("courseid") == 101 || item.getAs[Long]("courseid") == 103)

    //TODO 2、将倾斜的key打散  打散36份
    val newCourseShoppingCart = skewCourseShoppingCart.mapPartitions(( partitions: Iterator[Row] ) => {
      partitions.map(item => {
        val courseid = item.getAs[Long]("courseid")
        val randInt = Random.nextInt(36)
        CourseShoppingCart(courseid, item.getAs[String]("orderid"),
          item.getAs[String]("coursename"), item.getAs[String]("cart_discount"),
          item.getAs[String]("sellmoney"), item.getAs[String]("cart_createtime"),
          item.getAs[String]("dt"), item.getAs[String]("dn"), randInt + "_" + courseid)
      })
    })
    //TODO 3、小表进行扩容 扩大36倍
    val newSaleCourse = saleCourse.flatMap(item => {
      val list = new ArrayBuffer[SaleCourse]()
      val courseid = item.getAs[Long]("courseid")
      val coursename = item.getAs[String]("coursename")
      val status = item.getAs[String]("status")
      val pointlistid = item.getAs[Long]("pointlistid")
      val majorid = item.getAs[Long]("majorid")
      val chapterid = item.getAs[Long]("chapterid")
      val chaptername = item.getAs[String]("chaptername")
      val edusubjectid = item.getAs[Long]("edusubjectid")
      val edusubjectname = item.getAs[String]("edusubjectname")
      val teacherid = item.getAs[Long]("teacherid")
      val teachername = item.getAs[String]("teachername")
      val coursemanager = item.getAs[String]("coursemanager")
      val money = item.getAs[String]("money")
      val dt = item.getAs[String]("dt")
      val dn = item.getAs[String]("dn")
      for (i <- 0 until 36) {
        list.append(SaleCourse(courseid, coursename, status, pointlistid, majorid, chapterid, chaptername, edusubjectid,
          edusubjectname, teacherid, teachername, coursemanager, money, dt, dn, i + "_" + courseid))
      }
      list
    })

    // TODO 4、倾斜的大key 与  扩容后的表 进行join
    val df1: DataFrame = newSaleCourse
      .join(newCourseShoppingCart.drop("courseid").drop("coursename"), Seq("rand_courseid", "dt", "dn"), "right")
      .join(coursePay, Seq("orderid", "dt", "dn"), "left")
      .select("courseid", "coursename", "status", "pointlistid", "majorid", "chapterid", "chaptername", "edusubjectid"
        , "edusubjectname", "teacherid", "teachername", "coursemanager", "money", "orderid", "cart_discount", "sellmoney",
        "cart_createtime", "pay_discount", "paymoney", "pay_createtime", "dt", "dn")


    // TODO 5、没有倾斜大key的部分 与 原来的表 进行join
    val df2: DataFrame = saleCourse
      .join(commonCourseShoppingCart.drop("coursename"), Seq("courseid", "dt", "dn"), "right")
      .join(coursePay, Seq("orderid", "dt", "dn"), "left")
      .select("courseid", "coursename", "status", "pointlistid", "majorid", "chapterid", "chaptername", "edusubjectid"
        , "edusubjectname", "teacherid", "teachername", "coursemanager", "money", "orderid", "cart_discount", "sellmoney",
        "cart_createtime", "pay_discount", "paymoney", "pay_createtime", "dt", "dn")

    // TODO 6、将 倾斜key join后的结果 与 普通key join后的结果，uinon起来
    df1
      .union(df2)
      .write.mode(SaveMode.Overwrite).insertInto("sparktuning.salecourse_detail")
  }


}
