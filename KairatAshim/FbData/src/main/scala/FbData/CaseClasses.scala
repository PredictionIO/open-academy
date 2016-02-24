package FbData

object CaseClasses {

  case class User(
                    userId : String,
                    time : Long,
                    registerCountry : String
                  ) extends Serializable

  case class UsersAd(
                       userId : String,
                       utmSource : String,
                       utmCampaign : String,
                       utmMedium : String,
                       utmTerm : String,
                       utmContent : String
                     ) extends Serializable

  case class Item(
                   itemId : String,
                   style : String,
                   personality : String,
                   color : String,
                   theme : String,
                   price : Double,
                   category : String
                 ) extends Serializable

  case class Conversion(
                         userId : String,
                         itemId : String,
                         price : Double,
                         quantity : Int,
                         timestamp : Long
                       ) extends Serializable

  case class View(
                   userId : String,
                   itemId : String,
                   timestamp : Long,
                   pagetype : String
                 ) extends Serializable
}