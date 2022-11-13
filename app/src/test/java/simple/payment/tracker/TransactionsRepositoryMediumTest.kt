package simple.payment.tracker

import ch.qos.logback.core.ConsoleAppender
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import simple.payment.tracker.logging.addAppender
import simple.payment.tracker.logging.hide
import simple.payment.tracker.logging.logback
import simple.payment.tracker.logging.patternLayoutEncoder

class TransactionsRepositoryMediumTest {
  private val firebase = FirebaseTestAccess()
  private val logger =
      logback {
            addAppender(ConsoleAppender()) { //
              encoder = patternLayoutEncoder("[%thread] - %msg%n")
            }
          }
          .hide()
          .createLogger("test")

  @Test
  fun `There are 4556 notifications`() =
      runBlocking<Unit> {
        val values: Map<Long, Notification> =
            firebase.notifications().values.associateBy(Notification::time)
        assertThat(values).hasSize(4556)
      }

  @Test
  fun `TransactionsRepository removes duplicates, there are 2453 unique notifications`() =
      runBlocking<Unit> {
        val uniquePayments =
            TransactionsRepository.createForTest(
                    logger,
                    flowOf(firebase.notifications().values.toList()),
                    flowOf(emptyList()),
                    flowOf(emptyList()),
                )
                .transactions()
                .first()

        assertThat(uniquePayments).hasSize(2452)
      }

  @Test
  fun `TransactionsRepository detects automatic payments`() =
      runBlocking<Unit> {
        val autoPayments =
            TransactionsRepository.createForTest(
                    logger,
                    flowOf(firebase.notifications().values.toList()),
                    flowOf(emptyList()),
                    flowOf(firebase.automatic().values.toList()),
                )
                .transactions()
                .first()
                .filter { it is AutomaticPayment }

        assertThat(autoPayments).hasSize(950)

        autoPayments
            .groupBy { it.merchant }
            .values
            .sortedByDescending { it.size }
            .take(20)
            .forEach { println("Automatic: $it") }
      }

  @Test
  fun `TransactionsRepository removes assigned payments from the inbox`() =
      runBlocking<Unit> {
        val payments: List<PaymentRecord> = firebase.payments().values.toList()
        val notifications = firebase.notifications().values.toList()

        val inbox =
            TransactionsRepository.createForTest(
                    logger,
                    flowOf(notifications),
                    flowOf(payments),
                    flowOf(firebase.automatic().values.toList()),
                )
                .inbox
                .first()
                .associateBy { it.time }

        assertThat(inbox).hasSize(7)
      }
}
