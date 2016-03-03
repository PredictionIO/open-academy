//package fbSpark
//
//import org.apache.spark.SparkConf
//import org.apache.spark.SparkContext
//import org.apache.spark.sql.DataFrame
//import org.apache.spark.sql.SQLContext
//
//object Run {
//
//  def main(args:Array[String]): Unit = {
//
//  val sparkConf: SparkConf = Common.getSparkConf("FB_SPARK")
//  val sparkContext: SparkContext = new SparkContext(sparkConf)
//  val sqlContext: SQLContext = new SQLContext(sparkContext)
//
//      Common.printWrapper("Hello Wolrd!")
//      
////  sqlContext
////    .read
////    //.parquet("/rcx/home/marco/ltvStacking/STACKING_TEST_SET.parquet/")
////    .show()
//  }
//
//}
