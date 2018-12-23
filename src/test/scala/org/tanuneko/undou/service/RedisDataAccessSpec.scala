package org.tanuneko.undou.service

import akka.actor.ActorSystem
import akka.util.Timeout
import com.redis.RedisClient
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, MustMatchers, WordSpec}
import org.tanuneko.undou.dataaccess.RedisDataAccess

import scala.concurrent.Await
import scala.concurrent.duration._

class RedisDataAccessSpec extends WordSpec
  with BeforeAndAfter
  with MustMatchers {

  implicit val sys = ActorSystem("RedisDataAccessSpec")
  implicit val tm = Timeout(10 seconds)
  implicit val ec = sys.dispatcher

  val redisClient = RedisClient("localhost", 6379)
  val redisService = new RedisDataAccess(redisClient)

  before {
    val f = redisClient.hmset("undou2:media:1", Map(
      "mediaId" -> "1",
      "name" -> "testmedia",
      "length" -> "35000",
      "countPlayed" -> "3389",
      "url" -> "http://tako.net/index.html"
    ))
    if(!Await.result(f, Duration("5 seconds")))
      fail("test data prep failed")
    else
      println("test data is inserted.")
  }

  "RedisDataAccess" should {
    "Get test data from Redis" in {
      val f = redisService.getMediaInfo(1)
      Await.result(f, Duration("5 seconds")) match {
        case Right(rez) => {
          println(rez)
          rez.mediaId must equal(1)
          rez.name must equal("testmedia")
          rez.length must equal(35000L)
          rez.countPlayed must equal(3389L)
          rez.url must equal("http://tako.net/index.html")
          rez.altMediaId must be(None)
        }
        case _ =>
          fail("...")
      }

    }
  }

  after {
    println("cleaning up test data..")
    val f = redisClient.del("undou2:media:1")
    if(Await.result(f, Duration("5 seconds")) != 1) {
      fail("test data removed should be 1")
    }
    println("terminating actor system..")
    Await.result(sys.terminate(), Duration.Inf)
  }

}
