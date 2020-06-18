package simple.payment.tracker

import androidx.annotation.Keep
import com.squareup.moshi.Moshi
import io.reactivex.Observable
import simple.payment.tracker.stores.FileDataStore
import simple.payment.tracker.stores.Filer
import simple.payment.tracker.stores.listDataStore
import simple.payment.tracker.stores.modify

@Keep
data class Payment(
    val category: String,
    val notificationId: Long?,
    val time: Long?,
    val comment: String?,
    val merchant: String?,
    val sum: Int?,
    val id: Long = notificationId ?: time!!
) {
    companion object // functions below
}

class PaymentsRepository(
    private val logger: Logger,
    private val filer: Filer,
    private val moshi: Moshi
) {
    private val payments: FileDataStore<List<Payment>> = FileDataStore.listDataStore(
        filer,
        "payments.txt",
        "[]",
        moshi
    )

    init {
        payments.dump(logger)
    }

    fun payments(): Observable<List<Payment>> = payments.observe()

    fun addPayment(payment: Payment) {
        logger.debug { "Adding payment: $payment" }

        payments.modify {
            filterNot { it.id == payment.id }
                .plus(payment)
        }
    }

    fun changePayment(transaction: Transaction, category: String, comment: String?) {
        val newPayment = if (transaction.notificationId != null) {
            Payment.fromNotification(
                category,
                notificationId = transaction.notificationId,
                comment = comment
            )
        } else {
            Payment.manual(
                category = category,
                sum = transaction.sum,
                time = transaction.time,
                merchant = transaction.merchant,
                comment = comment
            )
        }
        addPayment(newPayment)
    }
}

fun Payment.Companion.fromNotification(
    category: String,
    notificationId: Long,
    sum: Int? = null,
    comment: String? = null
): Payment {
    return Payment(
        category = category,
        notificationId = notificationId,
        time = null,
        comment = comment,
        merchant = null,
        sum = sum
    )
}

fun Payment.Companion.manual(
    category: String,
    sum: Int,
    comment: String? = null,
    merchant: String?,
    time: Long?
): Payment {
    return Payment(
        category = category,
        notificationId = null,
        time = time,
        comment = comment,
        merchant = merchant,
        sum = sum
    )
}
