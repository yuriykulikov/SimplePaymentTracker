package simple.payment.tracker

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import java.util.concurrent.TimeUnit
import kotlin.math.abs

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
class TransactionsRepository(
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
      .refCount(45, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
  }

  fun transactions(): Observable<List<Transaction>> {
    return transactions
  }
}

fun aggregate(
  payments: List<Payment>,
  notifications: Map<Long, Notification>
): List<Transaction> {
  val start = System.nanoTime()
  // 1. link notifications
  // 2. filter unconfirmed notifications
  // 3. show notifications without duplicates

  val validNotifications: Map<Long, Notification> = notifications
    .filterValues { notification -> "You received" !in notification.text }
    .filterValues { notification -> "Partial refund" !in notification.text }

  // N^2
  val idToGroup: Map<Long, Set<Long>> = validNotifications.values.map { notification ->
    val duplicates = validNotifications.values.filter { other ->
      notification.text == other.text
        && notification.device != other.device
        && abs(notification.time - other.time) < 3 * 60 * 60 * 1000
    }
    notification.time to duplicates.map { it.time }.plus(notification.time).toSet()
  }
    .toMap()

  val confirmedIds = payments.mapNotNull { it.notificationId }
  val duplicates = confirmedIds.mapNotNull { idToGroup[it] }.flatten()
  val allConfirmed = (confirmedIds + duplicates).toSet()

  val unconfirmed = idToGroup.minus(allConfirmed)
    .values
    .distinct()
    .map { notifications.getValue(it.first()) }
    .map { Transaction(notification = it) }

  val confirmedTransactions = payments.map { Transaction(payment = it) }
  return (confirmedTransactions + unconfirmed).sortedByDescending { it.time }
    .also {
      println("aggregate took ${(System.nanoTime() - start) / 1000000}ms")
    }
}

// You saved 1,36 EUR on a 34,10 EUR
fun Notification.sum(): Int {
  return runCatching {
    val sum = text
      .substringAfter("You paid ")
      .substringAfter("You sent ")
      .substringAfter(" on a ")
      .substringBefore(",")
      .substringBefore(".")
    val youSaved: Int = when {
      "You saved " in text -> text.substringAfter("You saved ").substringBefore(",").toInt()
      else -> 0
    }
    when {
      sum.startsWith("$") -> sum.removePrefix("$").toInt() * 10 / 9
      text.contains("RUB to") -> sum.replace(".", "").toInt() / 80
      else -> sum.toInt()
    } - youSaved
  }.getOrElse {
    throw IllegalArgumentException("Failed to parse sum of $this, caused by $it", it)
  }
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
