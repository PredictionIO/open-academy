package dataAnalysis
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SQLContext
import org.joda.time.DateTime

/**
  * Created by williamxue on 2/7/16.
  */
object AnalyzeData extends App {
  val sparkConf: SparkConf = Common.getSparkConf("DATA_ANALYZER")
  val DATAPATH = "/Users/williamxue/Dropbox\\ \\(MIT\\)/PredictionIO/fb/"

  val analyzer: DataAnalyzer = new DataAnalyzer(sparkConf, DATAPATH)
  analyzer.showCombinedTable()
  analyzer.showItemsTable()
  println(analyzer.countUsers)
  println(analyzer.findHighestPrice)
  println(analyzer.findLowestPrice)
  println(analyzer.findAveragePrice)
}
