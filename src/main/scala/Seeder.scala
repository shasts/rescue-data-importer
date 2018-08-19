import DatabaseQueryActor.QueryCommand
import Commons._
import akka.actor.Props
object Seeder {

  def main(arg: Array[String]): Unit = {
    val actorRef = system.actorOf(Props.apply(DatabaseQueryActor.create(user, password, url)))
    actorRef ! QueryCommand(0L)
  }
}
