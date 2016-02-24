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
      .textFile(filesPath + "item.csv", 750)
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
        val timestamp : Long = Common
          .timeFormatter
          .parseDateTime(fields(4))
          .getMillis

        Conversion(userId, itemId, price, quantity, timestamp)
      })
      .toDF
    }

  def setViewsDF() : DataFrame = {

    val pageTypes: List[String] = List("Product", "Collection")

    sparkContext
      .textFile(filesPath + "view.csv")
      .map(line => {
        val fields : Array[String] = line.split(",")

        val userId : String = fields(0)
        val itemId : String = fields(1)
        val timestamp : Long = Common
          .timeFormatter
          .parseDateTime(fields(2))
          .getMillis

        val pagetype : String = fields(3)

        View(userId, itemId, timestamp, pagetype)
      })
      .toDF
      .filter($"pagetype" === "product" || $"pagetype" === "collection")
  }


  ////////// METHODS for getting answers for quiz questions //////////

  def totalNumberOfUsers() : Long = {
    usersDF.count
  }

  def mostExpensiveItemPrice() : Any = {
    max(itemsDF("price"))
  }

  def helperMethod() : DataFrame = {
    val itemsDF1 : DataFrame = setItemsDF()
    return itemsDF1.describe("price")
  }

  def cheapestItemPrice() : Double = {
    itemsDF.select(min("price"))
      .first()
      .getAs[Double]("max(price)")
  }

  def averageItemPrice() : Double = {
    itemsDF.select(avg("price"))
      .first()
      .getAs[Double]("avg(price)")
  }

  def averageBoughtItemPrice() : Double = {
    conversionsDF
      .select(avg("price"))
      .first()
      .getAs[Double]("avg(price)")
  }


}

