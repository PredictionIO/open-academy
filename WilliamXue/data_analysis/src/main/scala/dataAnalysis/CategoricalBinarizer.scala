package dataAnalysis

import scala.collection.immutable.HashMap

import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.linalg.SparseVector
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.UserDefinedFunction

class CategoricalBinarizer extends java.io.Serializable {

  def train(data: DataFrame, inputCol: String): CategoricalBinarizerModel = {
    val sqlContext: SQLContext = data
      .sqlContext
    val sparkContext: SparkContext = sqlContext
      .sparkContext

    val indexMap: Broadcast[HashMap[String, Int]] = sparkContext.broadcast({
      val arrayMap: Array[(String, Int)] = data
        .select(inputCol)
        .distinct
        .map(_.getAs[Any](0).toString)
        .collect
        .zipWithIndex

      HashMap(arrayMap: _*)
    })

    new CategoricalBinarizerModel(indexMap)
  }
}

class CategoricalBinarizerModel(indexMap: Broadcast[HashMap[String, Int]]) extends java.io.Serializable {

  private def zeros(size: Int): Vector = {
    new SparseVector(size, Array[Int](), Array[Double]())
  }

  private val mapSize: Int = indexMap.value.size

  private val indexUDF: UserDefinedFunction = functions.udf((a: Any) => {
    val s: String = a.toString

    try {
      val index: Int = indexMap.value.apply(s)

      if (index == mapSize - 1) {
        zeros(mapSize - 1)
      } else {
        new SparseVector(mapSize - 1, Array(index), Array[Double](1))
          .asInstanceOf[Vector]
      }
    } catch {
      case e: java.util.NoSuchElementException => {
        zeros(mapSize - 1)
      }
    }
  })

  def transform(data: DataFrame, inputCol: String, outputCol: String): DataFrame  = {
    data
      .withColumn(outputCol, indexUDF(data(inputCol)))
  }

}