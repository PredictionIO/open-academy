package dataAnalysis

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.ml.classification.{LogisticRegression, LogisticRegressionModel}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.param.Param
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, UserDefinedFunction, DataFrame, SQLContext}
import org.joda.time.DateTime
import org.apache.spark.sql.functions._
import java.util.concurrent.TimeUnit
import java.nio.file.{Files, Paths}

/**
  * Created by williamxue on 2/7/16.
  */
class DataAnalyzer(var sparkConf: SparkConf, var dataPath: String) {

  protected val sparkContext: SparkContext = new SparkContext(sparkConf)
  protected val sqlContext: SQLContext = new SQLContext(sparkContext)
  import sqlContext.implicits._

  // Initialize default tables
  protected var usersTable: DataFrame = _
  protected var usersAdsTable: DataFrame = _
  protected var itemsTable: DataFrame = _
  protected var conversionsTable: DataFrame = _
  protected var viewsTable: DataFrame = _

  // Initialize the master table containing all user info
  protected var combinedTable: DataFrame = _

  // Initialize the table containing the binary feature vector
  protected var binaryVectorTable: DataFrame = _
  loadTables()

  // Initialize the models
  protected var basicLogisticRegressionModel: LogisticRegressionModel = _

  loadModels()

  private def loadTables() = {
    val usersDataPath = dataPath + "/preprocessed/users.parquet"
    val usersAdsDataPath = dataPath + "/preprocessed/users_ads.parquet"
    val itemsDataPath = dataPath + "/preprocessed/items.parquet"
    val conversionsDataPath = dataPath + "/preprocessed/conversions.parquet"
    val viewsDataPath = dataPath + "/preprocessed/views.parquet"
    val sixMonthRevenueDataPath = dataPath + "/preprocessed/sixMonthRevenue.parquet"
    val binaryVectorDataPath = dataPath + "/preprocessed/binaryVector.parquet"

    // Load the basic tables
    if (Files.exists(Paths.get(usersDataPath))) {
      print("Loading...")
      usersTable = sqlContext.read.parquet(usersDataPath)
    } else {
      usersTable = loadUsersTable()
      usersTable.write.parquet(usersDataPath)
    }

    if (Files.exists(Paths.get(usersAdsDataPath))) {
      usersAdsTable = sqlContext.read.parquet(usersAdsDataPath)
    } else {
      usersAdsTable = loadUsersAdsTable()
      usersAdsTable.write.parquet(usersAdsDataPath)
    }

    if (Files.exists(Paths.get(itemsDataPath))) {
      itemsTable = sqlContext.read.parquet(itemsDataPath)
    } else {
      itemsTable = loadItemsTable()
      itemsTable.write.parquet(itemsDataPath)
    }

    if (Files.exists(Paths.get(conversionsDataPath))) {
      conversionsTable = sqlContext.read.parquet(conversionsDataPath)
    } else {
      conversionsTable = loadConversionsTable()
      conversionsTable.write.parquet(conversionsDataPath)
    }

    if (Files.exists(Paths.get(viewsDataPath))) {
      viewsTable = sqlContext.read.parquet(viewsDataPath)
    } else {
      viewsTable = loadViewsTable()
      viewsTable.write.parquet(viewsDataPath)
    }

    // Load the table created by synthesizing user information that contains all of the user data
    if (Files.exists(Paths.get(sixMonthRevenueDataPath))) {
      combinedTable = sqlContext.read.parquet(sixMonthRevenueDataPath)
    } else {
      combinedTable = loadSixMonthRevenueTable()
      combinedTable.write.parquet(sixMonthRevenueDataPath)
    }

    if (Files.exists(Paths.get(binaryVectorDataPath))) {
      binaryVectorTable = sqlContext.read.parquet(binaryVectorDataPath)
    } else {
      binaryVectorTable = loadBinaryResponseTable()
      binaryVectorTable.write.parquet(binaryVectorDataPath)
    }
  }

