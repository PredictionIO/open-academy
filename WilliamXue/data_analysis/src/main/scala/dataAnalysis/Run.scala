package dataAnalysis
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

/**
  * Created by williamxue on 2/7/16.
  */

object Run {

  def main(args:Array[String]): Unit = {

    val sparkConf: SparkConf = Common.getSparkConf("FB_SPARK")
    val sparkContext: SparkContext = new SparkContext(sparkConf)
    val sqlContext: SQLContext = new SQLContext(sparkContext)

    sqlContext
      .read
      .parquet("/rcx/home/marco/ltvStacking/STACKING_TEST_SET.parquet/")
      .show()
  }

}

