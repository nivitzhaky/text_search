package controllers

import java.util.UUID
import javax.inject._

import akka.actor.ActorSystem
import algo.TextSearch
import com.github.tototoshi.play2.json4s.native.Json4s
import org.json4s.Extraction
import persistence._
import utils.Kafka

import scala.concurrent.ExecutionContext.Implicits.global

//import play.api.libs.json.Json
import org.json4s._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future

//case class MessageAndId(id: String, message: CommentMessage)
case class BatchNoTimestamp(id: String, wanted: Int)

@Singleton
class SimilarTextController @Inject() (system: ActorSystem) extends Controller with Json4s {
  import MongoObject._

  implicit val formats = new DefaultFormats {}
  implicit val as = system
  val mongoPersistence = new DataMongoPersistence(DBName.similarText)

  init()

  def addData = Action { request =>
    println(request.body.asText.get)
    MongoObject.memData += request.body.asText.get
    mongoPersistence.addData(Data(UUID.randomUUID().toString, request.body.asText.get))
    Ok(Extraction.decompose("OK")).withHeaders(headers: _*)

  }

  def simpleSearch() = Action { implicit request =>
    import org.json4s.native.Serialization.writePretty
    val text = request.body.asText.get
    val words = text.split(" ").filter(_.size > 1).toSet
    val res = mongoPersistence.getAllData().map { d =>
      val commonn = d.text.split(" ").toSet.intersect(words).size
      SearchRes(d.text, commonn)
    }.filter(_.weight > 0).sortBy(_.weight * -1)
    val resStr = writePretty(Extraction.decompose(res))
    Ok(resStr).withHeaders(headers: _*).withHeaders(("Content-Type", "application/json;"))
  }

  def levenshteinSearch() = Action { implicit request =>
    import org.json4s.native.Serialization.writePretty
    val text = request.body.asText.get
    val res = mongoPersistence.getAllData().map { d =>
      SearchRes(d.text, TextSearch.levenshtein(d.text, text)(d.text.length - 1, text.length - 1))
    }.filter(_.weight > 0).sortBy(_.weight * -1)
    val resStr = writePretty(Extraction.decompose(res))
    Ok(resStr).withHeaders(headers: _*).withHeaders(("Content-Type", "application/json;"))
  }

  def mongoSearch() = Action { implicit request =>
    import org.json4s.native.Serialization.writePretty
    val text = request.body.asText.get
    val res = mongoPersistence.searchForText(text).take(10).map { d =>
      SearchRes(d.text, 1)
    }.filter(_.weight > 0)
    val resStr = writePretty(Extraction.decompose(res))
    Ok(resStr).withHeaders(headers: _*).withHeaders(("Content-Type", "application/json;"))
  }

  def index = Action {
    Ok(Json.obj()).withHeaders(headers: _*)
  }

  def headers = List(
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Allow-Methods" -> "GET, POST, OPTIONS, DELETE, PUT",
    "Access-Control-Max-Age" -> "3600",
    "Access-Control-Allow-Headers" -> "Origin, Content-Type, Accept, Authorization",
    "Access-Control-Allow-Credentials" -> "true"
  )
  def options(p: String) = Action { request =>
    NoContent.withHeaders(headers: _*)
  }

  def init() = {
    mongoPersistence.createIndexes()
  }

}
