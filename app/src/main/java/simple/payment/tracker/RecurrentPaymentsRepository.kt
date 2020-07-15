package simple.payment.tracker

import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Observable
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

val dateFormat = SimpleDateFormat(
  "yyyy-MM-dd",
  Locale.GERMANY
)

data class RecurrringPayment(
  val comment: String,
  val category: String,
  val sum: Int,
  val start: String,
  val end: String? = null
) {
  companion object {
    fun fromMap(map: Map<String, Any>): RecurrringPayment {
      return RecurrringPayment(
        comment = map["comment"] as String? ?: "",
        category = map["category"] as String,
        sum = (map["sum"] as Long? ?: 0L).toInt(),
        start = map["start"] as String,
        end = map["end"] as String?
      )
    }
  }
}

class RecurrentPaymentsRepository(
  private val firebaseDatabase: FirebaseDatabase
) {
  private val recurring: Observable<List<RecurrringPayment>> = firebaseDatabase
    .reference
    .child("recurringpayments")
    .observe { map -> RecurrringPayment.fromMap(map) }
    .map { it.values.toList() }
    .replay(1)
    .refCount()

  val payments: Observable<List<Payment>> = recurring.map { it.toPayments() }
}

fun List<RecurrringPayment>.toPayments(
  now: Calendar = Calendar.getInstance()
): List<Payment> {
  return flatMap { rec ->
    generateSequence(dateFormat.parse(rec.start)) {
      now.apply {
        time = it
        add(Calendar.MONTH, 1)
      }.time
    }
      .takeWhile {
        it.before(
          rec.end?.let(dateFormat::parse) ?: Date.from(Instant.now())
        )
      }
      .mapIndexed { index, date ->
        Payment(
          category = rec.category,
          trip = null,
          time = date.time + index,// add some millis to avoid duplicate dates
          cancelled = false,
          sum = rec.sum,
          merchant = "Recurrent",
          notificationId = null,
          comment = rec.comment
        )
      }
      .toList()
  }
}
