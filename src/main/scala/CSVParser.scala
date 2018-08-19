import scala.io.Source
import Commons._

object CSVParser {

  case class RequesterDetails(
    id: Option[String] = None,
    requestee: Option[String] = None,
    phone: Option[String] = None,
    distritc: Option[String] = None,
    location: Option[String] = None,
    date_added: Option[String] = None,
    status: Option[String] = None,
    lat: Option[String] = None,
    lon: Option[String] = None,
    situation: Option[String] = None
  )

  val requests = scala.collection.mutable.Map[String, RequesterDetails]()

  def main(arg: Array[String]): Unit = {

    arg.foreach { f =>
      val bufferedSource = Source.fromFile(f)
      bufferedSource.getLines().foreach { line =>
        val tokens = line.split(',').lift
        val id = tokens(0).get
        val columnId = tokens(1).get
        val value = tokens(2).get
        val requestOpt = requests.get(id)
        requestOpt match {
          case None =>
            val request = RequesterDetails(id = Some(id))
            requests + (id -> enrichWithData(columnId, value)(request))
          case Some(request) =>
            requests + (id -> enrichWithData(columnId, value)(request))
        }
      }
    }

    requests.values.toList.foreach { req =>
      Commons.indextInEs(req)(Commons.ws, Commons.system.dispatcher)
    }
  }
}
