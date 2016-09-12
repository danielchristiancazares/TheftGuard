import SkurtTheftGuard.{StartPolling, StopPolling}
import akka.actor.{Actor, ActorContext, ActorLogging, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory

object Dispatcher {
  def props(endpoints: List[String]) = Props(classOf[Dispatcher], endpoints)

  private def makeRouter(context: ActorContext): Router = {
    val routees = Vector.fill(ConfigFactory.load().getInt("numOfCars")) {
      val r = context.actorOf(Worker.props)
      ActorRefRoutee(r)
    }

    Router(RoundRobinRoutingLogic(), routees)
  }

  private def getEndpoints: List[String] = {
    val config = ConfigFactory.load()
    val baseUri = config.getString("endpoint")
    (1 to config.getInt("numOfCars")).toList.map(i => s"$baseUri/carStatus/$i")
  }
}

class Dispatcher extends Actor with ActorLogging {
  var router = Dispatcher.makeRouter(context)

  def receive: Receive = {
    case StartPolling => Dispatcher.getEndpoints.foreach { endpoint => router.route(endpoint, sender()) }

    case StopPolling => context.system.terminate()
  }

}