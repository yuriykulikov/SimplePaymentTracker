package simple.payment.tracker

import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class TinkoffPaymentsRepository(private val firebaseDatabase: FirebaseDatabase) {
  val payments: Flow<List<Payment>> =
      firebaseDatabase
          .reference("tinkoffpayments")
          .valueEvents
          .map {
            it.value<Map<String, PaymentRecord>>().values.toList().map {
              ManualPayment(payment = it)
            }
          }
          .shareIn(CoroutineScope(Dispatchers.Unconfined), SharingStarted.WhileSubscribed(250), 1)
}
