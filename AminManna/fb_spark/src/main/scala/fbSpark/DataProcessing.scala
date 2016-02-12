package fbSpark

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SQLContext
import org.joda.time.DateTime

object DataProcessing extends App {

  val sparkConf: SparkConf = Common.getSparkConf("DATA_PROCESSING")
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext)

  import sqlContext.implicits._

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

      usersDF.join(usersAds, "userId").show

}
