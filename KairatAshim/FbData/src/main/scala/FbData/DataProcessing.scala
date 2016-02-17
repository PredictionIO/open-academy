package FbData

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

object DataProcessing extends App {

  val sparkConf: SparkConf = Common.getSparkConf("DATA_PROCESSING");
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext);

  sparkContext
    .textFile("/Users/kairat/Downloads/fb/users.csv")
    .take(20)
    .foreach(println)
}