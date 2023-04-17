package simple.payment.tracker.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow
import simple.payment.tracker.GroupReport
import simple.payment.tracker.GroupReportsProvider
import simple.payment.tracker.MonthlyStatistics
import simple.payment.tracker.TripStatistics
import simple.payment.tracker.theme.Theme

@Composable
fun MonthlyScreen(
    monthlyStatistics: MonthlyStatistics,
    userName: StateFlow<String?>,
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
      content = { paddingValues ->
        val name = userName.collectAsState().value
        StatisticsContent(
            Modifier.padding(paddingValues), monthlyStatistics, showMonthDetails, name ?: "")
      },
  )
}

@Composable
fun TripsScreen(
    tripStatistics: TripStatistics,
    userName: StateFlow<String?>,
    bottomBar: @Composable() () -> Unit,
    showMonthDetails: (GroupReport) -> Unit,
) {
  Scaffold(
      topBar = {
        TopAppBar(title = { Text(text = "Stats") }, backgroundColor = Theme.colors.topBar)
      },
      bottomBar = bottomBar,
      content = { paddingValues ->
        val name = userName.collectAsState().value
        StatisticsContent(
            Modifier.padding(paddingValues), tripStatistics, showMonthDetails, name ?: "")
      },
  )
}

@Composable
private fun StatisticsContent(
    modifier: Modifier,
    provider: GroupReportsProvider,
    showMonthDetails: (GroupReport) -> Unit,
    username: String,
) {
  val list: State<List<GroupReport>> =
      remember { provider.reports() }.collectAsState(initial = emptyList())

  LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
    items(list.value) { stats ->
      MonthEntry(stats, username, showMonthDetails)
      ListDivider()
    }
  }
}

private fun GroupReport.categorySums(forCat: String, forUser: String): Int {
  return payments.filter { it.user == forUser && it.category == forCat }.sumOf { it.sum }
}

@Composable
private fun MonthEntry(
    stats: GroupReport,
    username: String,
    showMonthDetails: (GroupReport) -> Unit
) {
  Column(Modifier.clickable { showMonthDetails(stats) }) {
    Row(modifier = Modifier.padding(top = 8.dp)) {
      Column(Modifier.weight(2F)) { Text(stats.name, style = typography.h6) }
      Column(Modifier.weight(1F)) {
        Text(
            stats.payments.sumOf { it.sum }.toString(),
            style = typography.h6,
            color = Theme.colors.textAccent,
        )
      }
      Column(Modifier.weight(1F)) {
        Text(
            stats.payments.filter { it.user == username }.sumOf { it.sum }.toString(),
            style = typography.h6,
            color = Theme.colors.textAccent,
        )
      }
    }
    stats.categorySums
        .sortedByDescending { (_, sum) -> sum }
        .forEach { (cat, sum) ->
          Row {
            Column(Modifier.weight(2F)) { Text(cat, style = typography.body1) }
            Column(Modifier.weight(1F)) { Text(sum.toString(), style = typography.body1) }
            Column(Modifier.weight(1F)) {
              Text(
                  stats
                      .categorySums(forCat = cat, forUser = username)
                      .takeIf { it != 0 }
                      ?.toString()
                      ?: "",
                  style = typography.body2)
            }
          }
        }
  }
}