  // Load raw tables of initial files
  private def loadUsersTable() : DataFrame = {

    val users: DataFrame = sparkContext
      .textFile(dataPath + "/fb/users.csv", 750)
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
      .textFile(dataPath + "/fb/users_ads.csv", 750)
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
      .textFile(dataPath + "/fb/items.csv", 750)
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
      .textFile(dataPath + "/fb/conversions.csv", 750)
      .map(line => {
        val fields = line.split(",")

        val userId: String = fields(0)
        val itemId: String = fields(1)
        val price: Double = fields(2).toDouble
        val quantity: Int = fields(3).toInt
        val eventTime: Long = DateTime
          .parse(fields(4))
          .getMillis

        DataAnalyzer.Conversions(userId, itemId, price, quantity, eventTime)
      })
      .toDF

    // return DataFrame with conversions (purchase) information
    conversions
  }

  private def loadViewsTable() : DataFrame = {
    val ValidPageTypes: List[String] = List("Product", "Collection")

    val views: DataFrame = sparkContext
      .textFile(dataPath + "/fb/views.csv", 750)
      .map(line => {
        val fields = line.split(",")

        val userId: String = fields(0)
        val itemId: String = fields(1)
        val eventTime: Long = DateTime
          .parse(fields(2))
          .getMillis
        val pageType: String = fields(3)

        DataAnalyzer.Views(userId, itemId, eventTime, pageType)
      })
      .toDF
      .filter($"pageType".isin(ValidPageTypes:_*))

    // return DataFrame with views information
    views
  }

  /**
    * Loads a table with all the information about the users including their revenues six months after they signed up.
    *
    * @return a dataframe representing a table with all the information about users that we have.
    */
  private def loadSixMonthRevenueTable(): DataFrame = {
    val revenueUDF: UserDefinedFunction = udf((price: Double, quantity: Int) => price * quantity)

    val conversionsWithRevTable: DataFrame = conversionsTable.withColumn("revenue", revenueUDF($"price", $"quantity"))

    var userSpentTable: DataFrame = conversionsWithRevTable
      .groupBy($"userId")
      .agg(sum("revenue"))
      .withColumnRenamed("sum(revenue)", "sixMonthRevenue")

    userSpentTable = userSpentTable.withColumnRenamed("userId", "id")

    val minActivityTime: Long = extractMinActivityTime()
    val maxActivityTime: Long = extractMaxActivityTime()

    val usersExtendedDataTable: DataFrame = usersTable.join(usersAdsTable, "userId")
    val usersDataEnoughTable: DataFrame = filterUsersSixMonths(usersExtendedDataTable, minActivityTime, maxActivityTime)

    val userPurchasesTable: DataFrame = usersDataEnoughTable
      .join(userSpentTable, usersDataEnoughTable("userId") === userSpentTable("id"), "left_outer")
      .drop("id")
      .dropDuplicates(Seq("userId"))
      .na.fill(0.0)

    userPurchasesTable
  }

  private def filterUsersSixMonths(tableToFilter: DataFrame, minActivityTime: Long, maxActivityTime: Long): DataFrame = {
    val sixMonths = 365/2.0.toLong
    val usersTableSixMonthsDataAvaliable : DataFrame = tableToFilter
      .filter($"signupTime" > minActivityTime)
      .filter($"signupTime" < maxActivityTime - TimeUnit.DAYS.toMillis(sixMonths))
    usersTableSixMonthsDataAvaliable
  }

  private def extractMinActivityTime() : Long = {
    val minActivityTimeConversions = conversionsTable
      .select(min("eventTime"))
      .first()
      .getAs[Long]("min(eventTime)")

    val minActivityTimeViews = viewsTable
      .select(min("eventTime"))
      .first()
      .getAs[Long]("min(eventTime)")

    math.min(minActivityTimeConversions, minActivityTimeViews)
  }

  private def extractMaxActivityTime() : Long = {
    val maxActivityTimeConversions = conversionsTable
      .select(max("eventTime"))
      .first()
      .getAs[Long]("max(eventTime)")

    val maxActivityTimeViews = viewsTable
      .select(max("eventTime"))
      .first()
      .getAs[Long]("max(eventTime)")

    math.max(maxActivityTimeConversions, maxActivityTimeViews)
  }

