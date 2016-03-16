
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
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{ Row, UserDefinedFunction, DataFrame, SQLContext }
import java.nio.file.{ Files, Paths }

object writeCSV extends App {

  val sparkConf: SparkConf = Common.getSparkConf("writeCSV")
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext)

  import sqlContext.implicits._

  private val sparkDir: String = "/Users/kellypet/PredictionIO/vendors/spark-1.5.1/bin/"
  val customerDataFile = "data/customerSixMonthRevenue.parquet"
  val sixMonthRevenue: String = "positiveSixMonthRevenue"
  val csvDir = sparkDir + "analysis/"

  val customerData: DataFrame = sqlContext.read.parquet(sparkDir + customerDataFile)
  customerData.show()

  val revenue: DataFrame = customerData.select($"sixMonthRevenue")
  revenue.show()
  println("revenue table above")
  val posRevenue: DataFrame = revenue.filter($"sixMonthRevenue" > 0)
  posRevenue.show()
  println("posRev)")

  def writeToCSV() = {
    val revenueToCSV: RDD[String] = posRevenue.
      rdd.map[String] { row: Row => row.toString() }

    if (Files.notExists(Paths.get(csvDir + sixMonthRevenue))) {
      revenueToCSV.saveAsTextFile(csvDir + sixMonthRevenue)
    }
  }
  writeToCSV()
}