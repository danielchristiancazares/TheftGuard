import java.util.concurrent.TimeoutException

import SkurtTheftGuard.StartPolling
import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.pattern.ask
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class ActorSpec extends TestKit(ActorSystem("SkurtTheftSystem"))
  with DefaultTimeout with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll with MockFactory {

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  "A Dispatcher" should {
    "poll on a StartPolling command" in {
      val testActorRef = TestActorRef(new Dispatcher)

      val future = testActorRef ? StartPolling

      future onComplete {
        case Success(value) => value.asInstanceOf[Some[String]] should be(Some("Success"))
        case Failure(fail) => fail should be(None)
      }
    }
    "fail on a non-standard command" in {
      val testActorRef = TestActorRef(new Dispatcher)

      object InvalidCommand

      val future = testActorRef ? InvalidCommand

      future onComplete {
        case Success(value) => value.asInstanceOf[Some[String]] should be(Some("Success"))
        case Failure(fail) => fail should be(None)
      }
    }
  }

  "A Worker" should {
    "timeout on an invalid endpoint" in {
      val `invalidEndpoint` = "http://mock-interview-api.herokuapp.com/mockCarStatus/mockNumber"

      val testActorRef = TestActorRef(new Worker)
      val future = testActorRef ? invalidEndpoint
      intercept[TimeoutException] {
        Await.ready(future, 1 seconds)
        assert(true)
      }
    }

    "respond on a valid endpoint" in {
      val validEndpoint = "http://skurt-interview-api.herokuapp.com/carStatus/1"

      val testActorRef = TestActorRef(new Worker)
      val future = testActorRef ? validEndpoint

      future onComplete {
        case Success(value) => value.asInstanceOf[Some[String]] should be(Some("Success"))
        case Failure(fail) => fail should be(None)
      }
    }

    "respond on a valid HttpResponse" in {
      val httpResponse = HttpResponse(200, entity = "Resource found.")

      val testActorRef = TestActorRef(new Worker)
      val future = testActorRef ? httpResponse

      future onComplete {
        case Success(value) => value.asInstanceOf[Some[String]] should be(Some("Resource found."))
        case Failure(fail) => fail should be(None)
      }
    }

    "respond on a invalid HttpResponse" in {
      val httpResponse = HttpResponse(404, entity = "No resource found.")

      val testActorRef = TestActorRef(new Worker)
      val future = testActorRef ? httpResponse

      future onComplete {
        case Success(value) => value.asInstanceOf[Some[String]] should be(Some("Resource found."))
        case Failure(fail) => fail should be(None)
      }
    }
  }
}
