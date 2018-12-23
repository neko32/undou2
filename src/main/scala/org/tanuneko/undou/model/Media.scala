package org.tanuneko.undou.model

final case class Media(mediaId: Int, name: String, length: Long, countPlayed: Long, url: String, altMediaId: Option[Int]) {

  def incr: Media = Media(mediaId, name, length, countPlayed + 1, url, altMediaId)

}


