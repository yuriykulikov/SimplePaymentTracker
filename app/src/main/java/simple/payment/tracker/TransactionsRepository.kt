package simple.payment.tracker

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import simple.payment.tracker.logging.Logger

data class Transaction(val payment: Payment? = null, val notification: Notification? = null) {
  val id: Long = payment?.id ?: requireNotNull(notification).time
  val merchant: String = payment?.merchant ?: requireNotNull(notification).merchant()
  val sum: Int = payment?.sum ?: requireNotNull(notification).sum()
  val comment: String = payment?.comment ?: ""
  val cancelled: Boolean = payment?.cancelled ?: false
  val time: Long = payment?.time ?: requireNotNull(notification).time
  val category: String = payment?.category ?: ""
  val trip: String? = payment?.trip
}

/** Aggregates notifications and payments into one flat list ready for presentation. */
class TransactionsRepository(
    private val logger: Logger,
    private val paymentsRepository: PaymentsRepository,
    private val notificationsRepository: NotificationsRepository,
    private val automaticPaymentsRepository: AutomaticPaymentsRepository,
) {
  private val transactions: Observable<List<Transaction>> by lazy {
    val notificationsById =
        notificationsRepository
            .notifications()
            .map { it.associateBy(Notification::time) }
            .distinctUntilChanged()

    Observables.combineLatest(
            paymentsRepository.payments(),
            notificationsById,
            automaticPaymentsRepository.matchers(),
        ) {
            payments: List<Payment>,
            notifications: Map<Long, Notification>,
            matchers: List<PaymentMatcher>,
          ->
          aggregate(payments, notifications, matchers)
        }
        .replay(1)
        .refCount(45, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
  }

  fun transactions(): Observable<List<Transaction>> {
    return transactions
  }
}

fun aggregate(
    payments: List<Payment>,
    notifications: Map<Long, Notification>,
    matchers: List<PaymentMatcher> = emptyList(),
): List<Transaction> {
  val start = System.nanoTime()
  // 1. link notifications
  // 2. filter unconfirmed notifications
  // 3. show notifications without duplicates

  val notificationsWithoutPayment: List<Notification> =
      notificationsWithoutExplicitPayment(notifications, payments.mapNotNull { it.notificationId })

  val automaticPayments = findAutomatic(notificationsWithoutPayment, matchers)

  val inbox =
      notificationsWithoutPayment.minus(automaticPayments.mapNotNull { it.notification }).map {
        Transaction(notification = it)
      }

  return listOf(
      confirmedTransactions(payments),
      inbox,
      automaticPayments,
  )
      .flatten()
      .sortedByDescending { it.time }
      .also { println("aggregate took ${(System.nanoTime() - start) / 1000000}ms") }
}

/** Finds notifications without explicit payments and removes duplicates */
private fun notificationsWithoutExplicitPayment(
    notifications: Map<Long, Notification>,
    idsFromPayments: List<Long>
): List<Notification> {
  val validNotifications: Map<Long, Notification> =
      notifications
          .filterValues { notification -> "You received" !in notification.text }
          .filterValues { notification -> "Partial refund" !in notification.text }
          .filterValues { notification -> !notification.text.startsWith("Are you trying") }
          .filterValues { notification -> notification.sum() != 0 }

  // N^2
  val idToGroup: Map<Long, Set<Long>> =
      validNotifications
          .values
          .map { notification ->
            val duplicates =
                validNotifications.values.filter { other ->
                  notification.text == other.text &&
                      notification.device != other.device &&
                      abs(notification.time - other.time) < 3 * 60 * 60 * 1000
                }
            notification.time to duplicates.map { it.time }.plus(notification.time).toSet()
          }
          .toMap()

  val duplicates = idsFromPayments.mapNotNull { idToGroup[it] }.flatten()

  return idToGroup.minus(idsFromPayments).minus(duplicates).values.distinct().map {
    notifications.getValue(it.first())
  }
}

private fun confirmedTransactions(payments: List<Payment>): List<Transaction> {
  return payments.map { Transaction(payment = it) }
}

/** Run all [PaymentMatcher]s against notifications to find all automatically detectable */
fun findAutomatic(
    validNotifications: List<Notification>,
    matchers: List<PaymentMatcher>
): List<Transaction> {
  return validNotifications.mapNotNull { notification ->
    matchers.firstOrNull { it.matches(notification) }?.convert(notification)
  }
}

// You saved 1,36 EUR on a 34,10 EUR
fun Notification.sum(): Int {
  return runCatching {
    // You payed 1,34 EUR to Some Guy"
    val sum =
        text.substringAfter("You paid ")
            .substringAfter("You sent ")
            .substringAfter(" on a ")
            .substringBefore(",")
            .substringBefore(".")
    val youSaved: Int =
        when {
          "You saved " in text -> text.substringAfter("You saved ").substringBefore(",").toInt()
          else -> 0
        }
    when {
      sum.startsWith("$") -> sum.removePrefix("$").toInt() * 10 / 9
      text.contains("RUB to") -> sum.replace(".", "").toInt() / 80
      else -> sum.toIntOrNull() ?: 0
    } - youSaved
  }
      .getOrElse {
        throw IllegalArgumentException("Failed to parse sum of $this, caused by $it", it)
      }
}

fun Notification.merchant(): String {
  return text.substringAfterLast(" EUR to ")
      .substringAfterLast(" EUR to ")
      .substringAfterLast(" USD to ")
      .substringAfterLast(" USD to ")
      .substringAfterLast(" RUB to ")
      .substringAfterLast(" RUB to ")
      .substringAfterLast("purchase at ")
}
