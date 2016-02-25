package FbData

object Main extends App {

  val filesPath = "/Users/kairat/Downloads/fb/"

  val dataProcessor : DataProcessing = new DataProcessing(filesPath)

  //println(dataProcessor.totalNumberOfUsers())
  //Common.customPrint("Most expensive item price", dataProcessor.mostExpensiveItemPrice())
  //Common.customPrint("Cheapset item price", dataProcessor.cheapestItemPrice())
  //Common.customPrint("Average item price", dataProcessor.averageItemPrice())
  //Common.customPrint("Average bought item price", dataProcessor.averageBoughtItemPrice())
  //Common.customPrint("Number of users who made purchase in the first 30 days", dataProcessor.purchaseInFirst30Days())
  //Common.customPrint("Number of users who made purchase in the first 30 days and spent more than $5000", dataProcessor.purchaseInFirst30DaysSpentMoreThan5000())
  Common.customPrint("earliest SignUp Date", dataProcessor.earliestSignUpDate())
  Common.customPrint("latest sign up date", dataProcessor.latestSignUpDate())
}