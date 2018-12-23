package org.tanuneko.undou.util

import java.time.{Instant, LocalDateTime, ZoneId}

object TimeUtil {

  def isAfter(time: Int, date: Instant): Boolean = {
    val ldt = LocalDateTime.ofInstant(date, ZoneId.systemDefault)
    ldt.getHour >= time
  }

}
