package simple.payment.tracker

import dev.gitlive.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.Serializable

val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY)

@Serializable
data class RecurrringPayment(
    val comment: String,
    val category: String,
    val sum: Int,
    val start: String,
    val end: String? = null
)

class RecurrentPaymentsRepository(private val firebaseDatabase: FirebaseDatabase) {
  private val scope = CoroutineScope(Dispatchers.Unconfined)

  private val recurring: Flow<List<RecurrringPayment>> =
      firebaseDatabase
          .reference("recurringpayments")
          .valueEvents
          .map { it.value<Map<String, RecurrringPayment>>().values.toList() }
          .shareIn(scope, SharingStarted.WhileSubscribed(250), 1)

  val payments: Flow<List<Payment>> = recurring.map { it.toPayments() }
}

fun List<RecurrringPayment>.toPayments(now: Calendar = Calendar.getInstance()): List<Payment> {
  return flatMap { rec ->
    generateSequence(dateFormat.parse(rec.start)) {
          now.apply {
                time = it
                add(Calendar.MONTH, 1)
              }
              .time
        }
        .takeWhile { it.before(rec.end?.let(dateFormat::parse) ?: Date.from(Instant.now())) }
        .mapIndexed { index, date ->
          Payment(
              category = rec.category,
              trip = null,
              time = date.time + index, // add some millis to avoid duplicate dates
              cancelled = false,
              sum = rec.sum,
              merchant = "Recurrent",
              notificationId = null,
              comment = rec.comment)
        }
  }
}
