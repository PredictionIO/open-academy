package DataProcessing

object DataProcessingMain extends App {

  val filesPath = "/Users/kairat/Downloads/fb/"

  val dataProcessor : DataProcessing = new DataProcessing(filesPath)

  dataProcessor.savePositiveSixMonthsRevenue()
}