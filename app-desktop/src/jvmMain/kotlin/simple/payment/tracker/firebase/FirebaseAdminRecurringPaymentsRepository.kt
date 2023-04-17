package simple.payment.tracker.firebase

import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import simple.payment.tracker.RecurringPaymentRecord
import simple.payment.tracker.RecurringPaymentsRepository
import simple.payment.tracker.logging.Logger

class FirebaseAdminRecurringPaymentsRepository(logger: Logger, firebaseDatabase: FirebaseDatabase) :
    RecurringPaymentsRepository {
  private val shareScope = CoroutineScope(Dispatchers.Unconfined)

  override val payments: Flow<List<RecurringPaymentRecord>> =
      firebaseDatabase
          .reference("recurringpayments")
          .valueEvents
          .map { it.value<Map<String, RecurringPaymentRecord>>().values.toList() }
          .shareIn(shareScope, SharingStarted.WhileSubscribed(250), 1)
}
