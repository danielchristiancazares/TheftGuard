import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.config.ConfigFactory

object Worker {
  def props = Props(classOf[Worker])
}

class Worker extends Actor with ActorLogging {
  final implicit val actorMaterializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  import akka.pattern.pipe
  import context.dispatcher
  val http = Http(context.system)
  val config = ConfigFactory.load()

  implicit val formats = org.json4s.DefaultFormats

  override def preStart = {
    log.debug("Starting a new worker...")
  }

  override def receive: Receive  = {
    case endpoint: String => {
      val headers: HttpHeader = HttpHeader.parse("Accept", "*/*") match {
        case x: ParsingResult.Ok => x.header
      }
      http.singleRequest(HttpRequest(GET, endpoint).withHeaders(headers)).pipeTo(self)
      Some("Success")
    }

    case HttpResponse(StatusCodes.OK, _, entity, _) => {
      val unmarsh = Unmarshal[HttpEntity](entity).to[String]
//      val json = compact(parse(unmarsh))
//      context.parent ! json
      Some(unmarsh)
    }

    case HttpResponse(code, _, _, _) => None
  }
}
