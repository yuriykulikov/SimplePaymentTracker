package simple.payment.tracker

import java.time.Instant
import java.util.Calendar
import java.util.Date
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class MonthlyStatistics(private val payments: Flow<List<Payment>>) : GroupReportsProvider {
  private val scope = CoroutineScope(Dispatchers.Unconfined)
  private val reports =
      payments
          .map { monthly(it) }
          .shareIn(scope, SharingStarted.WhileSubscribed(1.seconds.inWholeMilliseconds), 1)

  override fun reports(): Flow<List<GroupReport>> {
    return reports
  }

  override fun report(name: String): Flow<GroupReport> {
    return reports.map { reports -> reports.first { it.name == name } }
  }

  companion object {
    fun monthly(merged: List<Payment>): List<GroupReport> {
      val monthly =
          merged
              .filter { it.trip.isNullOrEmpty() } //
              .groupBy {
                val cal = it.calendar()
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH).plus(1).toString()
                        .padStart(2, '0')}"
              }
      return monthly.entries
          .reversed()
          .map { (month, payments) -> GroupReport(month, payments) }
          .sortedByDescending { it.name }
    }
  }
}

private fun Payment.calendar(): Calendar {
  return Calendar.getInstance().apply { time = Date.from(Instant.ofEpochMilli(this@calendar.time)) }
}
