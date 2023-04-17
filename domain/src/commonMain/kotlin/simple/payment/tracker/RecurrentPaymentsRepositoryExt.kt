package simple.payment.tracker

import java.time.Instant
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.flow.map

class RecurringPayments(private val repository: RecurringPaymentsRepository) {
  val payments =
      repository.payments.map { payments ->
        payments.flatMap { payment -> payment.generateSequence() }
      }
}

fun RecurringPaymentRecord.generateSequence(
    now: Calendar = Calendar.getInstance()
): Sequence<Payment> {
  val rec = this
  return generateSequence(dateFormat.parse(rec.start)) {
        now.apply {
              time = it
              add(Calendar.MONTH, 1)
            }
            .time
      }
      .takeWhile { it.before(rec.end?.let(dateFormat::parse) ?: Date.from(Instant.now())) }
      .mapIndexed { index, date ->
        RecurringPayment(
            category = rec.category,
            time = date.time + index, // add some millis to avoid duplicate dates
            sum = rec.sum,
            comment = rec.comment)
      }
}
