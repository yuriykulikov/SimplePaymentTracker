package simple.payment.tracker

import ch.qos.logback.core.ConsoleAppender
import io.reactivex.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import simple.payment.tracker.logging.addAppender
import simple.payment.tracker.logging.hide
import simple.payment.tracker.logging.logback
import simple.payment.tracker.logging.patternLayoutEncoder

class TransactionsRepositoryTest {

  private val loggerFactory =
      logback {
            addAppender(ConsoleAppender()) { //
              encoder = patternLayoutEncoder("[%thread] - %msg%n")
            }
          }
          .hide()

  @Test
  fun `unassigned notifications from two devices are shown as one transaction`() {
    val aggregated =
        TransactionsRepository.createForTest(
                loggerFactory.createLogger("test"),
                Observable.just(
                    listOf(
                        Notification(
                            time = 1600267655959,
                            device = "Pixel 3a",
                            text = "You paid 17.00 EUR to GRUBIGSTEINBAHN IV"),
                        Notification(
                            time = 1600267718253,
                            device = "Pixel 4",
                            text = "You paid 17.00 EUR to GRUBIGSTEINBAHN IV",
                        ))),
                Observable.just(emptyList()),
                Observable.just(emptyList()),
            )
            .transactions()
            .blockingFirst()

    assertThat(aggregated).hasSize(1)
  }
}
