package simple.payment.tracker

import io.reactivex.Observable

class PaymentsRepository(private val logger: Logger, private val firebaseDatabase: Firebase) {
  private val paymentsRef = firebaseDatabase.child("payments") { Payment.fromMap(it) }
  private val payments: Observable<List<Payment>> =
      paymentsRef
          .observe()
          .map { it.values.toList() }
          .map { list ->
            list.map {
              if (it.category == "Гедонизм") {
                it.copy(category = "Еда")
              } else {
                it
              }
            }
          }
          .replay(1)
          .refCount()

  fun payments(): Observable<List<Payment>> = this.payments

  fun changeOrCreatePayment(previousId: Long?, payment: Payment) {
    logger.debug { "Adding payment: $payment" }

    paymentsRef.put(payment.id.toString(), payment)
    if (previousId != null && payment.id != previousId) {
      logger.debug { "Removing the old one: $previousId" }
      paymentsRef.remove(previousId.toString())
    }
  }
}
