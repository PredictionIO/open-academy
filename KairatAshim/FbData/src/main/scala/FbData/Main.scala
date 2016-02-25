package FbData

object Main extends App {

  val filesPath = "/Users/kairat/Downloads/fb/"

  val dataProcessor : DataProcessing = new DataProcessing(filesPath)

  println(dataProcessor.totalNumberOfUsers())
  println(dataProcessor.mostExpensiveItemPrice())
  //println(dataProcessor.cheapestItemPrice())
  //println(dataProcessor.averageItemPrice())
  //println(dataProcessor.averageBoughtItemPrice())
}