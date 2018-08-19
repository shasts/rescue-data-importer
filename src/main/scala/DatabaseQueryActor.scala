import java.sql.{Connection, DriverManager}

import CSVParser._
import Commons._
import DatabaseQueryActor.QueryCommand
import akka.actor.Actor

import scala.util.{Failure, Success, Try}

class DatabaseQueryActor(dbUser: String, dbPassword: String, dbUrl: String) extends Actor {

  var lastPostId = 0L

  var failures = 0

  var connection: Connection = _

  override def preStart(): Unit = {
    super.preStart()
    try {
      val driver = "com.mysql.jdbc.Driver"
      Class.forName(driver)
      connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  override def receive: Receive = {
    case QueryCommand(lastQueried) =>
      queryLatestRecord(lastPostId) match {
        case Success((postId, count)) =>
          failures = 0
          println(s"Indexed until $postId. Did $count in last attempt")
          lastPostId = postId.toLong
        case Failure(exception) =>
          exception.printStackTrace()
          failures += 1
          if (failures >= 100) {
            context.system.stop(self)
          }
      }
      self ! QueryCommand(lastPostId)
  }

  private def queryLatestRecord(lastQueried: Long): Try[(String, Int)] = {
    Try {
      val requests = scala.collection.mutable.Map[String, RequesterDetails]()
      val statement = connection.createStatement
      val rs = statement.executeQuery(
        s"""SELECT post_id,
           | form_attribute_id,
           | value,
           | created
           | FROM
           | post_text
           | WHERE post_id > ${lastPostId + 1}
           | AND post_id < ${lastPostId + 100}
           | """.stripMargin)
      val request = RequesterDetails()
      while (rs.next) {
        val postId = rs.getLong("post_id").toString
        val formAttributeId = rs.getLong("form_attribute_id").toString
        val value = rs.getString("value")
        val requestOpt = requests.get(postId)
        val enriched = request.copy(id = Some(postId.toString))
        requestOpt match {
          case None =>
            val request = RequesterDetails(id = Some(postId))
            requests + (postId -> enrichWithData(formAttributeId, value)(request))
          case Some(r) =>
            requests + (postId -> enrichWithData(formAttributeId, value)(r))
        }
        indextInEs(enriched)(ws, context.system.dispatcher)
      }
      (requests.keys.toList.sorted.max, requests.keys.size)
    }
  }

  override def postStop(): Unit = {
    connection.close()
  }
}

object DatabaseQueryActor {

  case class QueryCommand(lastQueried: Long)

  def create(dbUser: String, dbPassword: String, dbUrl: String): DatabaseQueryActor = {
    new DatabaseQueryActor(dbUser, dbPassword, dbUrl)
  }
}

