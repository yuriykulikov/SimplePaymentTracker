package simple.payment.tracker.logging

interface LoggerFactory {
  fun createLogger(tag: String): Logger
}

class Logger(
    val slf4jLogger: org.slf4j.Logger,
) {
  inline fun trace(supplier: () -> String) {
    if (slf4jLogger.isTraceEnabled) {
      slf4jLogger.trace(supplier())
    }
  }

  inline fun debug(supplier: () -> String) {
    if (slf4jLogger.isDebugEnabled) {
      slf4jLogger.debug(supplier())
    }
  }

  inline fun warning(supplier: () -> String) {
    if (slf4jLogger.isWarnEnabled) {
      slf4jLogger.warn(supplier())
    }
  }

  inline fun info(supplier: () -> String) {
    if (slf4jLogger.isInfoEnabled) {
      slf4jLogger.info(supplier())
    }
  }

  inline fun error(e: Throwable? = null, supplier: () -> String) {
    if (slf4jLogger.isErrorEnabled) {
      slf4jLogger.error(supplier(), e)
    }
  }
}
