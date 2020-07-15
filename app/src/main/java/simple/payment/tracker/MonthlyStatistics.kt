package simple.payment.tracker

import io.reactivex.Observable
import java.time.Instant
import java.util.*

data class MonthlyReport(
  val month: String,
  val payments: List<Payment>
) {
  fun print() {
    println("Month: $month")
    println("Sum: ${payments.sumBy { it.sum }}")
    categorySums.forEach { (cat, sum) -> println("  $cat: $sum") }
  }

  val categorySums by lazy {
    payments
      .groupBy { it.trip ?: it.category }
      .mapValues { (cat, pmnts) -> pmnts.sumBy { it.sum } }
      .entries
      .sortedByDescending { (cat, sum) -> sum }
  }
}

class MonthlyStatistics(
  private val payments: Observable<List<Payment>>
) {
  fun reports(): Observable<List<MonthlyReport>> {
    return payments.map { monthly(it) }
  }

  companion object {
    fun monthly(merged: List<Payment>): List<MonthlyReport> {
      val monthly = merged.groupBy {
        val cal = it.calendar()
        "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH).plus(1).toString()
          .padStart(2, '0')}"
      }
      return monthly
        .entries
        .reversed()
        .map { (month, payments) ->
          MonthlyReport(month, payments)
        }
    }
  }

}

private fun Payment.calendar(): Calendar {
  return Calendar.getInstance().apply {
    time = Date.from(Instant.ofEpochMilli(this@calendar.time))
  }
}
