package org.tanuneko.undou.marshaller

import org.scalatest.{MustMatchers, WordSpec}
import org.tanuneko.undou.model.{Media, MediaNotFoundAtLocal, MediaPlaybackResponse, MediaPlaybackSuccess}
import org.tanuneko.undou.util.StringHelper
import spray.json._

class MediaPlaybackResponseFormatterSpec extends WordSpec
  with StringHelper
  with MustMatchers {

  import MediaPlaybackResponseFormatter._

  "MediaPlaybackResponseMarshaller" should {
    "Marshalls Scala Obj to Json" in {
      val respObj = MediaPlaybackResponse(Media(302, "taiso 1 & 2", 683434L, 12055L, "http://takora.net", None), MediaPlaybackSuccess)
      val json = respObj.toJson
      val fields = json.asJsObject.fields
      val mediaFields = fields("media").asJsObject.fields
      mediaFields("name").toString.trimDoubleQuote must equal("taiso 1 & 2")
      mediaFields("length").toString.trimDoubleQuote.toLong must equal(683434L)
      mediaFields("countPlayed").toString.trimDoubleQuote.toLong must equal(12055L)
      mediaFields("url").toString.trimDoubleQuote must equal("http://takora.net")
      mediaFields.get("altMediaId") must be(None)
      fields("mediaPlaybackStatus").toString.trimDoubleQuote must equal("MediaPlaybackSuccess")
    }

    "Unmarshalls Json to Scala Obj" in {
      val json =
        """
          |{
          | "media": {
          |   "mediaId": 350,
          |   "name": "takochans",
          |   "length": 5532423,
          |   "countPlayed": 1250028,
          |   "url": "http://tako.net",
          |   "altMediaId": 72
          | },
          | "mediaPlaybackStatus": "MediaNotFoundAtLocal"
          |}
        """.stripMargin.parseJson
      val resp = json.convertTo[MediaPlaybackResponse]
      resp.media.mediaId must equal(350)
      resp.media.name must equal("takochans")
      resp.media.length must equal(5532423L)
      resp.media.countPlayed must equal(1250028L)
      resp.media.url must equal("http://tako.net")
      resp.media.altMediaId must equal(Some(72))
      resp.mediaPlaybackStatus must equal(MediaNotFoundAtLocal)
    }
  }

}
