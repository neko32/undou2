package org.tanuneko.undou.marshaller

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.tanuneko.undou.model.Media
import spray.json.DefaultJsonProtocol

trait MediaFormatter extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val mediaFormatter = jsonFormat6(Media.apply)
}

object MediaFormatter extends MediaFormatter

