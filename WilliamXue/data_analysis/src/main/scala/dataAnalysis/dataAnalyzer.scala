package dataAnalysis

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.joda.time.DateTime
import org.apache.spark.sql.functions._
import java.util.concurrent.TimeUnit

/**
  * Created by williamxue on 2/7/16.
  */
class DataAnalyzer(var sparkConf: SparkConf, var dataPath: String) {
  protected val sparkContext: SparkContext = new SparkContext(sparkConf)
  protected val sqlContext: SQLContext = new SQLContext(sparkContext)
  import sqlContext.implicits._
//  protected val NumDays = 30

  // Initialize tables
  protected val usersTable: DataFrame = loadUsersTable()
  protected val usersAdsTable: DataFrame = loadUsersAdsTable()
  protected val itemsTable: DataFrame = loadItemsTable()
  protected val conversionsTable: DataFrame = loadConversionsTable()
  protected val viewsTable: DataFrame = loadViewsTable()
  // protected val usersPurchasesTable: DataFrame = loadUserPurchaseTable(NumDays)
  protected val combinedTable: DataFrame = usersTable.join(usersAdsTable, "userId")


  private def loadUsersTable() : DataFrame = {

    val users: DataFrame = sparkContext
      .textFile(dataPath + "users.csv", 750)
      .map(line => {
        val fields: Array[String] = line.split(",")
        val numFields: Int = fields.length

        val userId: String = fields(0)

        val timestamp: String = fields(numFields - 1)
        val signupTime: Long = DateTime
          .parse(timestamp)
          .getMillis

        val registerCountry: String = fields
          .slice(1, numFields - 1)
          .mkString(",")

        DataAnalyzer.Users(userId, signupTime, registerCountry)
      })
      .toDF
      .dropDuplicates(Seq("userId"))

    // return the DataFrame with users information
    users
  }

  private def loadUsersAdsTable() : DataFrame = {

    val usersAds: DataFrame = sparkContext
      .textFile(dataPath + "users_ads.csv", 750)
      .map(line => {
        val fields = line.split(",")

        val userId: String = fields(0)
        val utmSource: String = fields(1)
        val utmCampaign: String = fields(2)
        val utmMedium: String = fields(3)
        val utmTerm: String = fields(4)
        val utmContent:String = fields(5)

        DataAnalyzer.UsersAds(userId, utmSource, utmCampaign, utmMedium, utmTerm, utmContent)
      })
      .toDF
      .dropDuplicates(Seq("userId"))

    // return the DataFrame with usersAds information
    usersAds
  }

  private def loadItemsTable() : DataFrame = {

    val items: DataFrame = sparkContext
      .textFile(dataPath + "items.csv", 750)
      .map(line => {
        val fields = line.split(",")

        val itemId: Long = fields(0).toLong
        val style: String = fields(1)
        val personality: String = fields(2)
        val color: String = fields(3)
        val theme: String = fields(4)
        val price: Double = fields(5).toDouble
        val category: String = fields(6)

        DataAnalyzer.Items(itemId, style, personality, color, theme, price, category)
      })
      .toDF
      .dropDuplicates(Seq("itemId"))

    // return the DataFrame with item information
    items
  }

  private def loadConversionsTable() : DataFrame = {

    val conversions: DataFrame = sparkContext
      .textFile(dataPath + "conversions.csv", 750)
      .map(line => {
        val fields = line.split(",")

        val userId: String = fields(0)
        val itemId: String = fields(1)
        val price: Double = fields(2).toDouble
        val quantity: Int = fields(3).toInt
        val timestamp: Long = DateTime
          .parse(fields(4))
          .getMillis

        DataAnalyzer.Conversions(userId, itemId, price, quantity, timestamp)
      })
      .toDF

    // return DataFrame with conversions (purchase) information
    conversions
  }

