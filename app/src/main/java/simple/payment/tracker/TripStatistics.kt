package simple.payment.tracker

import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class TripStatistics(private val payments: Observable<List<Payment>>) : GroupReportsProvider {
  private val reports = payments.map { monthly(it) }.replay(1).refCount(1, TimeUnit.SECONDS)

  override fun reports(): Observable<List<GroupReport>> {
    return reports
  }

  companion object {
    fun monthly(merged: List<Payment>): List<GroupReport> {
      val trips = merged.filterNot { it.trip.isNullOrEmpty() }.groupBy { it.trip ?: "" }

      return trips.entries //
          .map { (trip, payments) -> GroupReport(trip, payments) }
          .sortedByDescending { it.date }
    }
  }
}
