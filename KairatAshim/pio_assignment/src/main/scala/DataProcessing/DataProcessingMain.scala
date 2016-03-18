package DataProcessing

import Common.Common
import org.apache.spark.{SparkContext, SparkConf}

object DataProcessingMain extends App {

  val filesPath = "/Users/kairat/Downloads/fb/"

  val sparkConf: SparkConf = Common.getSparkConf("DATA_PROCESSING")
  val sparkContext: SparkContext = new SparkContext(sparkConf)

  val dataProcessor : DataProcessing = new DataProcessing(filesPath, sparkContext)

  //dataProcessor.savePositiveSixMonthsRevenue()
  dataProcessor.savePositiveSixMonthsRevenueAsParquet()
}