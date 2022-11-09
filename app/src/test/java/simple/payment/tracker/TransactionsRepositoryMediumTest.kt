package simple.payment.tracker

import ch.qos.logback.core.ConsoleAppender
import io.reactivex.Observable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx2.rxObservable
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
                    rxObservable { trySend(firebase.notifications().values.toList()) },
                    Observable.just(emptyList()),
                    Observable.just(emptyList()),
                )
                .transactions()
                .blockingFirst()

        assertThat(uniquePayments).hasSize(2452)
      }

  @Test
  fun `TransactionsRepository detects automatic payments`() {
    val autoPayments =
        TransactionsRepository.createForTest(
                logger,
                rxObservable { trySend(firebase.notifications().values.toList()) },
                Observable.just(emptyList()),
                rxObservable { trySend(firebase.automatic().values.toList()) },
            )
            .transactions()
            .blockingFirst()
            .filter { it.payment?.auto == true }

    assertThat(autoPayments).hasSize(950)

    autoPayments
        .groupBy { it.payment?.merchant }
        .values
        .sortedByDescending { it.size }
        .take(20)
        .forEach { println("Automatic: $it") }
  }

  @Test
  fun `TransactionsRepository removes assigned payments from the inbox`() =
      runBlocking<Unit> {
        val payments: List<Payment> = firebase.payments().values.toList()
        val notifications = firebase.notifications().values.toList()

        val inbox =
            TransactionsRepository.createForTest(
                    logger,
                    Observable.just(notifications),
                    Observable.just(payments),
                    rxObservable { trySend(firebase.automatic().values.toList()) },
                )
                .inbox
                .blockingFirst()
                .associateBy { it.time }

        assertThat(inbox).hasSize(7)
      }
}
