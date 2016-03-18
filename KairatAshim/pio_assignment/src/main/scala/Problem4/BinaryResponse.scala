package Problem4

import Common.Common
import DataProcessing.DataProcessing
import DataProcessing.customClasses.Conversion
import org.apache.spark
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{SQLContext, Row, UserDefinedFunction, DataFrame}
import org.apache.spark.sql.functions._
import org.joda.time.DateTime
import org.apache.spark.SparkContext._

class BinaryResponse(val filesPath : String, val sparkContext : SparkContext){

  val dataProcessor : DataProcessing = new DataProcessing(filesPath, sparkContext)

  import dataProcessor.sqlContext.implicits._


  var finalDF : DataFrame = dataProcessor.getFinalDF()

  // sort revenue from smallest to highest
  val sortedFinalRDD : RDD[Row] =  finalDF
    .sort("sixMonthsRevenue")
    .rdd

  // Total number of revenue rows
  val numberOfRows : Long = finalDF.count

  // Number of revenues smaller than 95th percentile
  val indexOf95thPercentile : Long = numberOfRows*0.95.toLong

  // add indices to the sorted RDD
  val sortedFinalRDDKeyValues : RDD[(Long, Row)] =
    sortedFinalRDD
      .zipWithIndex
      .map{case(row, ind) => (ind, row)}

  val rowOf95thPercentile: Row = sortedFinalRDDKeyValues
    .lookup(indexOf95thPercentile)
    .head

  val revenueOf95thPercentile: Double = rowOf95thPercentile
    .getAs[Double]("sixMonthsRevenue")

  // rows with indices greater than separationIndex will have revenue is greater than the 95th percentile
  val createBinaryResponseColumn: UserDefinedFunction = udf((revenue: Double) => {
    if (revenue > revenueOf95thPercentile) 1
    else  0
  })

  def binarizeDF() : DataFrame = {
    val dfWithBinaryResponse : DataFrame = finalDF
      .withColumn("binaryResponse", createBinaryResponseColumn($"sixMonthsRevenue"))
      .drop("sixMonthsRevenue")

    dfWithBinaryResponse
  }
}