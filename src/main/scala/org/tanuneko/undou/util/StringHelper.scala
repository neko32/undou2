package org.tanuneko.undou.util

trait StringHelper {
  implicit class StringHelper(s: String) {
    def trimDoubleQuote = s.replaceAll("\"", "")
  }
}
