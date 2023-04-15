package simple.payment.tracker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

class TripStatistics(private val payments: Flow<List<Payment>>) : GroupReportsProvider {
  private val scope = CoroutineScope(Dispatchers.Unconfined)
  private val reports =
      payments.map { monthly(it) }.shareIn(scope, SharingStarted.WhileSubscribed(1000), 1)

  override fun reports(): Flow<List<GroupReport>> {
    return reports
  }

  override fun report(name: String): Flow<GroupReport> {
    return reports.map { reports -> reports.first { it.name == name } }
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
