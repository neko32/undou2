package org.tanuneko.undou.route

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.model.StatusCode._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.tanuneko.undou.service.MediaPlaybackService
import org.tanuneko.undou.util.JsonResponseHandling
import org.tanuneko.undou.marshaller._
import org.tanuneko.undou.model.{MediaPlaybackByRedirect, MediaPlaybackSuccess}
import spray.json._

import scala.util.{Failure, Success}

class RestRouter(mediaPlaybackService: MediaPlaybackService) extends JsonResponseHandling {

  import MediaFormatter._
  import MediaPlaybackResponseFormatter._

  def route: Route = mediaRoute

  def mediaRoute = {
    pathPrefix("play") {
      get {
        parameters('mediaId.as[Int]) { mediaId =>
          onComplete(mediaPlaybackService.play(mediaId)) {
            case Success(rez) => rez match {
              case Right(resp) =>
                resp.mediaPlaybackStatus match {
                  case MediaPlaybackSuccess => asJsonResponse(StatusCode.int2StatusCode (200), resp.toJson.prettyPrint)
                  case MediaPlaybackByRedirect =>
                    redirect(resp.media.url, StatusCodes.TemporaryRedirect)
                }
              case Left(e) => asJsonResponse(StatusCode.int2StatusCode(500), null)
            }
            case Failure(e) => asJsonResponse(StatusCode.int2StatusCode(500), null)
          }
        }
      }
    }
  }

}
