package revenueAnalysis

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SQLContext

object Run {

  def main(args:Array[String]): Unit = {

  val sparkConf: SparkConf = Common.getSparkConf("revenueAnalysis")
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext)

  sqlContext
    .read
    .parquet("revenueAnalysis.parquet")
    .show()
  }

}


