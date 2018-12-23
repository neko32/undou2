package org.tanuneko.undou.marshaller

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.tanuneko.undou.model.{MediaNotFoundAtLocal, MediaPlaybackResponse, MediaPlaybackStatus, MediaPlaybackSuccess}
import org.tanuneko.undou.util.StringHelper
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

trait MediaPlaybackResponseFormatter extends SprayJsonSupport
  with StringHelper
  with DefaultJsonProtocol {

  import MediaFormatter._

  implicit val mediaPlaybackStatusFormat = new RootJsonFormat[MediaPlaybackStatus] {
    override def read(json: JsValue): MediaPlaybackStatus = {
      MediaPlaybackStatus.fromString(json.toString.trimDoubleQuote)
    }

    override def write(obj: MediaPlaybackStatus): JsValue = {
      obj match {
        case MediaPlaybackSuccess => JsString("MediaPlaybackSuccess")
        case MediaNotFoundAtLocal => JsString("MediaNotFoundAtLocal")
        case _ => JsString("MediaPlaybackUnknown")
      }
    }
  }

  implicit val mediaPlaybackRespFormat = jsonFormat2(MediaPlaybackResponse.apply)

}

object MediaPlaybackResponseFormatter extends MediaPlaybackResponseFormatter
