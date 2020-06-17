package simple.payment.tracker

import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables


data class Transaction(
    val confirmed: Boolean,
    val sum: Int,
    val merchant: String,
    val comment: String?,
    val category: String?,
    val time: Long,
    /** Referenced notification, if available */
    val notificationId: Long?,
    /** Referenced payment, if available */
    val paymentId: Long?,
    val cancelled: Boolean
)

/**
 * Aggregates notifications and payments into one flat list ready for presentation.
 */
class ListAggregator(
    private val paymentsRepository: PaymentsRepository,
    private val notificationsRepository: NotificationsRepository,
    private val logger: Logger
) {
    fun transactions(): Observable<List<Transaction>> {
        val notificationsById = notificationsRepository.notifications()
            .map { it.associateBy(Notification::time) }
            .distinctUntilChanged()

        return Observables.combineLatest(
            paymentsRepository.payments(),
            notificationsById
        ) { payments: List<Payment>, notifications: Map<Long, Notification> ->
            aggregate(payments, notifications)
        }
    }

    private fun aggregate(
        payments: List<Payment>,
        notifications: Map<Long, Notification>
    ): List<Transaction> {
        val confirmedTransactions = payments.mapNotNull { payment: Payment ->
            Transaction(
                confirmed = true,
                sum = payment.sum,
                merchant = payment.merchant,
                comment = payment.comment,
                category = payment.category,
                notificationId = payment.notificationId,
                paymentId = payment.id,
                time = payment.time,
                cancelled = payment.cancelled
            )
        }

        val confirmedIds = payments.mapNotNull { it.notificationId }.toSet()

        val unconfirmed = notifications.values.filter { it.time !in confirmedIds }
            .filter { notification -> "You received" !in notification.text }
            .map { notification ->
                Transaction(
                    confirmed = false,
                    sum = notification.sum(),
                    merchant = notification.merchant(),
                    comment = null,
                    category = null,
                    notificationId = notification.time,
                    paymentId = null,
                    time = notification.time,
                    cancelled = false
                )
            }
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
        else -> sum.toInt()
    } - youSaved
}

fun Notification.merchant(): String {
    return text
        .substringAfterLast(" EUR to ")
        .substringAfterLast(" EUR to ")
        .substringAfterLast(" USD to ")
        .substringAfterLast(" USD to ")
        .substringAfterLast("purchase at ")
}
