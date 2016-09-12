import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}

object Worker {
  def props = Props(classOf[Worker])
}

class Worker extends Actor with ActorLogging {
  final implicit val actorMaterializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  import akka.pattern.pipe
  import context.dispatcher
  val http = Http(context.system)

  override def preStart = {
    println("Starting a new worker...")
  }

  override def receive: Receive  = {
    case endpoint: String => {
      http.singleRequest(HttpRequest(GET, endpoint)).pipeTo(self)
      Some("Success")
    }

    case HttpResponse(StatusCodes.OK, _, entity, _) => {
      println("Received response")
      Some(Unmarshal(entity).to[String])
    }

    case HttpResponse(code, _, _, _) => {
      println("Response failure")
      None
    }
  }
}
