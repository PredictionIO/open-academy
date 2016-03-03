package FbData

import FbData.CaseClasses._
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.DateTime
import org.apache.spark.sql.functions._


class DataProcessing(val filesPath: String) {
  val sparkConf: SparkConf = Common.getSparkConf("DATA_PROCESSING");
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext);

  import sqlContext.implicits._

  // DataFrames from the cvs files
  val usersDF : DataFrame = setUsersDF()
  val usersAdsDF : DataFrame = setUsersAdsDF()
  val itemsDF : DataFrame = setItemsDF()
  val conversionsDF : DataFrame = setConversionsDF()
  val viewsDF : DataFrame = setViewsDF()


  ///////////////// METHODS FOR SETTING DataFrames //////////////////

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

  def setItemsDF() : DataFrame = {
    val itemsDF : DataFrame = sparkContext
      .textFile(filesPath + "items.csv", 750)
      .map(line => {
        val fields : Array[String] = line.split(",")

        val itemId : String = fields(0)
        val style : String = fields(1)
        val personality : String = fields(2)
        val color : String = fields(3)
        val theme : String = fields(4)
        val price : Double  = fields(5).toDouble
        val category : String = fields(6)

        Item(itemId, style, personality, color, theme, price, category)
      })
      .toDF
      .dropDuplicates(Seq("itemId"))

    itemsDF
  }

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

        Conversion(userId, itemId, price, quantity, timestamp)
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
      .filter($"pagetype" !== "category")
  }


  ////////// METHODS for getting answers for quiz questions //////////
  def totalNumberOfUsers() : Long = {
    return usersDF.count
  }

  def mostExpensiveItemPrice() : Double = {
    itemsDF.agg(max(itemsDF.col("price")))
      .first()
      .get(0)
      .toString
      .toDouble
  }


  def cheapestItemPrice() : Double = {
    itemsDF
      .agg(min(itemsDF.col("price")))
      .first()
      .get(0)
      .toString
      .toDouble
  }

  def averageItemPrice() : Double = {
    itemsDF
      .agg(avg(itemsDF.col("price")))
      .first()
      .get(0)
      .toString
      .toDouble
  }

  def averageBoughtItemPrice() : Double = {
    conversionsDF
      .agg(avg(conversionsDF.col("price")))
      .first()
      .get(0)
      .toString
      .toDouble
  }

  def purchaseInFirst30Days() : Long = {
    usersDF.join(conversionsDF, usersDF("userId") === conversionsDF("userId"))
      .filter(($"signupTime" - $"timestamp") <= 30)
        .filter($"price" > 5000)
      //.dropDuplicates(Seq("userId"))
      .count()
  }

  def purchaseInFirst30DaysSpentMoreThan5000() : Long = {
    usersDF.join(conversionsDF, usersDF("userId") === conversionsDF("userId"))
      .filter(($"signupTime" - $"timestamp") <= 30)
      .filter($"price" > 5000)
      //.dropDuplicates(Seq("userId"))
      .count()
  }

  def earliestSignUpDate() : Any = {
    val instant = usersDF
      .agg(min(usersDF.col("signupTime")))
      .first()
      .get(0)
      .toString
      .toLong

    val dt : DateTime = new DateTime(instant)

    dt.toDate
  }

  def latestSignUpDate() : Any = {
    val instant = usersDF
      .agg(max(usersDF.col("signupTime")))
      .first()
      .get(0)
      .toString
      .toLong

    val dt : DateTime = new DateTime(instant)

    dt.toDate
  }


}

