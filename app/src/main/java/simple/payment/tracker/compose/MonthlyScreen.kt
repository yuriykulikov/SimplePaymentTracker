package simple.payment.tracker.compose


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import simple.payment.tracker.MonthlyReport
import simple.payment.tracker.MonthlyStatistics

@Composable
fun MonthlyScreen(monthlyStatistics: MonthlyStatistics, bottomBar: @Composable() () -> Unit) {
  Scaffold(
    topBar = { TopAppBar(title = { Text(text = "Stats") }) },
    bottomBar = bottomBar,
    bodyContent = {
      StatisticsContent(monthlyStatistics)
    },
  )
}

@Composable
private fun StatisticsContent(monthlyStatistics: MonthlyStatistics) {
  val list: State<List<MonthlyReport>> = rememberRxState(initial = emptyList()) {
    monthlyStatistics
      .reports()
      .map { reports -> reports.sortedByDescending { it.month } }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    items(list.value, itemContent = { stats ->
      MonthEntry(stats)
      ListDivider()
    })
  }
}

@Composable
private fun MonthEntry(stats: MonthlyReport) {
  Row(modifier = Modifier.padding(top = 8.dp)) {
    Column(Modifier.weight(2F)) {
      Text(stats.month, style = typography.h6)
    }
    Column(Modifier.weight(1F)) {
      Text(
        stats.payments.sumBy { it.sum }.toString(),
        style = typography.h6,
        color = colors.secondary,
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
            style = typography.body1
          )
        }
        Column(Modifier.weight(1F)) {
          Text(
            sum.toString(),
            style = typography.body1
          )
        }
      }
    }
}


