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
    override val category: String,
    override val time: Long,
    override val comment: String,
    override val sum: Int,
    override val user: String?,
) : Payment() {
  override val merchant: String = "Amazon"
  override val trip: String? = null
}

class AmazonPaymentsRepository(private val firebaseDatabase: FirebaseDatabase, logger: Logger) {
  private val shareScope = CoroutineScope(Dispatchers.Unconfined)

  val payments: Flow<List<Payment>> =
      firebaseDatabase
          .reference("amazonpayments")
          .valueEvents
          .map { it.value<Map<String, AmazonPayment>>().values.toList() }
          .catch { e ->
            logger.error(e) { "Amazon payments failed: $e" }
            flowOf<List<AmazonPayment>>(emptyList())
          }
          .shareIn(shareScope, SharingStarted.WhileSubscribed(250), 1)
}
