package simple.payment.tracker

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.moshi.Moshi
import io.reactivex.Observable
import simple.payment.tracker.stores.FileDataStore
import simple.payment.tracker.stores.Filer
import simple.payment.tracker.stores.listDataStore
import simple.payment.tracker.stores.modify

class PaymentsRepository(
    private val logger: Logger,
    private val filer: Filer,
    private val moshi: Moshi,
    private val firebaseDatabase: FirebaseDatabase
) {
    private val payments: FileDataStore<List<Payment>> = FileDataStore.listDataStore(
        filer,
        "payments.txt",
        "[]",
        moshi
    )

    private val firePayments: DatabaseReference = firebaseDatabase
        .reference
        .child("payments")

    private val fireSubject: Observable<List<Payment>>

    init {
        fireSubject = firePayments.observe { Payment.fromMap(it) }
            .map { it.values.toList() }
            .replay(1)
            .refCount()
    }

    fun payments(): Observable<List<Payment>> = fireSubject

    fun addPayment(payment: Payment) {
        logger.debug { "Adding payment: $payment" }

        payments.modify {
            filterNot { it.id == payment.id }
                .plus(payment)
        }

        firePayments.child(payment.id.toString()).setValue(payment)
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
