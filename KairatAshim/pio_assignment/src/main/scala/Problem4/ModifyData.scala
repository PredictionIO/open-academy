package Problem4

import DataProcessing.DataProcessing
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{UserDefinedFunction, DataFrame}
import org.apache.spark.sql.functions._

object ModifyData extends App {

  val filesPath = "/Users/kairat/Downloads/fb/"

  val dataProcessor : DataProcessing = new DataProcessing(filesPath)

  import dataProcessor.sqlContext.implicits._

  var finalDF : DataFrame = dataProcessor.getFinalDF()

  // udf to create row index
  var rowIndex : Int = -1
  val createRowIndexCol: UserDefinedFunction = udf((revenue : Double) => rowIndex+=1)

  finalDF
    .sort("sixMonthsRevenue")
    .withColumn("rowIndex", createRowIndexCol($"sixMonthsRevenue"))
    .show()

  //val createBinaryResponseCol : UserDefinedFunction = udf((revenue: Double) => if ())

  def getPercentile(revenueVal : Double, valIndex : Int) : Double = {
    return 0
  }
}