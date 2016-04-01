package Problem4

import Common.Common
import org.apache.spark.sql.DataFrame
import org.apache.spark.{SparkContext, SparkConf}

object Problem4Main extends App {

  val filesPath = "/Users/kairat/Downloads/fb/"

  val sparkConf: SparkConf = Common.getSparkConf("DATA_PROCESSING")
  val sparkContext: SparkContext = new SparkContext(sparkConf)

  //val DFwithBinaryResponse : BinaryResponse = new BinaryResponse(filesPath, sparkContext)

  val transformIntoSingleColumn : TransformIntoSingleColumn = new TransformIntoSingleColumn(filesPath, sparkContext)

  transformIntoSingleColumn.printTransformedDF()
}