package org.tanuneko.undou.dataaccess

import java.io.IOException

import akka.util.Timeout
import com.redis.RedisClient
import com.typesafe.scalalogging.Logger
import org.tanuneko.undou.error.AppError
import org.tanuneko.undou.model.Media

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

// data key
// undou2:media:[mediaId]:(media object)
// media object (mediaId: Int, name: String, length: Long, countPlayed: Long, url: String)

trait DataAccess {

  def incrementCounter(mediaId: Int): Future[Long]
  def getMediaInfo(mediaId: Int): Future[Either[AppError, Media]]

  def mediaPrefix = "undou2:media"

}

class RedisDataAccess(redisClient: RedisClient)
                     (implicit tm: Timeout, ec: ExecutionContext)
  extends DataAccess {
  val logger = Logger[RedisDataAccess]
  override def incrementCounter(mediaId: Int): Future[Long] = {
    logger.info(s"incrementing count for ${mediaId}")
    redisClient.hincrby(s"${mediaPrefix}:${mediaId.toString}", "countPlayed", 1)
  }

  override def getMediaInfo(mediaId: Int): Future[Either[AppError, Media]] = {

    logger.info(s"hgetall for ${mediaId}")
    redisClient.hgetall[String](s"${mediaPrefix}:${mediaId}")
      .map { data =>
        logger.info(s"data: ${data}")
        if(data.size > 0) {
          val altMedia = data.get("altMediaId") match {
            case Some(id) => Some(id.toInt)
            case None => None
          }
          Right(Media(data("mediaId").toInt, data("name"), data("length").toLong, data("countPlayed").toLong,
            data("url"), altMedia))
        } else {
          Left(AppError("media.dataaccess.notfound", s"Requested media[${mediaId}] is not found"))
        }
      }
  }
}

class InMemDataAccess extends DataAccess {

  lazy val data: mutable.Map[Int, Media] = mutable.Map.empty[Int, Media]

  def add(media: Media) = data(media.mediaId) = media

  override def incrementCounter(mediaId: Int): Future[Long] = {
    // if not found, ignore the operation
    data.get(mediaId) match {
      case Some(media) =>
        data(mediaId) = media.incr
        Future.successful(1)
      case _ => Future.successful(-1L)
    }
  }

  override def getMediaInfo(mediaId: Int): Future[Either[AppError, Media]] = {
    Future.successful(
      data.get(mediaId) match {
        case Some(media) =>
          Right(media)
        case None =>
          Left(AppError("media.dataaccess.notfound", s"requested media[${mediaId}] not found"))
      }
    )
  }
}
