package persistence

import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.commons.MongoDBObject
import persistence.MongoObject.client
import utils.MongoUtils

import scala.collection.mutable.ArrayBuffer

object MongoObject {
  lazy val client = MongoClient()
  var memData = ArrayBuffer.empty[String]
}
object DBName {
  val similarText = "similar-text"
}
object TableName {
  val data = "data"

}

case class Data(id: String, text: String)
case class SearchRes(text: String, weight: Int)

class DataMongoPersistence(dbName: String) extends MongoUtils {

  import TableName._
  lazy val db = client.getDB(dbName)

  def MDB = MongoDBObject
  def addData(text: Data) = {
    db(data).insert(toDBObj(text))
  }

  def getAllData(): List[Data] = {
    db(data).find().map(x => dbObjTo[Data](x)).toSet.toList
  }

  def searchForText(txt: String): List[Data] = db(data).find(MDB("$text" -> MDB("$search" -> txt))).toList
    .map(x => dbObjTo[Data](x))

  def createIndexes() = {
    db(data).createIndex(MDB("text" -> "text"))
  }

}

