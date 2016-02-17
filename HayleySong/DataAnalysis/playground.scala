case class Items(
    itemId: String,
    style: String,
    personality: String,
    color: String,
    theme: String,
    price: Double,
    category: String
) extends Serializable


var itemsDF: DataFrame = sparkContext
.textFile("/home/hjsong/PredictionIO_project/fb_spark/fb/items.csv", 750)
.map(line => {
    var fields: Array[String] = line.split(",")
    var numFields: Int = fields.length    
    
    Items(
        itemId = fields(0),
        style = fields(1),
        personality = fields(2),
        color = fields(3),
        theme = fields(4),
        price = fields(5).toDouble(),
        category = fields(numFields-1)
})
    .toDF();
    println("-------itemsDF done----------")
    itemsDF.show(10);
    itemsDF.join(ordersDF, "itemId");
    println("::::writing itmesDF after joining on itemId")
    itemsDF.show(10);
    itemsDF.write.parquet("items.parquet");
