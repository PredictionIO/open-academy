package dataAnalysis

import org.apache.spark.SparkConf

/**
  * Created by williamxue on 2/7/16.
  */
object AnalyzeData extends App {
  val sparkConf: SparkConf = Common.getSparkConf("DATA_ANALYZER")
  val DATAPATH = "/Users/williamxue/Dropbox\\ \\(MIT\\)/PredictionIO/fb/"

  val analyzer: DataAnalyzer = new DataAnalyzer(sparkConf, DATAPATH)
  // analyzer.showCombinedTable()
  // analyzer.showItemsTable()
  analyzer.showCombinedTable()
  // analyzer.showUsersPurchasesTable()
  // println(analyzer.countUsers)
  // println(analyzer.countUsersOverPrice(5000.00))
  println(analyzer.countUsersItemBoughtTimeLimit(30))
  println(analyzer.countUsersItemBoughtAbovePrice(30, 5000.00))
  // println(analyzer.findHighestPrice)
  // println(analyzer.findLowestPrice)
  // println(analyzer.findAveragePrice)
  // println(analyzer.findAveragePriceBought)
  // println(analyzer.findEarliestSignUpDate)
  // println(analyzer.findLatestSignUpDate)
}
