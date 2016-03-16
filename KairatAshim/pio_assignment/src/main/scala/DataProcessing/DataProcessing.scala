package DataProcessing

import Common.CaseClasses.{User, UsersAd, View}
import DataProcessing.customClasses.{Conversion}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SQLContext, UserDefinedFunction}
import Common.Common
import org.apache.spark.sql.functions._
import org.joda.time.DateTime


class DataProcessing(val filesPath : String){
  val sparkConf: SparkConf = Common.getSparkConf("DATA_PROCESSING");
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext);

  import sqlContext.implicits._

  // DataFrames from csv files
  val conversionsDF : DataFrame = setConversionsDF()
  val viewsDF : DataFrame = setViewsDF()
  val usersDF : DataFrame = setUsersDF()
  val usersAdsDF : DataFrame = setUsersAdsDF()

  val usersAndUserAdsDF : DataFrame = usersDF
    .join(usersAdsDF, "userId")


  var filteredUsersDF : DataFrame = getFilteredDataFrame()

  def setConversionsDF() : DataFrame = {
    sparkContext
      .textFile(filesPath + "conversions.csv")
      .map(line => {
        val fields : Array[String] = line.split(",")

        val userId : String = fields(0)
        val itemId : String  = fields(1)
        val price : Double = fields(2).toDouble
        val quantity : Int = fields(3).toInt
        val timestamp : Long = DateTime
          .parse(fields(4))
          .getMillis

        Conversion(userId, price, quantity, timestamp)
      })
      .toDF
  }

  def setViewsDF() : DataFrame = {

    sparkContext
      .textFile(filesPath + "views.csv")
      .map(line => {
        val fields : Array[String] = line.split(",")

        val userId : String = fields(0)
        val itemId : String = fields(1)
        val timestamp : Long = DateTime
          .parse(fields(2))
          .getMillis

        val pagetype : String = fields(3)

        View(userId, itemId, timestamp, pagetype)

      })
      .toDF
      .filter($"pagetype" !== "Category")
  }


  def setUsersDF() : DataFrame = {
    sparkContext
      .textFile(filesPath + "users.csv", 750)
      .map(line => {
        val fields : Array[String] = line.split(",")
        val numFields : Int = fields.length

        val userId : String  = fields(0)

        val timestamp : String  = fields(numFields - 1)

        val millitime : Long = DateTime
          .parse(timestamp)
          .getMillis

        val registerCountry : String = fields
          .slice(1, numFields - 1)
          .mkString(",")

        User(userId, millitime, registerCountry)
      })
      .toDF
      .dropDuplicates(Seq("userId"))
  }

  def setUsersAdsDF() : DataFrame = {
    sparkContext
      .textFile(filesPath + "users_ads.csv", 750)
      .map(line => {
        val fields : Array[String] = line.split(",")

        val userId: String = fields(0)
        val utmSource: String = fields(1)
        val utmCampaign: String = fields(2)
        val utmMedium: String = fields(3)
        val utmTerm: String = fields(4)
        val utmContent:String = fields(5)

        UsersAd(userId, utmSource, utmCampaign, utmMedium, utmTerm, utmContent)
      })
      .toDF
      .dropDuplicates(Seq("userId"))
  }

  ///////// Question 1a ///////////

  def getConversionsDF() : DataFrame = {
    conversionsDF
  }

  def getViewsDF() : DataFrame = {
    viewsDF
  }


  ///////// Question 1b ///////////

  def getFilteredDataFrame() : DataFrame = {
    var minimumActivityTime : Long =  conversionsDF
      .agg(min(conversionsDF.col("eventTime")))
      .first()
      .get(0)
      .toString
      .toLong

    var maximumActivityTime : Long = conversionsDF
      .agg(max(conversionsDF.col("eventTime")))
      .first()
      .get(0)
      .toString
      .toLong

    val viewsMinimumActivityTime : Long = viewsDF
      .agg(min("eventTime"))
      .first()
      .get(0)
      .toString
      .toLong

    val viewsMaximumActivityTime : Long = viewsDF
      .agg(max(viewsDF.col("eventTime")))
      .first()
      .get(0)
      .toString
      .toLong



    if (viewsMinimumActivityTime < minimumActivityTime) {
      minimumActivityTime = viewsMinimumActivityTime
    }

    if (viewsMaximumActivityTime > maximumActivityTime) {
      maximumActivityTime = viewsMaximumActivityTime
    }

    val maxMinusSixMonths : Long = new DateTime(maximumActivityTime)
      .minusMonths(6)
      .getMillis

    val filteredUsersDF : DataFrame = usersAndUserAdsDF
      .filter($"signupTime" >= minimumActivityTime)
      .filter($"signupTime" < maxMinusSixMonths)

    filteredUsersDF
  }



  ///////// Question 1c ///////////

  def getConversionsDFWithRevenueColumn() : DataFrame = {
    val revenueFunction: UserDefinedFunction = udf((price: Double, quantity: Int) => price * quantity)

    val conversionsWithRevenueColumn: DataFrame = conversionsDF.withColumn("revenue", revenueFunction($"price", $"quantity"))

    conversionsWithRevenueColumn
  }

  val conversionsDFWithRevenueColumn : DataFrame = getConversionsDFWithRevenueColumn()


  ///////// Question 1d ///////////

  val aggregateRevenueDF : DataFrame = conversionsDFWithRevenueColumn
    .groupBy($"userId")
    .agg(sum("revenue"))
    .withColumnRenamed("sum(revenue)", "sixMonthsRevenue")
    .withColumnRenamed("userId", "id")


  ///////// Question 1e ///////////

  /*
  println("aggregate users df")
  println
  aggregateRevenueDF.show()
  println


  println("filter users df")
  println
  filteredUsersDF.show()
  println
  */

  def getFinalDF() : DataFrame = {

    val returnDF : DataFrame = filteredUsersDF
      .join(aggregateRevenueDF, filteredUsersDF("userId") === aggregateRevenueDF("id"), "left_outer")
      .drop("id")
      .na
      .fill(0.0, Seq("sixMonthsRevenue"))

    returnDF
  }

  var finalDF : DataFrame = getFinalDF()

  /*
  println
  finalDF.show()
  println
  */
  // User defined function to convert Double to String
  val makeColString: UserDefinedFunction = udf((revenue: Double) => revenue.toString)

  def savePositiveSixMonthsRevenue() : Unit = {
    val tempDF : DataFrame = finalDF
      .withColumn("sixMonthsRevenueString", makeColString($"sixMonthsRevenue"))
      .filter($"sixMonthsRevenue" > 0.0)
      .select("sixMonthsRevenueString")
      .withColumnRenamed("sixMonthsRevenueString", "value")
      .select("value")


    tempDF
      .write
      .text("/Users/kairat/Desktop/pio_assignment/revenueCSV")
  }

}

object customClasses {

  case class Conversion(
                         userId : String,
                         price : Double,
                         quantity : Int,
                         eventTime : Long
                       ) extends Serializable

}

