package org.tanuneko.undou

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.redis.RedisClient
import com.typesafe.config.ConfigFactory
import org.tanuneko.undou.dataaccess.RedisDataAccess
import org.tanuneko.undou.route.RestRouter
import org.tanuneko.undou.service.DefaultMediaPlaybackService
import org.tanuneko.undou.tasks.{PLAYBACK, PlaybackActor, RESETMARKFILE}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object App {

  def main(args: Array[String]):Unit = {

    implicit val cfg = ConfigFactory.load("main")
    implicit val sys = ActorSystem("undou2", cfg)
    implicit val mat = ActorMaterializer()
    implicit val ec = sys.dispatcher
    implicit val tm = Timeout(10 seconds)

    // config
    val (host, port) = (cfg.getString("app.host"), cfg.getInt("app.port"))
    val appName = cfg.getString("app.akka.systemName")

    // services
    val redisClient = RedisClient(cfg.getString("redis.host"), cfg.getInt("redis.port"))
    val dataAccess = new RedisDataAccess(redisClient)
    val mediaPlaybackService = new DefaultMediaPlaybackService(dataAccess)

    // router
    val restRouter = new RestRouter(mediaPlaybackService)

    // scheduled jobs
    val playback = sys.actorOf(Props[PlaybackActor])

    sys.scheduler.schedule(
      initialDelay = 1.minutes,
      interval = 1.minutes,
      receiver = playback,
      message = PLAYBACK
    )

    sys.scheduler.schedule(
      initialDelay = 30.seconds,
      interval = 1.minutes,
      receiver = playback,
      message = RESETMARKFILE
    )

    // bootstrap
    val binder: Future[ServerBinding] = Http().bindAndHandle(restRouter.route, host, port)
    val log = Logging(sys.eventStream, appName)
    binder.map { serverBindings =>
      log.info(s"REST API is bound to ${serverBindings.localAddress}")
    }.onComplete {
      case Success(_) => log.info(s"Success to bind")
      case Failure(e) =>
        log.error(e, "Failed to bind")
        sys.terminate()
    }
  }

}
