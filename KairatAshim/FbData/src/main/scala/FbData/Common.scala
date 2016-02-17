package FbData

import org.apache.spark.SparkConf

object Common {
  // Get default Spark configuration
  def getSparkConf(appName : String): SparkConf = {
    new SparkConf()
      .setAppName(appName)
      .set("spark.storage.memoryFraction", "0.05")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.executor.cores", "2")
      .set("spark.executor.memory", "13G")
      .set("spark.default.parallelism", "24")
  }

  def printWrapper(a : Any): Unit = {
    println()
    println("----------")
    println(a)
    println("----------")
    println()
    Thread.sleep(1000)
  }
}

