package simple.payment.tracker

import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import simple.payment.tracker.logging.Logger

class PaymentsRepository(
    private val logger: Logger,
    private val firebaseDatabase: FirebaseDatabase,
    private val signedInUserEmail: StateFlow<String?>,
) {
  private val paymentsRef = firebaseDatabase.reference("payments")
  private val payments: Flow<List<Payment>> =
      paymentsRef.valueEvents.map { it.value<Map<String, Payment>>().values.toList() }

  fun payments(): Flow<List<Payment>> = this.payments

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
