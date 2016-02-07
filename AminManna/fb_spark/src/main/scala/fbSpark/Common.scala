package fbSpark

import org.apache.spark.SparkConf
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

object Common{

  val timeFormatter: DateTimeFormatter = DateTimeFormat
    .forPattern("yyyy-MM-dd HH:mm:ss.S")


  // Get default Spark configuration.
  def getSparkConf(appName: String): SparkConf = {
    new SparkConf()
      .setAppName(appName)
      .set("spark.storage.memoryFraction", "0.05")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.executor.cores", "2")
      .set("spark.executor.memory", "13G")
      .set("spark.default.parallelism", "24")
  }

  def printWrapper(a: Any): Unit = {
    println()
    println("----------")
    println(a)
    println("----------")
    println()
    Thread.sleep(1000)
  }

}
