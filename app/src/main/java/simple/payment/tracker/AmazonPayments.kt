package simple.payment.tracker

import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.Serializable
import simple.payment.tracker.logging.Logger

@Serializable
data class AmazonPayment(
    val orderId: String,
    val category: String,
    val time: Long,
    val comment: String,
    val sum: Int
)

class AmazonPaymentsRepository(private val firebaseDatabase: FirebaseDatabase, logger: Logger) {
  private val shareScope = CoroutineScope(Dispatchers.Unconfined)
  private val amazonPayments: Flow<List<AmazonPayment>> =
      firebaseDatabase
          .reference("amazonpayments")
          .valueEvents
          .map { it.value<Map<String, AmazonPayment>>().values.toList() }
          .catch { e ->
            logger.error(e) { "Amazon payments failed: $e" }
            flowOf(emptyList<AmazonPayment>())
          }
          .shareIn(shareScope, SharingStarted.WhileSubscribed(250), 1)

  val payments: Flow<List<Payment>> = amazonPayments.map { it.map(AmazonPayment::toPayment) }
}

fun AmazonPayment.toPayment(): Payment {
  return Payment(
      category = category,
      trip = null,
      time = time,
      cancelled = false,
      sum = sum,
      merchant = "Amazon",
      notificationId = null,
      comment = comment)
}
