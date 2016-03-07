package fbSpark

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SQLContext
import org.joda.time.DateTime


object DataProcessing extends App {

  val sparkConf: SparkConf = Common.getSparkConf("DATA_PROCESSING")
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  sparkContext.setLogLevel("WARN")
  val sqlContext: SQLContext = new SQLContext(sparkContext)

  import sqlContext.implicits._

/*
  case class Users(
    userId: String,
    time: Double,
    registerCountry: String)
    extends Serializable

  val usersDF: DataFrame = sparkContext
    .textFile("/Users/amin/Desktop/users/fb/users.csv", 750)
    .map(line => {
      val fields: Array[String] = line.split(",")
      val numFields: Int = fields.size

      val userId: String = fields(0)
      val timestamp: String = fields(numFields - 1)
      val milliTime: Double = DateTime
        .parse(timestamp)
        .getMillis
        .toDouble / 1000
      val registerCountry: String = fields
        .slice(1, numFields - 1)
        .mkString(",")

      Users(userId, milliTime, registerCountry)
    })
    .toDF
    .dropDuplicates(Seq("userId"))
*/
  //usersDF.write.parquet("users.parquet")

  var usersDF : DataFrame = sqlContext.read.parquet("users.parquet") //Takes 4.5 seconds

  println("usersDF:")
  usersDF.show

/* UNUSED:
  case class UsersAds(
    userId: String,
    utmSource: String,
    utmCampaign: String,
    utmMedium: String,
    utmTerm: String,
    utmContent: String)

  val usersAds: DataFrame = sparkContext
    .textFile("/Users/amin/Desktop/users/fb//users_ads.csv", 750)
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

  println("usersAds:")
  usersAds.show

  val usersWithAds: DataFrame = usersDF.join(usersAds, "userId")
  
  println("usersWithAds:")
  usersWithAds.show
*/

/*
  case class Conversion(
    userId: String,
    itemId: String,
    price: String,
    conversionTime: Double,
    quantity: Double)

  val conversionDF: DataFrame = sparkContext
    .textFile("/Users/amin/Desktop/users/fb/conversions.csv", 750)
    .map(line => {
      val fields: Array[String] = line.split(",")
      val numFields: Int = fields.size

      val userId: String = fields(0)
      val itemId: String = fields(1)
      val price: String = fields(2)
      val quantity: Double = fields(3).trim.toInt
      val timestamp: String = fields(4)
      val milliTime: Double = DateTime
        .parse(timestamp)
        .getMillis
        .toDouble / 1000

      Conversion(userId, itemId, price, milliTime, quantity)
    })
    .toDF 

  conversionDF.write.parquet("conversions.parquet")
*/

  var conversionDF:DataFrame = sqlContext.read.parquet("conversions.parquet") //Takes 0.5 seconds

  println("conversionDF:")
  conversionDF.show

  var user_conversionDF = usersDF.join(conversionDF, "userId")  //Fast
  
  // user_conversionDF.show

  user_conversionDF = user_conversionDF.withColumn("time_since_signup", user_conversionDF("conversionTime") - user_conversionDF("time")) 

  println("user_conversionDF:")
  user_conversionDF.show

  user_conversionDF = user_conversionDF.filter("time_since_signup < 2592000")

  println("filtered user_conversionDF:")
  user_conversionDF.show

  user_conversionDF = user_conversionDF
    .groupBy($"userId").agg(Map("price" -> "sum" ))

  println("grouped by user user_conversionDF:")
  user_conversionDF.describe().show

  user_conversionDF = user_conversionDF
    .filter("sum(price) > 5000")

  println(user_conversionDF.count() + " users spent over $5000 in first 30 days")

}
