package simple.payment.tracker.firebase

import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import simple.payment.tracker.PaymentMatcher
import simple.payment.tracker.PaymentMatcherRepository

class FirebaseAdminPaymentMatcherRepository(firebaseDatabase: FirebaseDatabase) :
    PaymentMatcherRepository {
  private val scope = CoroutineScope(Dispatchers.Unconfined)
  private val matchers =
      firebaseDatabase
          .reference("automatic")
          .valueEvents
          .map { it.value<Map<String, PaymentMatcher>>().values.toList() }
          .shareIn(scope, SharingStarted.WhileSubscribed(250), 1)

  override fun matchers(): Flow<List<PaymentMatcher>> {
    return matchers
  }
}
