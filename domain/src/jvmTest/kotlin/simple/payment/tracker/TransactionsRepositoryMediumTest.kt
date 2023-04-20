package simple.payment.tracker

import ch.qos.logback.core.ConsoleAppender
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
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
        values shouldHaveSize 4556
      }

  @Test
  fun `TransactionsRepository removes duplicates, there are 2453 unique notifications`() =
      runBlocking<Unit> {
        val uniquePayments =
            Transactions.createForTest(
                    logger,
                    flowOf(firebase.notifications().values.toList()),
                    flowOf(emptyList()),
                    flowOf(emptyList()),
                )
                .transactions()
                .first()

        uniquePayments shouldHaveSize 2435
      }

  @Test
  fun `TransactionsRepository detects automatic payments`() =
      runBlocking<Unit> {
        val autoPayments =
            Transactions.createForTest(
                    logger,
                    flowOf(firebase.notifications().values.toList()),
                    flowOf(emptyList()),
                    flowOf(firebase.automatic().values.toList()),
                )
                .transactions()
                .first()
                .filter { it is AutomaticPayment }

        autoPayments shouldHaveSize 950

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
            Transactions.createForTest(
                    logger,
                    flowOf(notifications),
                    flowOf(payments),
                    flowOf(firebase.automatic().values.toList()),
                )
                .inbox
                .first()
                .associateBy { it.time }

        inbox shouldHaveSize 8
      }
}
