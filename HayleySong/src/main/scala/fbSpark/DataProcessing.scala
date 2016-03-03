//package fbSpark
//
//import org.apache.spark.SparkConf
//import org.apache.spark.SparkContext
//import org.apache.spark.sql.DataFrame
//import org.apache.spark.sql.SQLContext
//import org.joda.time.DateTime
//
//object DataProcessing extends App {
//
//  val sparkConf: SparkConf = Common.getSparkConf("DATA_PROCESSING")
//  val sparkContext: SparkContext = new SparkContext(sparkConf)
//  val sqlContext: SQLContext = new SQLContext(sparkContext)
//
//  import sqlContext.implicits._
////  case class Users(
////    userId: String,
////    time: Double,
////    registerCountry: String)
////    extends Serializable
////
////  val usersDF: DataFrame = sparkContext
////    .textFile("/home/hjsong/PredictionIO_project/fb_spark/fb/users.csv", 750)
////    .map(line => {
////      val fields: Array[String] = line.split(",")
////      val numFields: Int = fields.size
////
////      val userId: String = fields(0)
////      val timestamp: String = fields(numFields - 1)
////      val milliTime: Double = DateTime
////        .parse(timestamp)
////        .getMillis
////        .toDouble / 1000
////      val registerCountry: String = fields
////        .slice(1, numFields - 1)
////        .mkString(",")
////
////      Users(userId, milliTime, registerCountry)
////    })
////    .toDF
////    .dropDuplicates(Seq("userId"))
//////    
////usersDF.show(5)
////    println("Number of unique userIds: " + usersDF.count)
////    println("Number of unique users using distinct: " + usersDF.select("userId").distinct.count)
////    println("-----------Done processing usersDF-----------")
////usersDF.write.parquet("users.parquet");
//    
//    //My turn1: Create 'orders' table
//    //Make a conersions df using conversions.csv file
////    println("\n\nhere!")
////    case class Orders(
////        userId: String,
////        itemId: String,
////        price: Double,
////        quantity: Int,
////        time: Double
////    ) extends Serializable
////    
////    val ordersDF: DataFrame = sparkContext
////        .textFile("/home/hjsong/PredictionIO_project/fb_spark/fb/conversions.csv", 750)
////        .map(line =>{
////            val fields: Array[String] = line.split(",")
////            val numFields: Int = fields.size
////            val timestamp: String = fields(numFields-1)
////            val milliTime: Double = DateTime
////                .parse(timestamp)
////                .getMillis
////                .toDouble /1000
////            
////            Orders(
////                userId = fields(0),
////                itemId = fields(1),
////                price = fields(2).toDouble,
////                quantity = fields(3).toInt,
////                time = milliTime
////            )
////        })
////        .toDF();
////    
////    
////    println("---------------Done creating ordersDF------------------")
////    ordersDF.show();
////    ordersDF.printSchema();
////    ordersDF.write.parquet("orders.parquet");
////    
//////    usersDF.join(ordersDF, "userId")
////    println("---------------Done joining---------------------------------");
//
////    
//
//    println("\nCreating UsersAdsDF...");
//    case class UsersAds(
//      userId: String,
//      utmSource: String,
//      utmCampaign: String,
//      utmMedium: String,
//      utmTerm: String,
//      utmContent: String)
//
//    val usersAdsDF: DataFrame = sparkContext
//      .textFile("/home/hjsong/PredictionIO_project/fb_spark/fb/users_ads.csv", 750)
//      .map(line => {
//        val fields = line.split(",")
//
//        UsersAds(
//          userId = fields(0),
//          utmSource = fields(1),
//          utmCampaign = fields(2),
//          utmMedium = fields(3),
//          utmTerm = fields(4),
//          utmContent = fields(5))
//      })
//      .toDF();
//    
//    println("usersAdsDF created")
//    usersAdsDF.show();
//    
//    usersAdsDF.write.parquet("usersAds.parquet");
////      .dropDuplicates(Seq("userId"))
////    println("usersDF joined with usersAdsDF by userId---->>")
////      usersDF.join(usersAdsDF, "userId")
////    .show();
//    
////    println("\nCreating itemsDF...")
////    case class Items(
////    itemId: String,
////    style: String,
////    personality: String,
////    color: String,
////    theme: String,
////    price: Double,
////    category: String
////) extends Serializable
////
////
////var itemsDF: DataFrame = sparkContext
////.textFile("/home/hjsong/PredictionIO_project/fb_spark/fb/items.csv", 750)
////.map(line => {
////    var fields: Array[String] = line.split(",")
////    var numFields: Int = fields.length    
////    
////    Items(
////        itemId = fields(0),
////        style = fields(1),
////        personality = fields(2),
////        color = fields(3),
////        theme = fields(4),
////        price = fields(5).toDouble,
////        category = fields(numFields-1)
////        )
////})
////    .toDF();
////    println("-------itemsDF done----------")
//////    itemsDF.show(10);
//////    itemsDF.join(ordersDF, "itemId");
//////    println("::::writing itmesDF after joining on itemId")
////    itemsDF.show(10);
////    itemsDF.write.parquet("items.parquet");
////    
//    //gET THE ITMES WITH MAX PRICE, MIN PRICE 
//
//}