  def exportCombinedTableToCSV() = {
    val exportDirectory = dataPath + "/processed"

    val combinedTableRDD: RDD[String] = combinedTable
      .filter($"sixMonthRevenue" > 0)
      .select("sixMonthRevenue")
      .rdd.map[String]{row: Row => row.getAs[Double](0).toString}

    if (Files.notExists(Paths.get(exportDirectory + "/six_month_revenue_parts"))) {
      combinedTableRDD
        .saveAsTextFile(exportDirectory + "/six_month_revenue_parts")
    }
  }


  // Binary Response
  private def loadBinaryResponseTable(): DataFrame = {
    val sortedRevenueRDD: RDD[(Row,Long)] = combinedTable
      .sort($"sixMonthRevenue")
      .rdd
      .zipWithIndex

    val numElements = sortedRevenueRDD.count
    val ninetyFifthPercentileIndex = (0.95 * numElements).toLong

    val sortedIndexableConversionsTable: RDD[(Long, Row)] = sortedRevenueRDD
      .map{case(key, value) => (value, key)}

    val ninetyFifthPercentileRow: Row = sortedIndexableConversionsTable
      .lookup(ninetyFifthPercentileIndex)
      .head

    val ninetyFifthPercentileValue: Double = ninetyFifthPercentileRow
      .getAs[Double]("sixMonthRevenue")


    val binaryResponseUDF: UserDefinedFunction = udf((sixMonthRevenue: Double) => {
      if (sixMonthRevenue > ninetyFifthPercentileValue) 1.0
      else 0.0
    })

    val binaryResponseTable: DataFrame = combinedTable
      .withColumn("binaryResponse", binaryResponseUDF($"sixMonthRevenue"))
      .drop($"sixMonthRevenue")

    var binarizedFieldsTable: DataFrame = new CategoricalBinarizer()
      .train(binaryResponseTable, "userId")
      .transform(binaryResponseTable, inputCol="userId", outputCol="binaryUserId")
      .drop($"userId")

    binarizedFieldsTable= new CategoricalBinarizer()
      .train(binarizedFieldsTable, "signupTime")
      .transform(binarizedFieldsTable, inputCol="signupTime", outputCol="binarySignupTime")
      .drop($"signupTime")

    binarizedFieldsTable= new CategoricalBinarizer()
      .train(binarizedFieldsTable, "registerCountry")
      .transform(binarizedFieldsTable, inputCol="registerCountry", outputCol="binaryRegisterCountry")
      .drop($"registerCountry")

    binarizedFieldsTable= new CategoricalBinarizer()
      .train(binarizedFieldsTable, "utmSource")
      .transform(binarizedFieldsTable, inputCol="utmSource", outputCol="binaryUtmSource")
      .drop($"utmSource")

    binarizedFieldsTable= new CategoricalBinarizer()
      .train(binarizedFieldsTable, "utmCampaign")
      .transform(binarizedFieldsTable, inputCol="utmCampaign", outputCol="binaryUtmCampaign")
      .drop($"utmCampaign")

    binarizedFieldsTable= new CategoricalBinarizer()
      .train(binarizedFieldsTable, "utmMedium")
      .transform(binarizedFieldsTable, inputCol="utmMedium", outputCol="binaryUtmMedium")
      .drop($"utmMedium")

    binarizedFieldsTable= new CategoricalBinarizer()
      .train(binarizedFieldsTable, "utmTerm")
      .transform(binarizedFieldsTable, inputCol="utmTerm", outputCol="binaryUtmTerm")
      .drop($"utmTerm")

    binarizedFieldsTable= new CategoricalBinarizer()
      .train(binarizedFieldsTable, "utmContent")
      .transform(binarizedFieldsTable, inputCol="utmContent", outputCol="binaryUtmContent")
      .drop($"utmContent")

    val assembler: VectorAssembler = new VectorAssembler()
      .setInputCols(Array("binaryUserId",
                          "binarySignupTime",
                          "binaryRegisterCountry",
                          "binaryUtmSource",
                          "binaryUtmCampaign",
                          "binaryUtmMedium",
                          "binaryUtmTerm",
                          "binaryUtmContent"))
      .setOutputCol("features")

    val featureTable: DataFrame = assembler.transform(binarizedFieldsTable)
      .drop($"binaryUserId")
      .drop($"binarySignupTime")
      .drop($"binaryRegisterCountry")
      .drop($"binaryUtmSource")
      .drop($"binaryUtmCampaign")
      .drop($"binaryUtmMedium")
      .drop($"binaryUtmTerm")
      .drop($"binaryUtmContent")

    featureTable
  }

