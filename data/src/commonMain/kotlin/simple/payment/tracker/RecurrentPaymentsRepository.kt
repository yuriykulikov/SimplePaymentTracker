package simple.payment.tracker

import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY)

@Serializable
data class RecurringPaymentRecord(
    val comment: String,
    val category: String,
    val sum: Int,
    val start: String,
    val end: String? = null
)

interface RecurringPaymentsRepository {
  val payments: Flow<List<RecurringPaymentRecord>>
}
