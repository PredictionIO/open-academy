package Problem4

import Common.Common
import DataProcessing.DataProcessing
import org.apache.spark.sql.DataFrame
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.ml.feature.VectorAssembler

class TransformIntoSingleColumn(val filesPath : String, val sparkContext : SparkContext) extends Serializable{

  val dataProcessor : DataProcessing = new DataProcessing(filesPath, sparkContext)

  import dataProcessor.sqlContext.implicits._

  val finalDF : DataFrame = dataProcessor.getFinalDF()

  def printFinalDFCols() : Unit = {
    println
    finalDF.show()
    println
  }

  val transformedsixMonthsRevenue : DataFrame = new CategoricalBinarizer()
    .train(finalDF, "sixMonthsRevenue")
    .transform(finalDF, "sixMonthsRevenue", "transformedSixMonthsRevenue")

  val assembler = new VectorAssembler()
    .setInputCols(Array("hour", "mobile", "userFeatures"))
    .setOutputCol("features")

  def printTransformedDF() : Unit = {
    println
    transformedsixMonthsRevenue.show()
    println
  }


  //  val output = assembler.transform(dataset)
 // println(output.select("features", "clicked").first())

}