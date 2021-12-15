package simple.payment.tracker

import io.reactivex.Observable
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

  fun convert(notification: Notification): Transaction {
    return Transaction(
        Payment(
            category = category,
            time = notification.time,
            comment = comment ?: "",
            merchant = merchant,
            sum = notification.sum(),
            notificationId = notification.time,
            cancelled = false,
            trip = null,
            auto = true,
        ),
        notification)
  }
}

class AutomaticPaymentsRepository(private val firebaseDatabase: Firebase) {
  private val matchers =
      firebaseDatabase
          .child(
              "automatic",
              mapper = { map ->
                PaymentMatcher(
                    merchant = map["merchant"] as String,
                    category = map["category"] as String,
                )
              })
          .observe()
          .map { it.values.toList() }

  fun matchers(): Observable<List<PaymentMatcher>> {
    return matchers
  }
}
