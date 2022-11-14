package simple.payment.tracker.logging

import android.content.Context
import ch.qos.logback.core.rolling.RollingFileAppender
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.slf4j.ILoggerFactory

fun Scope.logger(tag: String): Logger {
  return get<LoggerFactory>().createLogger(tag)
}

/**
 * Creates a module which exports a [LoggerFactory] to create loggers. These [Logger]s are backed by
 * a [RollingFileAppender] and a [LogcatAppender].
 */
fun loggerModule() = module { single<LoggerFactory> { configureLogback(get()).hide() } }

private fun configureLogback(context: Context): ILoggerFactory {
  return logback {
    val logDir = context.filesDir.absolutePath

    addAppender(LogcatAppender()) { //
      encoder = patternLayoutEncoder("[%thread] - %msg%n")
    }

    addAppender(RollingFileAppender(), async = true) {
      isAppend = true
      rollingPolicy = timeBasedRollingPolicy {
        fileNamePattern = "$logDir/rolling-%d{yyyy-MM-dd}.log"
        maxHistory = 3
        isCleanHistoryOnStart = true
      }

      encoder =
          patternLayoutEncoder(
              "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n")
    }
  }
}
