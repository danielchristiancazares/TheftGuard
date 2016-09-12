import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object SkurtTheftGuard {
  sealed trait SkurtMessage
  object StartPolling extends SkurtMessage
  object StopPolling extends SkurtMessage

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val baseUri = config.getString("endpoint")
    val endpoints = (1 to config.getInt("numOfCars")).toList.map(i => s"$baseUri/carStatus/$i")

    val system = ActorSystem("SkurtTheftSystem")
    val dispatcher = system.actorOf(Props(classOf[Dispatcher]), "dispatcher")

    while(true) {
      dispatcher ! StartPolling
      Thread.sleep(60000)
    }
  }
}
