package org.tanuneko.undou.tasks

import java.awt.Desktop
import java.net.URI
import java.time.Instant
import java.util.Calendar

import akka.actor.Actor
import com.typesafe.scalalogging.Logger
import org.slf4j.MarkerFactory
import org.tanuneko.undou.util.{MarkFileUtil, TimeUtil, YoutubeUtil}

import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.concurrent.duration._

class PlaybackActor extends Actor {

  private var prev = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
  private var todayPlayed = false

  private val logger = Logger[PlaybackActor]

  def receive = {
    case PLAYBACK =>
      var okToPlay = true
      val marker = MarkerFactory.getMarker("PLAYBACK")
      if(MarkFileUtil.markFileExists) {
        okToPlay = Source.fromFile(MarkFileUtil.markFile).getLines().mkString("") match {
          case s if s.toLowerCase == MarkFileUtil.markStr => false
          case _ => true
        }
      }
      val isAfter = TimeUtil.isAfter(22, Instant.now)
      if(okToPlay && !todayPlayed && isAfter) {
        logger.info(s"Playing - current time: ${Instant.now}")
        implicit val ctx: ExecutionContext = context.system.dispatcher
        Desktop.getDesktop.browse(new URI(YoutubeUtil.taisoUrl))
        context.system.scheduler.scheduleOnce(400 seconds) {
          Desktop.getDesktop.browse(new URI(YoutubeUtil.exerciseUrl))
        }
        todayPlayed = true
      } else {
        logPlaybackPassCase(okToPlay, todayPlayed, 22, isAfter)
      }

    case RESETMARKFILE =>
      val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
      val marker = MarkerFactory.getMarker("RESET")
      if(MarkFileUtil.markFileExists) {
        if(prev != today) {
          MarkFileUtil.unmark
          logger.info("Mark file has been reset")
          todayPlayed = false
        } else {
          logger.debug("same day..")
        }
      }
      prev = today
  }

  def logPlaybackPassCase(okToPlay: Boolean, todayPlayed: Boolean, hour: Int, afterTime: Boolean): Unit = {
    logger.info("Today' session will be skipped:" +
      s"OK To Play - ${okToPlay} " +
      s"Today Alraedy Played - ${todayPlayed} " +
      s"after ${hour} - ${afterTime}")
  }

}
