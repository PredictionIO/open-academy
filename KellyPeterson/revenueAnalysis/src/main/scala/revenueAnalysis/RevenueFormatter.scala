package revenueAnalysis

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.joda.time.DateTime
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{ DataFrame, SQLContext, GroupedData }
import java.lang.Math
import java.util.concurrent.TimeUnit

object RevenueFormatter extends App {

  val sparkConf: SparkConf = Common.getSparkConf("RevenueCFormatter")
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext)
  import sqlContext.implicits._

  case class PositiveRevenue(
    sixMonthRevenue: Double)
    extends Serializable

  val positiveRevDF: DataFrame = sparkContext.textFile("/Users/kellypet/positiveSixMonthRevenue2.csv", 750)
    .map(line => {
      val fields: Array[String] = line.split(",")
      val numFields: Int = fields.size

      val sixMonthRevenue: Double = fields(0).toDouble

      PositiveRevenue(sixMonthRevenue)
    })
    .toDF
  positiveRevDF.write.parquet("data/postiveSixMonthRevenueParquet.parquet")
}