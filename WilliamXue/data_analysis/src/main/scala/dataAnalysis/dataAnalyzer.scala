package dataAnalysis
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SQLContext
import org.joda.time.DateTime

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

  def showCombinedTable() = {
    combinedTable.show
  }

  def countUsers : Long = {
    usersTable.count
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
}

