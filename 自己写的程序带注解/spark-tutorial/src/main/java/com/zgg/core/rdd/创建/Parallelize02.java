package com.zgg.core.rdd.创建;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

/*
 * 从集合（内存）中创建 RDD - 数据如何分配
 * */
public class Parallelize02 {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("RddPartitionParallelize02");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        /*
          输出：
            numSlices=2:
              - part-0: [1,2]
              - part-1: [3,4,5]

            numSlices=3:
              - part-0: [1]
              - part-1: [2,3]
              - part-2: [4,5]

            numSlices=4:
              - part-0: [1]
              - part-1: [2]
              - part-2: [3]
              - part-3: [4,5]
         */

        /*
            - def parallelize[T: ClassTag](
                  seq: Seq[T],
                  numSlices: Int = defaultParallelism): RDD[T] = withScope {
                assertNotStopped()
                // 进入 ParallelCollectionRDD 类
                new ParallelCollectionRDD[T](this, seq, numSlices, Map[Int, Seq[String]]())
              }

            - override def getPartitions: Array[Partition] = {
                val slices = ParallelCollectionRDD.slice(data, numSlices).toArray
                slices.indices.map(i => new ParallelCollectionPartition(id, i, slices(i))).toArray
              }

            - def slice[T: ClassTag](seq: Seq[T], numSlices: Int): Seq[Seq[T]] = {
                 ...
                 // TODO
                 // length=5  numSlices=2   [0,2) [2,5)
                 // length=5  numSlices=3   [0,1) [1,3) [3,5)
                 // length=5  numSlices=4   [0,1) [1,2) [2,3) [3,5)
                 def positions(length: Long, numSlices: Int): Iterator[(Int, Int)] = {
                      (0 until numSlices).iterator.map { i =>
                        val start = ((i * length) / numSlices).toInt
                        val end = (((i + 1) * length) / numSlices).toInt
                        (start, end)  // 返回这个元组
                      }
                    }
                 seq match {
                    ...
                    case _ =>
                        val array = seq.toArray // To prevent O(n^2) operations for List etc
                        // TODO
                        positions(array.length, numSlices).map { case (start, end) =>
                            array.slice(start, end).toSeq   // 对数组切片，左闭右开
                    }.toSeq
                 }
                 ...
             }
         */

        List<Integer> lst = Arrays.asList(1, 2, 3, 4, 5);
//        JavaRDD<Integer> rdd = sc.parallelize(lst, 2);
//        JavaRDD<Integer> rdd = sc.parallelize(lst, 3);
        JavaRDD<Integer> rdd = sc.parallelize(lst, 4);
        rdd.saveAsTextFile("output");

        sc.stop();
    }
}
