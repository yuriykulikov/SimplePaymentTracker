package simple.payment.tracker.koin

import org.koin.core.scope.Scope
import simple.payment.tracker.logging.Logger
import simple.payment.tracker.logging.LoggerFactory

fun Scope.logger(tag: String): Logger {
  return get<LoggerFactory>().createLogger(tag)
}
