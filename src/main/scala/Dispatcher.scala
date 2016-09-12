import SkurtTheftGuard.StartPolling
import akka.actor.{Actor, ActorContext, ActorLogging, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import com.typesafe.config.ConfigFactory
import io.orchestrate.client.{Client, OrchestrateClient}

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
    case StartPolling => {
      Dispatcher.getEndpoints.foreach { endpoint => router.route(endpoint, sender()) }
      println("success?")
      Some("Success")
    }
//    case str: String => {
//      val client: Client = new OrchestrateClient("45b2c8ee-4b7d-4a5a-97f7-67b0589e2027")
//      client.kv("location",java.util.UUID.randomUUID.toString)
//        .put(str)
//      println(str)
//      http.singleRequest(HttpRequest(method = PUT, uri = mongoUri, headers = List(authorization), entity = json)).pipeTo(context.parent)
    }
  }

}