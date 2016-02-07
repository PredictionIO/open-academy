package fbSpark


import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SQLContext
import org.joda.time.DateTime

object test extends App {
  val sparkConf: SparkConf = new SparkConf()
	.setMaster("spark://dhcp-18-189-120-217.dyn.mit.edu:7077")
	.setAppName("fdsdsd")
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext)

  val usersDF: String = sparkContext
    .textFile("/Users/mayuri/Downloads/fb/users.csv", 750)
    .first
    println(usersDF)
}
