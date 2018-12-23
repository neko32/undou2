package org.tanuneko.undou.service

import java.awt.Desktop
import java.net.URI

import akka.actor.ActorSystem
import akka.event.Logging
import com.typesafe.scalalogging.Logger
import org.tanuneko.undou.dataaccess.DataAccess
import org.tanuneko.undou.error.AppError
import org.tanuneko.undou.model.{Media, MediaPlaybackByRedirect, MediaPlaybackResponse, MediaPlaybackSuccess}
import org.tanuneko.undou.util.{MarkFileUtil, YoutubeUtil}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success
import scala.util.control.NonFatal

trait MediaPlaybackService {

  def play(mediaId: Int): Future[Either[AppError, MediaPlaybackResponse]]

}

class DefaultMediaPlaybackService
(da: DataAccess)
(implicit actorSys: ActorSystem, ec: ExecutionContext) extends MediaPlaybackService {

  lazy val logger = Logger[DefaultMediaPlaybackService]

  override def play(mediaId: Int): Future[Either[AppError, MediaPlaybackResponse]] = {
    logger.info(s"mediaId received - ${mediaId}")

    // handle mark file
    if (!MarkFileUtil.markFileExists) {
      MarkFileUtil.create
    }
    MarkFileUtil.mark
    logger.info(s"Marked as ${MarkFileUtil.markStr}")

    // retrieve media
    (for {
      media <- da.getMediaInfo(mediaId)
      _ <- da.incrementCounter(mediaId)
    } yield {
      media
    })
      .map {
        case Right(a) =>
          actorSys.scheduler.scheduleOnce(400 seconds) {
            Desktop.getDesktop.browse(new URI(a.url))
          }
          Right(MediaPlaybackResponse(a, MediaPlaybackByRedirect))
        case Left(e) => Left[AppError, MediaPlaybackResponse](e)
      }

    //    Right(MediaPlaybackResponse(Media(, "taiso 1 & 2", 650L, 120L, YoutubeUtil.taisoUrl, None), MediaPlaybackByRedirect)))
  }
}