  private def loadViewsTable() : DataFrame = {
    val ValidPageTypes: List[String] = List("Product", "Collection")

    val views: DataFrame = sparkContext
      .textFile(dataPath + "views.csv", 750)
      .map(line => {
        val fields = line.split(",")

        val userId: String = fields(0)
        val itemId: String = fields(1)
        val timestamp: Long = DateTime
          .parse(fields(3))
          .getMillis
        val pagetype = fields(4)

        DataAnalyzer.Views(userId, itemId, timestamp, pagetype)
      })
      .toDF
      .filter($"pagetype".isin(ValidPageTypes:_*))

    // return DataFrame with views information
    views
  }

  // TODO: Finish this!
  /**
    * Precondition: usersTable and conversionsTable must already be loaded
    * @param numDays the number of days since the start that we want to look at purchases from
    * @return a dataframe with information about users purchase histories over a certain amount of days
    */
  private def loadUserPurchaseTable(numDays:Int) : DataFrame = {

    val DaysUntilTimeLimit = TimeUnit.DAYS
      .toMillis(numDays)
      .toDouble / 1000

    val StartTime = usersTable
      .select(min("signupTime"))
      .first()
      .getAs[Double]("min(signupTime)")

    val TimeLimit = StartTime + DaysUntilTimeLimit

    println("Timelimit " + TimeLimit)

    val conversionsBeforeTimeLimitTable = conversionsTable.filter($"timestamp" < TimeLimit)

    conversionsBeforeTimeLimitTable.show

    val addPurchaseMadeColumn = udf((userId: String) => {
      println("udfStart")
      !(conversionsBeforeTimeLimitTable.filter($"userId" === userId).count() == 0)
    })

    val addAmountSpent = udf((userId:String) => {
      val thisUsersPurchases: DataFrame = conversionsBeforeTimeLimitTable
        .filter($"purchaseMade" === true && $"userId" === userId)

      val thisUsersPurchasedItemPrices: List[String] = thisUsersPurchases
        .select("price")
        .rdd.map(r => r(0).asInstanceOf[String])
        .collect()
        .toList

      val thisUsersPurchasedItemQuantities: List[String] = thisUsersPurchases
        .select("quantity")
        .rdd.map(r => r(0).asInstanceOf[String])
        .collect()
        .toList

      val thisUsersAmountSpent: Double = thisUsersPurchasedItemPrices
        .zip(thisUsersPurchasedItemQuantities)
        .map((entry: (String, String)) => {entry._1.toDouble * entry._2.toInt})
        .sum

      thisUsersAmountSpent
    })

    val userPurchasesTable = usersTable
      .withColumn("purchaseMade", addPurchaseMadeColumn(col("userId")))
      .withColumn("amountSpent", addAmountSpent(col("userId")))
      .filter($"purchaseMade" === true)
      .dropDuplicates(Seq("userId"))

    userPurchasesTable.printSchema

    userPurchasesTable
  }


  // Methods to show tables.

  def showItemsTable() = {
    itemsTable.show
  }

  def showCombinedTable() = {
    combinedTable.show
  }

//  def showUsersPurchasesTable() = {
//    usersPurchasesTable.show
//  }

  def countUsers : Long = {
    usersTable.count
  }

  // Methods to get various information about the data.
  // Useful for answering the quiz.

  def findHighestPrice: Double = {
    itemsTable.select(max("price"))
      .first()
      .getAs[Double]("max(price)")
  }

  def findLowestPrice: Double = {
    itemsTable.select(min("price"))
      .first()
      .getAs[Double]("min(price)")
  }

  def findAveragePrice: Double = {
    itemsTable
      .select(avg("price"))
      .first()
      .getAs[Double]("avg(price)")
  }

  def findAveragePriceBought: Double = {
    conversionsTable
      .select(avg("price"))
      .first()
      .getAs[Double]("avg(price)")
  }

