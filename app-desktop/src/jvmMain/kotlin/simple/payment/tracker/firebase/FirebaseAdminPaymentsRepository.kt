package simple.payment.tracker.firebase

import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import simple.payment.tracker.PaymentRecord
import simple.payment.tracker.PaymentsRepository
import simple.payment.tracker.logging.Logger

class FirebaseAdminPaymentsRepository(
    private val logger: Logger,
    firebaseDatabase: FirebaseDatabase,
    private val signedInUserEmail: StateFlow<String?>,
) : PaymentsRepository {
  private val paymentsRef = firebaseDatabase.reference("payments")
  private val payments: Flow<List<PaymentRecord>> =
      paymentsRef.valueEvents.map { it.value<Map<String, PaymentRecord>>().values.toList() }

  override fun payments(): Flow<List<PaymentRecord>> = this.payments

  override suspend fun changeOrCreatePayment(previousId: Long?, payment: PaymentRecord) {
    if (previousId != null) {
      check(payment.id == previousId) { "Assigned payment ID should not be changed!" }
    }
    val paymentWithUserId =
        payment.takeIf { (it.user == null) }?.copy(user = signedInUserEmail.value) ?: payment
    logger.debug { "changeOrCreatePayment(previousId: $previousId, payment: $paymentWithUserId)" }

    paymentsRef.child(paymentWithUserId.id.toString()).setValue(paymentWithUserId)
  }
}
