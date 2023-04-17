package simple.payment.tracker.logging

interface LoggerFactory {
  fun createLogger(tag: String): Logger
}

enum class Level {
  TRACE,
  DEBUG,
  INFO,
  WARNING,
  ERROR,
}

class Logger(
    val levelEnabled: (Level) -> Boolean,
    val log: (Level, Throwable?, String) -> Unit,
) {
  inline fun trace(supplier: () -> String) {
    if (levelEnabled(Level.TRACE)) {
      log(Level.TRACE, null, supplier())
    }
  }

  inline fun debug(supplier: () -> String) {
    if (levelEnabled(Level.DEBUG)) {
      log(Level.DEBUG, null, supplier())
    }
  }

  inline fun info(supplier: () -> String) {
    if (levelEnabled(Level.INFO)) {
      log(Level.INFO, null, supplier())
    }
  }

  inline fun warning(e: Throwable? = null, supplier: () -> String) {
    if (levelEnabled(Level.WARNING)) {
      log(Level.WARNING, e, supplier())
    }
  }
  inline fun error(e: Throwable? = null, supplier: () -> String) {
    if (levelEnabled(Level.ERROR)) {
      log(Level.ERROR, e, supplier())
    }
  }
}
