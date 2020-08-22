package simple.payment.tracker.compose

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.Text
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.layout.Column
import androidx.ui.layout.Row
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.wrapContentSize
import androidx.ui.material.MaterialTheme
import org.koin.core.context.KoinContextHandler
import simple.payment.tracker.MonthlyReport
import simple.payment.tracker.MonthlyStatistics

@Composable
fun MonthlyScreen() {
  Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
    val list = KoinContextHandler.get().get<MonthlyStatistics>()
      .reports()
      .map { reports -> reports.sortedByDescending { it.month } }
      .toMutableState(initial = emptyList<MonthlyReport>())

    LazyColumnItems(list.value, itemContent = { stats ->
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


