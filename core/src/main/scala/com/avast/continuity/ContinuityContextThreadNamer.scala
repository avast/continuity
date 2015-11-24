package com.avast.continuity

import java.text.ParseException

import com.avast.continuity.ContinuityContextThreadNamer.{Delimiter, ThreadNamePlaceholder, UnknownKeyPlaceholder}

import scala.annotation.tailrec

/** Names threads according to a given format using values from the Continuity context.
  *
  * The format can contain special placeholders delimited by '%' which will be filled in by values from the context.
  * Placeholder %thread% is replaced by the current thread name.
  *
  * Example:
  * '''%myKey%-%thread%'''
  * The thread will be named with the value of 'myKey' from the context and the current thread name.
  */
class ContinuityContextThreadNamer(format: String) extends ThreadNamer with Continuity {

  override protected def name = {
    val threadName = Thread.currentThread.getName
    val parser = new Parser(threadName)
    val newThreadName = parser.parse()

    if (newThreadName.isEmpty) {
      threadName
    } else {
      newThreadName
    }
  }

  private class Parser(currentThreadName: String) {

    private var index = 0

    def parse(): String = {
      val builder = new StringBuilder

      @tailrec
      def loop(): Unit = {
        nextChar match {
          case Some(char) if char == Delimiter =>
            parseContextKey() match {
              case ThreadNamePlaceholder => builder ++= currentThreadName
              case key => builder ++= getFromContext(key).getOrElse(UnknownKeyPlaceholder)
            }
            loop()

          case Some(char) =>
            builder += char
            loop()

          case None => // ignore
        }
      }

      loop()

      builder.toString
    }

    private def nextChar: Option[Char] = if (index < format.length) {
      val char = format(index)
      index += 1
      Some(char)
    } else {
      None
    }

    private def parseContextKey(): String = {
      val key = new StringBuilder

      @tailrec
      def loop(): Unit = {
        nextChar match {
          case Some(char) if char != Delimiter =>
            key += char
            loop()

          case Some(Delimiter) => // break
          case None => throw new ParseException("Unexpected end of string", index)
        }
      }

      loop()

      key.toString
    }

  }

}

object ContinuityContextThreadNamer {

  private final val Delimiter = '%'

  private final val ThreadNamePlaceholder = "thread"

  private final val UnknownKeyPlaceholder = "unknown"

  /** Creates a namer with the given format: '''%key%-%thread%''' */
  def prefix(key: String): ContinuityContextThreadNamer = new ContinuityContextThreadNamer(s"%$key%-%thread%")

}
