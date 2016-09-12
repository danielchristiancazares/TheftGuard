import java.util.concurrent.TimeoutException

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.pattern.ask
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class ActorSpec extends TestKit(ActorSystem("SkurtTheftSystem"))
  with DefaultTimeout with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll with MockFactory {

  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  "A Worker" should {
    "timeout on an invalid endpoint" in {
      val `invalidEndpoint` = "invalidendpoint.com"

      val testActorRef = TestActorRef(new Worker {
        override def receive = {
          case `invalidEndpoint` => throw new TimeoutException
        }
      })

      val future = testActorRef ? invalidEndpoint

      intercept[TimeoutException] {
        testActorRef.receive(invalidEndpoint)
      }

    }

    "respond on a valid endpoint" in {
      val validEndpoint = "http://skurt-interview-api.herokuapp.com/carStatus/11"

      val testActorRef = TestActorRef(new Worker)

      val future = testActorRef ? validEndpoint

      future onComplete {

        case Success(value) => value.asInstanceOf[HttpResponse]._1 should be(200)
        case Failure(fail) => fail should be(404)
      }
    }
  }
}
