import CSVParser.RequesterDetails
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import play.api.libs.json.{JsString, Json}
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.api.libs.ws.{StandaloneWSRequest, WSAuthScheme}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

object Commons {
  val system = ActorSystem()
  private val config = ConfigFactory.load()

  val url = config.getString("mysql.url")
  val user = config.getString("mysql.user")
  val password =  config.getString("mysql.password")

  private val esUrl = config.getString("es.url")
  private val esUser = config.getString("es.user")
  private val esPassword = config.getString("es.password")
  private val esIndex = config.getString("es.index")

  implicit val ws: StandaloneAhcWSClient = StandaloneAhcWSClient()(ActorMaterializer.create(system))

  def indextInEs(req: RequesterDetails)(implicit ws: StandaloneAhcWSClient, ec:ExecutionContext): Unit = {
    val obj = Json.obj(
      "id" -> JsString(req.id.getOrElse("")),
      "district" -> JsString(req.distritc.getOrElse("")),
      "location" -> JsString(req.location.getOrElse("")),
      "lat" -> JsString(req.lat.getOrElse("")),
      "long" -> JsString(req.lon.getOrElse("")),
      "date_added" -> JsString(req.date_added.getOrElse("")),
      "status" -> JsString(req.date_added.getOrElse("")),
      "requestee_phone" -> JsString(req.phone.getOrElse(""))
    )
    val fut = withWsClient(s"requests_import/doc/_doc/${req.id.get}")
      .withHttpHeaders("Content-Type" -> "application/json")
      .put(Json.stringify(obj))
    val res = Await.result(fut, 10.seconds)
    println(res.body)
  }

  private def withWsClient(path: String)(implicit ws: StandaloneAhcWSClient, ec:ExecutionContext): StandaloneWSRequest = {
    ws.url(s"$esUrl/$path")
      .withAuth(esUser, esPassword, WSAuthScheme.BASIC)
      .withHttpHeaders("Content-Type" -> "application/json")
  }

  def enrichWithData(columnId: String, value: String)(details: RequesterDetails): RequesterDetails = {
    val updated = columnId match {
      case "98" => details.copy(requestee = Some(value))
      case "110" => details.copy(phone = Some(value))
      case "125" => details.copy(status = Some(value))
      case "76" => details.copy(location = Some(value))
      case _ => details
    }
    updated
  }

}
