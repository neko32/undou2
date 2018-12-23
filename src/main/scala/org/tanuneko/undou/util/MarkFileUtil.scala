package org.tanuneko.undou.util

import java.io.{File, PrintWriter}

object MarkFileUtil {

  def markFile = System.getProperty("java.io.tmpdir") + "/mark.txt"
  def markStr = "pass"
  def nonMarkStr = "nopass"

  def markFileExists: Boolean = {
    new File(markFile).exists
  }

  def create: Unit = {
    val pw = new PrintWriter(new File(markFile))
    pw.write(nonMarkStr)
    pw.close
  }

  def mark: Unit = {
    val pw = new PrintWriter(new File(markFile))
    pw.write(markStr)
    pw.close
  }

  def unmark: Unit = {
    val pw = new PrintWriter(new File(markFile))
    pw.write(nonMarkStr)
    pw.close
  }
}
