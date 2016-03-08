package dataAnalysis

import org.apache.spark.SparkConf

/**
  * Created by williamxue on 2/7/16.
  */
object AnalyzeData extends App {
  val sparkConf: SparkConf = Common.getSparkConf("DATA_ANALYZER")
  val DATAPATH = "../../../data"

  val analyzer: DataAnalyzer = new DataAnalyzer(sparkConf, DATAPATH)

  analyzer.showCombinedTable()

  analyzer.exportCombinedTableToCSV()
}
