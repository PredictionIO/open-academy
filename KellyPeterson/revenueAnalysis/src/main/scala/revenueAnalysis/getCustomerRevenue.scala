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

object GetCustomerRevenue extends App {

  val sparkConf: SparkConf = Common.getSparkConf("GetCustomerRevenue")
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext)


  import sqlContext.implicits._

  // a, b.

  var conversionDF: DataFrame = sqlContext.read.parquet("data/conversions.parquet")
  var adsDF: DataFrame = sqlContext.read.parquet("data/ads.parquet")

  var userData: DataFrame = sqlContext.read.parquet("data/userActivity.parquet")
  val users: DataFrame = userData.join(adsDF, "userId")
  users.show()

  val minViewTime = userData.select(min("viewTime")).first().getDouble(0)
  val maxViewTime = userData.select(max("viewTime")).first().getDouble(0)

  val minConvTime = userData.select(min("conversionTime")).first().getDouble(0)
  val maxConvTime = userData.select(max("conversionTime")).first().getDouble(0)

  val minActivityTime = Math.min(minViewTime, minConvTime)
  val maxActivityTime = Math.max(maxViewTime, maxConvTime)

  // filter the user data frame so that it only consists of customers which satisfy the following two
  // properties:
  // (1) their signup time occurs after or at the minimum activity time, and
  // (2) there is at least six months difference between their signup time and the maximum activity time.

  private val secondsPerDay = 24 * 60 * 60 // 86400 seconds per day
  private val secondsPer30Days: Double = secondsPerDay * 30 //259200 seconds per 30 days
  val secondsPerSixMonths: Double = secondsPer30Days * 6

  // Filter users according to rule (1): their signup time occurs after or at the minimum activity time,
  val minSignupTimeFilter: (Double) => UserDefinedFunction = {
    (minimumActivityTime: Double) =>
      functions.udf({
        (signupTime: Double) =>
          {
            signupTime - minimumActivityTime >= 0
          }
      })
  }

  val usersMinTimeFilter: DataFrame = userData.filter(minSignupTimeFilter(minActivityTime)($"signupTime"))
  usersMinTimeFilter.show()
  val usersMaxTimeFilter: DataFrame = usersMinTimeFilter.filter($"signupTime" <= maxActivityTime - secondsPerSixMonths)
  usersMaxTimeFilter.show()
  val usersAggActivity: DataFrame = usersMaxTimeFilter.join(adsDF, "userId")
  usersAggActivity.show()

  // c.

  val revenueUDF: UserDefinedFunction = udf((price: Double, quantity: Int) => price * quantity)
  val revenueDF: DataFrame = conversionDF.withColumn("revenue", revenueUDF($"itemPrice", $"quantity"))

  // d.
  val aggRevenueByUser: DataFrame = revenueDF.groupBy($"userId").agg(sum("revenue")).withColumnRenamed("sum(revenue)", "sixMonthRevenue")
  val aggRevenueById: DataFrame = aggRevenueByUser.withColumnRenamed("userId", "id")

  // e.

  val joinedUsers: DataFrame = usersAggActivity.join(aggRevenueById, usersAggActivity("userId") === aggRevenueById("id"), "left_outer")
  joinedUsers.show()
  val usersNoNulls: DataFrame = joinedUsers.na.fill(Map("sixMonthRevenue" -> 0.0)).drop("id")
  val deDedupUsers: DataFrame = usersNoNulls.dropDuplicates(Seq("userId")).drop("timestamp").drop("viewTime").drop("itemId").drop("itemPrice").drop("quantity").drop("conversionTime")
  deDedupUsers.show()

  var parquetData = deDedupUsers.write.parquet("data/customerSixMonthRevenue.parquet")

}

