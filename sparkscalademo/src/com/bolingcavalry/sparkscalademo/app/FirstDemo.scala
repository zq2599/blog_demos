package com.bolingcavalry.sparkscalademo.app

import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Description: 第一个scala语言的spark应用
  * @author: willzhao E-mail: zq2599@gmail.com
  * @date: 2019/2/16 20:23
  */
object FirstDemo {
  def main(args: Array[String]): Unit={
    val conf = new SparkConf()
      .setAppName("first spark app(scala)")
      .setMaster("local[1]");

    new SparkContext(conf)
      .parallelize(List(1,2,3,4,5,6))
      .map(x=>x*x)
      .filter(_>10)
      .collect()
      .foreach(println);
  }
}
