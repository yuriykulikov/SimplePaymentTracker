package simple.payment.tracker

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import java.util.concurrent.TimeUnit

data class Transaction(
  val payment: Payment? = null,
  val notification: Notification? = null
) {
  val id: Long = payment?.id ?: requireNotNull(notification).time
  val merchant: String = payment?.merchant ?: requireNotNull(notification).merchant()
  val sum: Int = payment?.sum ?: requireNotNull(notification).sum()
  val comment: String = payment?.comment ?: ""
  val cancelled: Boolean = payment?.cancelled ?: false
  val time: Long = payment?.time ?: requireNotNull(notification).time
  val category: String = payment?.category ?: ""
  val trip: String? = payment?.trip
}

/**
 * Aggregates notifications and payments into one flat list ready for presentation.
 */
class ListAggregator(
  private val paymentsRepository: PaymentsRepository,
  private val notificationsRepository: NotificationsRepository,
  private val logger: Logger
) {
  private val transactions: Observable<List<Transaction>> by lazy {
    val notificationsById = notificationsRepository.notifications()
      .map { it.associateBy(Notification::time) }
      .distinctUntilChanged()

    Observables.combineLatest(
      paymentsRepository.payments(),
      notificationsById
    ) { payments: List<Payment>, notifications: Map<Long, Notification> ->
      aggregate(payments, notifications)
    }
      .replay(1)
      .refCount(15, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
  }

  fun transactions(): Observable<List<Transaction>> {
    return transactions
  }

  private fun aggregate(
    payments: List<Payment>,
    notifications: Map<Long, Notification>
  ): List<Transaction> {
    val confirmedTransactions = payments.map { Transaction(payment = it) }

    val confirmedIds = payments.mapNotNull { it.notificationId }.toSet()

    val unconfirmed = notifications.values.filter { it.time !in confirmedIds }
      .filter { notification -> "You received" !in notification.text }
      // TODO somehow use it
      .filter { notification -> "Partial refund" !in notification.text }
      .map { Transaction(notification = it) }

    return (confirmedTransactions + unconfirmed).sortedByDescending { it.time }
  }
}

// You saved 1,36 EUR on a 34,10 EUR
fun Notification.sum(): Int {
  val sum = text
    .substringAfter("You paid ")
    .substringAfter("You sent ")
    .substringAfter(" on a ")
    .substringBefore(",")
  val youSaved: Int = when {
    "You saved " in text -> text.substringAfter("You saved ").substringBefore(",").toInt()
    else -> 0
  }
  return when {
    sum.startsWith("$") -> sum.removePrefix("$").toInt() * 10 / 9
    text.contains("RUB to") -> sum.replace(".", "").toInt() / 80
    else -> sum.toInt()
  } - youSaved
}

fun Notification.merchant(): String {
  return text
    .substringAfterLast(" EUR to ")
    .substringAfterLast(" EUR to ")
    .substringAfterLast(" USD to ")
    .substringAfterLast(" USD to ")
    .substringAfterLast(" RUB to ")
    .substringAfterLast(" RUB to ")
    .substringAfterLast("purchase at ")
}
