package dataAnalysis

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.{Row, DataFrame, SQLContext}
import org.joda.time.DateTime
import org.apache.spark.sql.functions._

/**
  * Created by williamxue on 2/7/16.
  */
class DataAnalyzer(var sparkConf: SparkConf, var dataPath: String) {
  protected val sparkContext: SparkContext = new SparkContext(sparkConf)
  protected val sqlContext: SQLContext = new SQLContext(sparkContext)
  import sqlContext.implicits._

  // Initialize tables
  protected val usersTable: DataFrame = loadUsersTable()
  protected val usersAdsTable: DataFrame = loadUsersAdsTable()
  protected val itemsTable: DataFrame = loadItemsTable()
  protected val combinedTable: DataFrame = usersTable.join(usersAdsTable, "userId")


  private def loadUsersTable() : DataFrame = {

    val users: DataFrame = sparkContext
      .textFile(dataPath + "users.csv", 750)
      .map(line => {
        val fields: Array[String] = line.split(",")
        val numFields: Int = fields.length

        val userId: String = fields(0)

        val timestamp: String = fields(numFields - 1)
        val milliTime: Double = DateTime
          .parse(timestamp)
          .getMillis
          .toDouble / 1000

        val registerCountry: String = fields
          .slice(1, numFields - 1)
          .mkString(",")

        DataAnalyzer.Users(userId, milliTime, registerCountry)
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

  def showItemsTable() = {
    itemsTable.show
  }

  def showCombinedTable() = {
    combinedTable.show
  }

  def countUsers : Long = {
    usersTable.count
  }

  def findHighestPrice: Double = {
    itemsTable.select(max("price")).first().getAs[Double]("max(price)")
  }

  def findLowestPrice: Double = {
    itemsTable.select(min("price")).first().getAs[Double]("min(price)")
  }

  def findAveragePrice: Double = {
    itemsTable.select(avg("price")).first().getAs[Double]("avg(price)")
  }

  def findEarliestSignUpDate: String = {
    val earliestSignUpMillis: Long = (1000 * usersTable.select(min("time")).first().getAs[Double]("min(time)")).toLong
    // val earliestSignUpDateTime: DateTime = new DateTime(earliestSignUpMillis)
    Common.timeFormatter.print(earliestSignUpMillis)
  }

  def findLatestSignUpDate: String = {
    val latestSignUpMillis: Long = (1000 * usersTable.select(max("time")).first().getAs[Double]("max(time)")).toLong
    // val latestSignUpDateTime: DateTime = new DateTime(latestSignUpMillis)
    Common.timeFormatter.print(latestSignUpMillis)
  }

}

object DataAnalyzer {
  case class Users(
                    userId: String,
                    time: Double,
                    registerCountry: String)
    extends Serializable

  case class UsersAds(
                       userId: String,
                       utmSource: String,
                       utmCampaign: String,
                       utmMedium: String,
                       utmTerm: String,
                       utmContent: String)
    extends Serializable

  case class Items(
                    itemId: Long,
                    style: String,
                    personality: String,
                    color: String,
                    theme: String,
                    price: Double,
                    category: String
                  )
}