  private def loadModels() = {
    basicLogisticRegressionModel = createLogisticRegressionModel(binaryVectorTable)
  }

  private def createLogisticRegressionModel(binaryVectorTable: DataFrame): LogisticRegressionModel = {
    val lr: LogisticRegression = new LogisticRegression()
      .setFeaturesCol("features")
      .setLabelCol("binaryResponse")
      .setMaxIter(10)

    lr.fit(binaryVectorTable)
  }

  def printAllModelCoefficients()= {
    val basicLRModelCoeffs = basicLogisticRegressionModel.coefficients
    val basicLRModelFilteredCoeffs = basicLogisticRegressionModel
      .coefficients
      .toArray
      .filter(_ > 0.5)

    println("Basic Logistic Regression Coefficients size: " + basicLRModelCoeffs.size)
    println("Basic Logistic Regression Coefficients size after filtering: " + basicLRModelFilteredCoeffs.length)
  }

  // Methods to show tables.

  def showItemsTable() = {
    itemsTable.show
  }

  def showCombinedTable() = {
    combinedTable.show
  }

  // Methods to get various information about the data.================================================================
  // Useful for answering the quiz.

  def countUsers : Long = {
    combinedTable.count
  }

  def countUsersOverPrice(price: Double): Long = {
    combinedTable.filter($"revenue" > price)
      .count
  }

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

    val StartTime = combinedTable
      .select(min("signupTime"))
      .first()
      .getAs[Long]("min(signupTime)")

    val TimeLimit = StartTime + DaysUntilTimeLimit

    val conversionsBeforeTimeLimitTable = conversionsTable.filter($"eventTime" < TimeLimit)

    conversionsBeforeTimeLimitTable
      .dropDuplicates(Seq("userId"))
      .count
  }

  def countUsersItemBoughtAbovePrice(numDays: Int, price: Double): Long = {
    val DaysUntilTimeLimit: Long = TimeUnit.DAYS
    .toMillis(numDays)

    val StartTime = combinedTable
    .select(min("signupTime"))
    .first()
    .getAs[Long]("min(signupTime)")

    val TimeLimit = StartTime + DaysUntilTimeLimit

    val conversionsBeforeTimeLimitTable = conversionsTable.filter($"eventTime" < TimeLimit)

    val revenueUDF: UserDefinedFunction = udf((price: Double, quantity: Int) => price * quantity)

    val conversionsWithRevTimeLimit: DataFrame = conversionsBeforeTimeLimitTable
      .withColumn("revenue", revenueUDF($"price", $"quantity"))

    val userRevenueBeforeTimeLimitTable: DataFrame = conversionsWithRevTimeLimit
      .groupBy($"userId")
      .agg(sum("revenue"))
      .withColumnRenamed("sum(revenue)", "revenue")
      .filter($"revenue" > price)

    userRevenueBeforeTimeLimitTable.count
  }

  def findEarliestSignUpDate: String = {
    val earliestSignUpDouble: Double = combinedTable
      .select(min("signupTime"))
      .first()
      .getAs[Double]("min(signupTime)")

    val earliestSignUpMillis: Long = (1000 * earliestSignUpDouble).toLong
    Common.timeFormatter.print(earliestSignUpMillis)
  }

  def findLatestSignUpDate: String = {
    val latestSignUpDouble: Double = combinedTable
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
                          eventTime: Long)
  extends Serializable

  case class Views(
                    userId: String,
                    itemId: String,
                    eventTime: Long,
                    pageType: String)
  extends Serializable

}

