package revenueAnalysis

import org.apache.spark.rdd.RDD
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.joda.time.DateTime
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{ UserDefinedFunction, DataFrame, SQLContext, Row, GroupedData }
import java.lang.Math
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.classification.LogisticRegressionModel
import org.apache.spark.ml.param.Param
import java.util.concurrent.TimeUnit

object LRM extends App {

  val sparkConf: SparkConf = Common.getSparkConf("LRM")
  val sparkContext: SparkContext = new SparkContext(sparkConf)
  val sqlContext: SQLContext = new SQLContext(sparkContext)
  import sqlContext.implicits._

  // Question 4:

  // a. Create a UserDefinedFunction instance to transform the sixMonthRevenue
  // column to a column called binaryResponse in which the column field is equal to 1 if
  // the corresponding revenue is greater than the 95th percentile, and 0 otherwise.

  var userData: DataFrame = sqlContext.read.parquet("data/userActivity.parquet")
  var adsDF: DataFrame = sqlContext.read.parquet("data/ads.parquet")

  val posSixMnthRev: DataFrame = sqlContext.read.parquet("data/postiveSixMonthRevenueParquet.parquet")
  posSixMnthRev.show()

  /**
   * compute percentile from an unsorted Spark RDD
   * @param data: input data set of Long integers
   * @param percentile: percentile to compute (eg. 95th percentile)
   * @return value of input data at the specified percentile
   */
  def computePercentile(data: RDD[Double], percentile: Double): Double = {
    val sortedData = data.sortBy(x => x)
    val numElements = sortedData.count()
    if (numElements == 1) sortedData.first()
    else {
      val n = (percentile / 100d) * (numElements + 1d)
      val k = math.floor(n).toLong
      val diff = n - k
      if (k <= 0) sortedData.first()
      else {
        val index = sortedData.zipWithIndex().map(_.swap)
        val last = numElements
        if (k >= numElements) {
          index.lookup(last - 1).head
        } else {
          index.lookup(k - 1).head + diff * (index.lookup(k).head - index.lookup(k - 1).head)
        }
      }
    }
  }

  val pqToRevenue: UserDefinedFunction = udf((itemPrice: Double, quantity: Double) => { itemPrice * quantity })
  val revenueToBinaryUDF: UserDefinedFunction = udf((sixMonthRevenue: Double, ninetyFifthPercentile: Double) => {
    if (sixMonthRevenue > ninetyFifthPercentile) 1.0
    else 0.0
  })

  var joinedData: DataFrame = sqlContext.read.parquet("data/userActivity.parquet")
  joinedData = joinedData.withColumn("revenue", pqToRevenue(joinedData("itemPrice"), joinedData("quantity")))
  val allData: DataFrame = joinedData.join(adsDF, "userId")

  val revenues = allData.select("revenue").map { x: Row => x.getAs[Double](0) }
  val ninetyFifthPercentile: Double = computePercentile(revenues, 0.95)

  val binaryRevenueDF: DataFrame = allData.withColumn("binaryResponse", revenueToBinaryUDF(allData("revenue"), lit(ninetyFifthPercentile)))
  binaryRevenueDF.show

  val categoricalFeatures = List("userId", "registerCountry", "utmSource", "utmCampaign", "utmMedium", "utmTerm", "utmContent")

  def binarizeColLabel(label: String): String = {
    val outputLabel = "binary" + label
    outputLabel
  }

  def getBinaryLabels(labels: List[String]): List[String] = {
    val binarizedLabels = labels.map((x: String) => binarizeColLabel(x))
    binarizedLabels
  }

  def binarizeCols(): DataFrame = {
    val binarizedLabels = getBinaryLabels(categoricalFeatures)
    println(binarizedLabels)

    var categoricalBinarizer: DataFrame = new CategoricalBinarizer()
      .train(binaryRevenueDF, categoricalFeatures(0))
      .transform(binaryRevenueDF, inputCol = categoricalFeatures(0), outputCol = binarizedLabels(0))
      .drop(categoricalFeatures(0))

    val indexList: List[Int] = List(1, 2, 3, 4, 5, 6)

    for (x <- indexList) {
      categoricalBinarizer = new CategoricalBinarizer()
        .train(categoricalBinarizer, categoricalFeatures(x))
        .transform(categoricalBinarizer, inputCol = categoricalFeatures(x), outputCol = binarizedLabels(x))
        .drop(categoricalFeatures(x));
    }

    val assembler = new VectorAssembler()
      .setInputCols(Array("binaryuserId", "binaryregisterCountry", "binaryutmSource", "binaryutmCampaign", "binaryutmMedium", "binaryutmTerm", "binaryutmContent")) //getBinaryLabels(categoricalFeatures)).toArray)
      .setOutputCol("features")

    val assembledBinaryVector: DataFrame = assembler.transform(categoricalBinarizer)
      .drop($"binaryuserId").drop($"binaryregisterCountry").drop($"binaryutmSource").drop($"binaryutmCampaign").drop($"binaryutmMedium").drop($"binaryutmTerm)").drop($"binaryutmContent")
    assembledBinaryVector
  }
  val assembledBinaryVector = binarizeCols()
  // Using LogisticRegressionModel to construct a classification model predicting binaryResponse given an input binary response

  def constructLRModel(): LogisticRegressionModel = {
    val lr: LogisticRegression = new LogisticRegression()
      .setMaxIter(10)
      .setRegParam(0.2)
      .setFeaturesCol("features")
      .setLabelCol("binaryResponse")

    lr.fit(assembledBinaryVector)
  }

  def constructLRModelElastic(): LogisticRegressionModel = {
    val lr: LogisticRegression = new LogisticRegression()
      .setMaxIter(10)
      .setRegParam(0.3)
      .setElasticNetParam(0.8)
      .setFeaturesCol("features")
      .setLabelCol("binaryResponse")
    lr.fit(assembledBinaryVector)
  }

  val lrModel: LogisticRegressionModel = constructLRModel()
  val coeffs = lrModel.weights.toArray.map(i => math.abs(i))

  val numOrigCoeffs = coeffs.length

  val filteredCoeffs = coeffs.filter(_ > 0.05)
  val numFilteredCoeffs = filteredCoeffs.length

  println(s"Size of original coefficient vector: ${numOrigCoeffs}")
  println(s"Size of filtered coefficient vector: ${numFilteredCoeffs}")

  // elastic net regularization
  val elasticLrModel: LogisticRegressionModel = constructLRModelElastic()
  val elasticCoeffs = elasticLrModel.weights.toArray.map(i => math.abs(i))

  val numOrigElasticCoeffs = elasticCoeffs.length

  val filteredElasticCoeffs = elasticCoeffs.filter(_ > 0.05)
  val numFilteredElasticCoeffs = filteredElasticCoeffs.length

  println(s"Size of original coefficient vector (with elastic net regularization): ${numOrigElasticCoeffs}")
  println(s" ; Size of filtered coefficient vector (with elastic net regularization): ${numFilteredElasticCoeffs}")
}