  def countUsersItemBoughtTimeLimit(numDays: Int): Long = {
    val DaysUntilTimeLimit: Long = TimeUnit.DAYS
      .toMillis(numDays)

    val StartTime = usersTable
      .select(min("signupTime"))
      .first()
      .getAs[Long]("min(signupTime)")

    val TimeLimit = StartTime + DaysUntilTimeLimit

    val conversionsBeforeTimeLimitTable = conversionsTable.filter($"timestamp" < TimeLimit)

    conversionsBeforeTimeLimitTable
      .dropDuplicates(Seq("userId"))
      .count
  }

  def countUsersOverPriceTimeLimit(numDays: Int, price: Double): Long = {
    val DaysUntilTimeLimit: Long = TimeUnit.DAYS
      .toMillis(numDays)

    val StartTime = usersTable
      .select(min("signupTime"))
      .first()
      .getAs[Long]("min(signupTime)")

    val TimeLimit = StartTime + DaysUntilTimeLimit

    val conversionsBeforeTimeLimitTable = conversionsTable.filter($"timestamp" < TimeLimit)

    val userIds: List[String] = conversionsBeforeTimeLimitTable
      .dropDuplicates(Seq("userId"))
      .select("userId")
      .rdd.map(r => r(0).asInstanceOf[String])
      .collect()
      .toList

    var count: Int = 0

    conversionsBeforeTimeLimitTable.show

    for (userId: String <- userIds) {
//      println(getAmountSpentThisUser(userId, conversionsBeforeTimeLimitTable))
      if (getAmountSpentThisUser(userId, conversionsBeforeTimeLimitTable) > price)
        count += 1
    }

    count
  }

  private def getAmountSpentThisUser(userId: String, conversionsBeforeTimeLimitTable: DataFrame): Double = {
    val thisUsersPurchases: DataFrame = conversionsBeforeTimeLimitTable
      .filter($"userId" === userId)

    val thisUsersPurchasedItemPrices: List[Double] = thisUsersPurchases
      .select("price")
      .rdd.map(r => r(0).asInstanceOf[Double])
      .collect()
      .toList

    val thisUsersPurchasedItemQuantities: List[Int] = thisUsersPurchases
      .select("quantity")
      .rdd.map(r => r(0).asInstanceOf[Int])
      .collect()
      .toList

    val thisUsersAmountSpent: Double = thisUsersPurchasedItemPrices
      .zip(thisUsersPurchasedItemQuantities)
      .map((entry: (Double, Int)) => {entry._1 * entry._2})
      .sum

    thisUsersAmountSpent
  }

  def findEarliestSignUpDate: String = {
    val earliestSignUpDouble: Double = usersTable
      .select(min("signupTime"))
      .first()
      .getAs[Double]("min(signupTime)")

    val earliestSignUpMillis: Long = (1000 * earliestSignUpDouble).toLong
    Common.timeFormatter.print(earliestSignUpMillis)
  }

  def findLatestSignUpDate: String = {
    val latestSignUpDouble: Double = usersTable
      .select(max("signupTime"))
      .first()
      .getAs[Double]("max(signupTime)")

    val latestSignUpMillis: Long = (1000 * latestSignUpDouble).toLong
    Common.timeFormatter.print(latestSignUpMillis)
  }

}

object DataAnalyzer {

  case class Users(
                    userId: String,
                    signupTime: Long,
                    registerCountry: String)
    extends Serializable

  case class UsersAds(
                       userId: String,
                       utmSource: String,
                       utmCampaign: String,
                       utmMedium: String,
                       utmTerm: String,
                       utmContent: String
                     )
    extends Serializable

  case class Items(
                    itemId: Long,
                    style: String,
                    personality: String,
                    color: String,
                    theme: String,
                    price: Double,
                    category: String)
  extends Serializable

  case class Conversions(
                          userId: String,
                          itemId: String,
                          price: Double,
                          quantity: Int,
                          timestamp: Long)
  extends Serializable

  case class Views(
                    userId: String,
                    itemId: String,
                    timestamp: Long,
                    pagetype: String)
  extends Serializable

}

