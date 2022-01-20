package simple.payment.tracker.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import simple.payment.tracker.GroupReport
import simple.payment.tracker.GroupReportsProvider
import simple.payment.tracker.MonthlyStatistics
import simple.payment.tracker.TripStatistics
import simple.payment.tracker.theme.Theme

@Composable
fun MonthlyScreen(
    monthlyStatistics: MonthlyStatistics,
    bottomBar: @Composable () -> Unit,
    showMonthDetails: (GroupReport) -> Unit,
) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(text = "Stats") },
            backgroundColor = Theme.colors.topBar,
        )
      },
      bottomBar = bottomBar,
      content = { StatisticsContent(monthlyStatistics, showMonthDetails) },
  )
}

@Composable
fun TripsScreen(
    tripStatistics: TripStatistics,
    bottomBar: @Composable() () -> Unit,
    showMonthDetails: (GroupReport) -> Unit,
) {
  Scaffold(
      topBar = {
        TopAppBar(title = { Text(text = "Stats") }, backgroundColor = Theme.colors.topBar)
      },
      bottomBar = bottomBar,
      content = { StatisticsContent(tripStatistics, showMonthDetails) },
  )
}

@Composable
private fun StatisticsContent(
    provider: GroupReportsProvider,
    showMonthDetails: (GroupReport) -> Unit,
) {
  val list: State<List<GroupReport>> = rememberRxState(initial = emptyList()) { provider.reports() }

  LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
    items(list.value) { stats ->
      MonthEntry(stats, showMonthDetails)
      ListDivider()
    }
  }
}

@Composable
private fun MonthEntry(stats: GroupReport, showMonthDetails: (GroupReport) -> Unit) {
  Column(Modifier.clickable { showMonthDetails(stats) }) {
    Row(modifier = Modifier.padding(top = 8.dp)) {
      Column(Modifier.weight(2F)) { Text(stats.name, style = typography.h6) }
      Column(Modifier.weight(1F)) {
        Text(
            stats.payments.sumBy { it.sum }.toString(),
            style = typography.h6,
            color = Theme.colors.textAccent,
        )
      }
    }
    stats.categorySums.sortedByDescending { (_, sum) -> sum }.forEach { (cat, sum) ->
      Row {
        Column(Modifier.weight(2F)) { Text(cat, style = typography.body1) }
        Column(Modifier.weight(1F)) { Text(sum.toString(), style = typography.body1) }
      }
    }
  }
}
