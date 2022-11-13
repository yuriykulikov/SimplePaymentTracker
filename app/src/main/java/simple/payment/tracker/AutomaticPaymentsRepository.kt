package simple.payment.tracker

import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.Serializable

@Serializable
data class PaymentMatcher(
    val merchant: String,
    val category: String,
    val comment: String? = null,
    val sum: Int? = null,
) {
  fun matches(notification: Notification): Boolean {
    return notification.merchant() == merchant && sum?.equals(notification.sum()) ?: true
  }

  fun convert(notification: Notification): AutomaticPayment {
    return AutomaticPayment(
        notification = notification,
        merchant = merchant,
        category = category,
        comment = comment ?: "",
    )
  }
}

class AutomaticPaymentsRepository(firebaseDatabase: FirebaseDatabase) {
  private val scope = CoroutineScope(Dispatchers.Unconfined)
  private val matchers =
      firebaseDatabase
          .reference("automatic")
          .valueEvents
          .map { it.value<Map<String, PaymentMatcher>>().values.toList() }
          .shareIn(scope, SharingStarted.WhileSubscribed(250), 1)

  fun matchers(): Flow<List<PaymentMatcher>> {
    return matchers
  }
}
