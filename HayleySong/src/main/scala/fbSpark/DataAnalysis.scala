package fbSpark

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SQLContext
import org.joda.time.DateTime

object DataAnalysis extends App {

  val sparkConf: SparkConf = Common.getSparkConf("DATA_ANALYSIS")
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext)

  import sqlContext.implicits._
    
    var usersDF: DataFrame = sqlContext.read.parquet("users.parquet");
    println("done reading users parquet");
  
  }