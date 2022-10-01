package simple.payment.tracker.logging

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.spi.ContextAware
import org.slf4j.ILoggerFactory

class LogbackLoggerFactory(private val slf4j: org.slf4j.ILoggerFactory) : LoggerFactory {
  override fun createLogger(tag: String): Logger {
    return Logger(slf4j.getLogger(tag))
  }
}

fun ILoggerFactory.hide(): LoggerFactory {
  return LogbackLoggerFactory(this)
}

/** Logback config DSL entry point. */
fun logback(config: LoggerContext.() -> Unit): ILoggerFactory {
  // reset the default context (which may already have been initialized)
  // since we want to reconfigure it
  val context = org.slf4j.LoggerFactory.getILoggerFactory() as LoggerContext
  context.stop()
  config(context)
  return context
}

/**
 * Configures and adds an appender to the [LoggerContext]. Can be wrapped in [AsyncAppender] if
 * [async] is set to `true`.
 */
fun <T : Appender<ILoggingEvent>> LoggerContext.addAppender(
    appender: T,
    async: Boolean = false,
    config: T.() -> Unit
) {
  appender.context = this
  config(appender)
  appender.start()

  val root =
      org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
          as ch.qos.logback.classic.Logger

  root.addAppender(
      when {
        async -> {
          AsyncAppender().apply {
            context = this@addAppender
            addAppender(appender)
            start()
          }
        }
        else -> appender
      })
}

/**
 * Creates and configures a [TimeBasedRollingPolicy].
 *
 * ## Example
 * ```
 * rollingPolicy = timeBasedRollingPolicy {
 *   fileNamePattern = "$logDir/rolling-%d{yyyy-MM-dd}.log"
 *   maxHistory = 7
 *   isCleanHistoryOnStart = true
 * }
 * ```
 */
fun RollingFileAppender<ILoggingEvent>.timeBasedRollingPolicy(
    config: TimeBasedRollingPolicy<ILoggingEvent>.() -> Unit
): TimeBasedRollingPolicy<ILoggingEvent> {
  val parent = this
  return TimeBasedRollingPolicy<ILoggingEvent>().apply {
    context = parent.context
    setParent(parent)
    config(this)
    start()
  }
}

/**
 * Creates a [PatternLayoutEncoder].
 *
 * See
 * [Logback ClassicPatternLayout](http://logback.qos.ch/manual/layouts.html#ClassicPatternLayout)
 * for details.
 */
fun ContextAware.patternLayoutEncoder(template: String): PatternLayoutEncoder {
  return PatternLayoutEncoder().apply {
    context = this@patternLayoutEncoder.context
    pattern = template
    start()
  }
}
