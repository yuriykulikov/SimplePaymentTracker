package simple.payment.tracker

import io.reactivex.Observable
import java.time.Instant
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class MonthlyStatistics(private val payments: Observable<List<Payment>>) : GroupReportsProvider {
  private val reports = payments.map { monthly(it) }.replay(1).refCount(1, TimeUnit.SECONDS)

  override fun reports(): Observable<List<GroupReport>> {
    return reports
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
      return monthly
          .entries
          .reversed()
          .map { (month, payments) -> GroupReport(month, payments) }
          .sortedByDescending { it.name }
    }
  }
}

private fun Payment.calendar(): Calendar {
  return Calendar.getInstance().apply { time = Date.from(Instant.ofEpochMilli(this@calendar.time)) }
}
