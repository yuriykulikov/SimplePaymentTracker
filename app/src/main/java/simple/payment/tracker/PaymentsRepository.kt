package simple.payment.tracker

import dev.gitlive.firebase.database.FirebaseDatabase
import io.reactivex.Observable
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asObservable
import simple.payment.tracker.logging.Logger

class PaymentsRepository(
    private val logger: Logger,
    private val firebaseDatabase: FirebaseDatabase,
    private val signedInUserEmail: StateFlow<String?>,
) {
  private val paymentsRef = firebaseDatabase.reference("payments")
  private val payments: Observable<List<Payment>> =
      paymentsRef.valueEvents
          .map { it.value<Map<String, Payment>>().values.toList() }
          .asObservable()

  fun payments(): Observable<List<Payment>> = this.payments

  suspend fun changeOrCreatePayment(previousId: Long?, payment: Payment) {
    val paymentWithUserId =
        payment.takeIf { (it.user == null) }?.copy(user = signedInUserEmail.value) ?: payment
    logger.debug { "changeOrCreatePayment(previousId: $previousId, payment: $paymentWithUserId)" }

    paymentsRef.child(paymentWithUserId.id.toString()).setValue(paymentWithUserId)
    if (previousId != null && paymentWithUserId.id != previousId) {
      logger.debug { "Removing the old one: $previousId" }
      paymentsRef.child(previousId.toString()).removeValue()
    }
  }
}
