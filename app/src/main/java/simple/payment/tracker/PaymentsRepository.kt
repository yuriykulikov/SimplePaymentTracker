package simple.payment.tracker

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
    private val firebaseDatabase: Firebase
) {
  private val paymentsStore: FileDataStore<List<Payment>> =
      FileDataStore.listDataStore(filer, "payments.txt", "[]", moshi)

  private val paymentsRef = firebaseDatabase.child("payments") { Payment.fromMap(it) }
  private val payments: Observable<List<Payment>> =
      paymentsRef.observe().map { it.values.toList() }.replay(1).refCount()

  fun payments(): Observable<List<Payment>> = this.payments

  fun changeOrCreatePayment(previousId: Long?, payment: Payment) {
    logger.debug { "Adding payment: $payment" }

    this.paymentsStore.modify {
      filterNot { it.id == payment.id }
      filterNot { it.id == previousId }.plus(payment)
    }

    paymentsRef.put(payment.id.toString(), payment)
    if (previousId != null && payment.id != previousId) {
      logger.debug { "Removing the old one: $previousId" }
      paymentsRef.remove(previousId.toString())
    }
  }
}
