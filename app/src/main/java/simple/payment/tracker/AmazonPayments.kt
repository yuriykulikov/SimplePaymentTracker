package simple.payment.tracker

import dev.gitlive.firebase.database.FirebaseDatabase
import io.reactivex.Observable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asObservable
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
  private val amazonPayments: Observable<List<AmazonPayment>> =
      firebaseDatabase
          .reference("amazonpayments")
          .valueEvents
          .map { it.value<Map<String, AmazonPayment>>().values.toList() }
          .asObservable()
          .replay(1)
          .refCount()
          .onErrorResumeNext { e: Throwable ->
            logger.error(e) { "Amazon payments failed: $e" }
            Observable.just(emptyList<AmazonPayment>())
          }

  val payments: Observable<List<Payment>> = amazonPayments.map { it.map(AmazonPayment::toPayment) }
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
