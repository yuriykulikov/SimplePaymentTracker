package simple.payment.tracker.firebase

import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import simple.payment.tracker.AmazonPayment
import simple.payment.tracker.AmazonPaymentsRepository
import simple.payment.tracker.logging.Logger

class FirebaseAdminAmazonPaymentsRepository(logger: Logger, firebaseDatabase: FirebaseDatabase) :
    AmazonPaymentsRepository {
  private val shareScope = CoroutineScope(Dispatchers.Unconfined)

  override val payments: Flow<List<AmazonPayment>> =
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
