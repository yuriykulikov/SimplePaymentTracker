package simple.payment.tracker.compose

import androidx.compose.foundation.Box
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import simple.payment.tracker.MonthlyReport
import simple.payment.tracker.MonthlyStatistics

@Composable
fun MonthlyScreen(monthlyStatistics: MonthlyStatistics) {
  Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
    val list = monthlyStatistics
      .reports()
      .map { reports -> reports.sortedByDescending { it.month } }
      .toState(initial = emptyList<MonthlyReport>())

    LazyColumnFor(list.value, itemContent = { stats ->
      Row {
        Column(Modifier.weight(2F)) {
          Text(stats.month, style = MaterialTheme.typography.h4)
        }
        Column(Modifier.weight(1F)) {
          Text(
            stats.payments.sumBy { it.sum }.toString(),
            style = MaterialTheme.typography.h5
          )
        }
      }
      stats.categorySums
        .sortedByDescending { (_, sum) -> sum }
        .forEach { (cat, sum) ->
          Row {
            Column(Modifier.weight(2F)) {
              Text(
                cat,
                style = MaterialTheme.typography.body1
              )
            }
            Column(Modifier.weight(1F)) {
              Text(
                sum.toString(),
                style = MaterialTheme.typography.body1
              )
            }
          }
        }
      ListDivider()
    })
  }
}


