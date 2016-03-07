package sparkDataExplorer

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SQLContext
import org.joda.time.DateTime
import org.apache.spark.sql.functions._
import org.apache.spark.sql.GroupedData

object FbDataAnalysis extends App {

  val sparkConf: SparkConf = Common.getSparkConf("FbDataAnalysis")
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext)

  val usersCSV: String = "/Users/kellypet/Desktop/PredictionIO/Ecommerce-Price-Predict-Project/fb/users.csv"
  val adsCSV: String = "/Users/kellypet/Desktop/PredictionIO/Ecommerce-Price-Predict-Project/fb/users_ads.csv"
  val itemsCSV: String = "/Users/kellypet/Desktop/PredictionIO/Ecommerce-Price-Predict-Project/fb/items.csv"
  val purchasesCSV: String = "/Users/kellypet/Desktop/PredictionIO/Ecommerce-Price-Predict-Project/fb/conversions.csv"

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

  case class Items(
    itemId: String,
    itemStyle: String,
    itemPersonality: String,
    itemColor: String,
    itemTheme: String,
    itemPrice: Double,
    itemCategory: String)
    extends Serializable

  val itemsDF: DataFrame = sparkContext.textFile(itemsCSV, 750)
    .map(line => {
      val fields: Array[String] = line.split(",")
      val numFields: Int = fields.size

      val itemId: String = fields(0)
      val itemStyle: String = fields(1)
      val itemPersonality: String = fields(2)
      val itemColor: String = fields(3)
      val itemTheme: String = fields(4)
      val itemPrice: Double = fields(5).trim.toDouble
      val itemCategory: String = fields(6)

      Items(itemId, itemStyle, itemPersonality, itemColor, itemTheme, itemPrice, itemCategory)
    })
    .toDF
    .dropDuplicates(Seq("itemId"))

  case class UserConversions(
    userId: String,
    itemId: String,
    itemPrice: Double,
    quantity: Double,
    purchaseTime: Double)
    extends Serializable

  val usersConvDF: DataFrame = sparkContext.textFile(purchasesCSV, 750)
    .map(line => {
      val fields: Array[String] = line.split(",")
      val numFields: Int = fields.size

      val userId: String = fields(0)
      val itemId: String = fields(1)
      val itemPrice: Double = fields(2).trim.toDouble
      val quantity: Double = fields(3).trim.toInt
      val timestamp: String = fields(4)
      val timeDate: DateTime = DateTime.parse(timestamp)
      val purchaseTime: Double = timeDate.getMillis.toDouble / 1000

      UserConversions(userId, itemId, itemPrice, quantity, purchaseTime)
    })
    .toDF
    .dropDuplicates(Seq("userId"))



  ////// Question 1: Total # of users
  var conversionDF = usersDF.join(usersConvDF, "userId")
  conversionDF.describe("userId").show()

  val numUsers = conversionDF.count()
  println(f"\n\n The total number of users is: ${numUsers} \n\n")



  ///////// Question 2: How much is the most expensive item?
  val maxPrice = itemsDF.agg(max("itemPrice")).first()
  println(f"\n\n The most expensive item costs: \n ${maxPrice} \n\n")
  itemsDF.agg(max("itemPrice")).show()



  ////////Question 3: How much is the cheapest item?
  val minPrice = itemsDF.agg(min("itemPrice")).first()
  println(f"\n\n The cheapest item costs: ${minPrice} \n\n")
  itemsDF.agg(min("itemPrice")).show()



  //////// Question 4: What is the avg item price?
  val avgItemPrice = itemsDF.agg(avg("itemPrice")).first()
  println(f"\n\nThe average price of an item was: \n ${avgItemPrice} \n\n")
  itemsDF.agg(avg("itemPrice")).show()



  //////// Question 5: What is the avg bought item price?
  val avgPurchasedItemPrice = conversionDF.agg(avg("itemPrice")).first()
  println(f"\n\n The average price of a purchased item was: \n ${avgPurchasedItemPrice} \n\n")
  conversionDF.agg(avg("itemPrice")).show()




  //////// Question 6: how many users made a purchase within 30 days of signing up?

  conversionDF = conversionDF.withColumn("time_to_first_purchase", conversionDF("purchaseTime") - conversionDF("signupTime"))
  val secondsPerDay = 24 * 60 * 60 // 86400 seconds per day
  val secondsPer30Days: Double = secondsPerDay * 30 //259200 seconds per 30 days

  var conversion30DaysDF = conversionDF.filter(conversionDF("time_to_first_purchase") <= secondsPer30Days).groupBy("userId") //6121 or 6112
  val earlyPurchasers = conversion30DaysDF.count.agg(Map("count" -> "sum")).first()
  println(f"\n\n The number of users who made a purchase within 30 days of signup is: \n ${earlyPurchasers} \n\n")





  //////// Question 7: how many users spent more than $5000 within 30 days of signing up?

  var purchasein30DaysDF = conversionDF.filter(conversionDF("time_to_first_purchase") <= secondsPer30Days)
  purchasein30DaysDF = purchasein30DaysDF.withColumn("purchaseCost", purchasein30DaysDF("itemPrice") * purchasein30DaysDF("quantity"))

  val bigSpenders = purchasein30DaysDF.groupBy("userId").agg(sum("purchaseCost")).withColumn("amountSpent", $"sum(purchaseCost)").filter("amountSpent > 5000")
  val numBigSpenders = bigSpenders.count()
  bigSpenders.sort(desc("amountSpent")).show()

  val lesserSpenders = purchasein30DaysDF.groupBy("userId").agg(sum("purchaseCost")).withColumn("amountSpent", $"sum(purchaseCost)").filter("amountSpent <= 5000")

  // for comparison of the user group that spent >$5000 and the user group that spent <= $5000 in first 30 days
  println("\n\n\n\n\n we can read the number of users who spent MORE than $5000 in the first 30 days from the table below: \n\n\n\n\n\n")
  bigSpenders.describe("userId").show()

  println("\n\n\n\n\n we can read the number of users who spent $5000 or less in the first 30 days from the table below: \n\n\n\n\n\n")
  lesserSpenders.describe("userId").show()

  // print results for Question 7
  println(f"\n\n The number of users who spent more than 5000 in the first 30 days is: \n ${numBigSpenders}\n\n")




  //////// Question 8-9: what is the earliest signup time? Latest signup time?
  val signupTimeDF = usersDF.select($"userId", $"signupTime", $"timestamp")

  val earlySignupTimeDF = signupTimeDF.sort(asc("signupTime"))
  earlySignupTimeDF.show()

  val lateSignupTimeDF = signupTimeDF.sort(desc("signupTime"))
  lateSignupTimeDF.show()

  ////////// Question 8: what is the earliest signup time?
  val earliestSignupTime = earlySignupTimeDF.select("timestamp")
  earliestSignupTime.first()
  val earliest = earliestSignupTime.first()
  // prints the timestamp of the earliest signup
  println(s"\n\n The earliest signup time was: \n ${earliest} \n\n")

  //The earliest signup time was:
  //[2012-10-19T19:53:14.000Z]



  ////////// Question 9: what is the latest signup time?
  val latestSignupTime = lateSignupTimeDF.select("timestamp")
  latestSignupTime.first()
  val latest = latestSignupTime.first()

  // prints the timestamp of the latest signup
  println(s"\n\n The latest signup time was: \n ${latest} \n\n")

  //The latest signup time was:
  //[2012-10-19T19:53:14.000Z]

}