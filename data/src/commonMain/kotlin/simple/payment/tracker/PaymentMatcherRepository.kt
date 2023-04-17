package simple.payment.tracker

import kotlinx.coroutines.flow.Flow
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

interface PaymentMatcherRepository {
  fun matchers(): Flow<List<PaymentMatcher>>
}
