package revenueAnalysis

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SQLContext
import org.joda.time.DateTime
import org.apache.spark.sql.functions._
import org.apache.spark.sql.functions
import org.apache.spark.sql.GroupedData
import org.apache.spark.sql.{ UserDefinedFunction, DataFrame, SQLContext }
import java.lang.Math

import java.util.concurrent.TimeUnit

object DataToParquet extends App {

  val sparkConf: SparkConf = Common.getSparkConf("DataToParquet")
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext)

  private val usersCSV: String = "/Users/kellypet/Desktop/PredictionIO/Ecommerce-Price-Predict-Project/fb/users.csv"
  private val viewsCSV: String = "/Users/kellypet/Desktop/PredictionIO/Ecommerce-Price-Predict-Project/fb/views.csv"
  private val itemsCSV: String = "/Users/kellypet/Desktop/PredictionIO/Ecommerce-Price-Predict-Project/fb/items.csv"
  private val purchasesCSV: String = "/Users/kellypet/Desktop/PredictionIO/Ecommerce-Price-Predict-Project/fb/conversions.csv"
  private val adsCSV: String = "/Users/kellypet/Desktop/PredictionIO/Ecommerce-Price-Predict-Project/fb/users_ads.csv"

  import sqlContext.implicits._

  case class Users(
    userId: String,
    timestamp: String,
    signupTime: Double,
    registerCountry: String)
    extends Serializable

  val usersDF: DataFrame = sparkContext.textFile(usersCSV, 750)
    .map(line => {
      val fields: Array[String] = line.split(",")
      val numFields: Int = fields.size

      val userId: String = fields(0)
      val timestamp: String = fields(numFields - 1)
      val timeDate: DateTime = DateTime.parse(timestamp)
      val signupTime: Double = timeDate.getMillis.toDouble / 1000
      val registerCountry: String = fields
        .slice(1, numFields - 1)
        .mkString(",")

      Users(userId, timestamp, signupTime, registerCountry)
    })
    .toDF
    .dropDuplicates(Seq("userId"))
  usersDF.write.parquet("data/users.parquet")

  case class Conversions(
    userId: String,
    itemId: String,
    itemPrice: Double,
    quantity: Double,
    conversionTime: Double)
    extends Serializable

  val conversionDF: DataFrame = sparkContext.textFile(purchasesCSV, 750)
    .map(line => {
      val fields: Array[String] = line.split(",")
      val numFields: Int = fields.size

      val userId: String = fields(0)
      val itemId: String = fields(1)
      val itemPrice: Double = fields(2).trim.toDouble
      val quantity: Double = fields(3).trim.toInt
      val timestamp: String = fields(4)
      val timeDate: DateTime = DateTime.parse(timestamp)
      val conversionTime: Double = timeDate.getMillis.toDouble / 1000

      Conversions(userId, itemId, itemPrice, quantity, conversionTime)
    })
    .toDF
    .dropDuplicates(Seq("userId"))
  conversionDF.write.parquet("data/conversions.parquet")

  case class Views(
    userId: String,
    viewTime: Double)
    extends Serializable

  val viewsDF: DataFrame = sparkContext.textFile(viewsCSV, 750)
    .map(line => {
      val fields: Array[String] = line.split(",")
      val numFields: Int = fields.size

      val userId: String = fields(0)
      val timestamp: String = fields(2)
      val timeDate: DateTime = DateTime.parse(timestamp)
      val viewTime: Double = timeDate.getMillis.toDouble / 1000

      Views(userId, viewTime)
    })
    .toDF
    .dropDuplicates(Seq("userId"))
  viewsDF.write.parquet("data/views.parquet")

  case class UsersAds(
    userId: String,
    utmSource: String,
    utmCampaign: String,
    utmMedium: String,
    utmTerm: String,
    utmContent: String)

  val usersAds: DataFrame = sparkContext.textFile(adsCSV, 750)
    .map(line => {
      val fields = line.split(",")

      UsersAds(
        userId = fields(0),
        utmSource = fields(1),
        utmCampaign = fields(2),
        utmMedium = fields(3),
        utmTerm = fields(4),
        utmContent = fields(5))
    })
    .toDF
    .dropDuplicates(Seq("userId"))

  usersAds.write.parquet("data/ads.parquet")
  //var conversionDF = usersDF.join(usersConvDF, "userId")
  //  var usersDF:DataFrame = sqlContext.read.parquet("data/users.parquet")
  //  var userAndPurchaseDF = usersDF.join(conversionDF, "userId")
  //  var viewsDF: DataFrame = sqlContext.read.parquet("data/views.parquet")
  // var activityDF = viewsDF.join(userAndPurchaseDF, "userId")
  // activityDF.write.parquet("data/userActivity.parquet")

}
