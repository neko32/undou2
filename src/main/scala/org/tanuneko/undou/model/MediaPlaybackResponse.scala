package org.tanuneko.undou.model

sealed trait MediaPlaybackStatus
case object MediaPlaybackSuccess extends MediaPlaybackStatus
case object MediaPlaybackByRedirect extends MediaPlaybackStatus
case object MediaNotFoundAtLocal extends MediaPlaybackStatus
case object MediaPlaybackUnknown extends MediaPlaybackStatus

object MediaPlaybackStatus {
  def fromString(str: String): MediaPlaybackStatus = {
    if(str == "MediaPlaybackSuccess") MediaPlaybackSuccess
    else if(str == "MediaNotFoundAtLocal") MediaNotFoundAtLocal
    else if(str == "MediaPlaybackByRedirect") MediaPlaybackByRedirect
    else MediaPlaybackUnknown
  }
}

sealed case class MediaPlaybackResponse(media: Media, mediaPlaybackStatus: MediaPlaybackStatus)



