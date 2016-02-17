package FbData

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SQLContext

object Run extends App {

  val sparkConf: SparkConf = Common.getSparkConf("FB_Data")
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext);

  sqlContext
    .read
    .parquet()
    .show()
}