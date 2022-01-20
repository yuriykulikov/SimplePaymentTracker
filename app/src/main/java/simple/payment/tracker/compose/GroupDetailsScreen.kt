package simple.payment.tracker.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.reactivex.Observable
import java.util.*
import simple.payment.tracker.GroupReport
import simple.payment.tracker.Payment
import simple.payment.tracker.Transaction

data class WeekData(
    val weekNumber: Int,
    val payments: List<Payment>,
) {
  val byCategory: List<Pair<String, List<Payment>>> by lazy {
    payments
        .groupBy { it.category }
        .entries
        .map { (cat, payments) -> cat to payments }
        .sortedByDescending { (_, payments) -> payments.sumOf { it.sum } }
  }
}

@Composable
fun GroupDetailsScreen(
    groupReport: GroupReport,
    showDetails: (Transaction?) -> Unit,
    state: LazyListState,
    reportLookup: (GroupReport) -> Observable<GroupReport>,
) {
  Scaffold(
      topBar = { TopAppBar(title = { Text(text = groupReport.name) }) },
      content = {
        GroupDetailsColumn(
            groupReport,
            showDetails,
            state,
            reportLookup,
        )
      },
  )
}

@Composable
private fun GroupDetailsColumn(
    groupReport: GroupReport,
    showDetails: (Transaction?) -> Unit,
    state: LazyListState,
    reportLookup: (GroupReport) -> Observable<GroupReport>,
) {
  val report = rememberRxState(groupReport) { reportLookup(groupReport) }
  LazyColumn(state = state) {
    items(report.value.weeklyPaymentsWithoutRecurrent()) { weekData: WeekData ->
      WeekDetails(weekData, showDetails)
    }
  }
}

@Composable
private fun WeekDetails(weekData: WeekData, showDetails: (Transaction?) -> Unit) {
  Card(modifier = Modifier.fillMaxWidth().padding(15.dp), elevation = 10.dp) {
    Column {
      WeekHeader(weekData)
      weekData.byCategory.forEachIndexed { index, (category, payments) ->
        Card(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = if (index == weekData.byCategory.lastIndex) 16.dp else 8.dp),
            elevation = 10.dp) { CategoryBreakdown(category, payments, showDetails) }
      }
    }
  }
}

@Composable
private fun WeekHeader(weekData: WeekData) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween) {
    Column {
      Text(
          text = "Week ${weekData.weekNumber}",
          style = MaterialTheme.typography.h5,
          color = MaterialTheme.colors.secondary,
      )
    }
    Column {
      Text(
          text =
              weekData.byCategory.sumOf { (_, payments) -> payments.sumOf { it.sum } }.toString(),
          style = MaterialTheme.typography.h5,
          color = MaterialTheme.colors.secondary,
      )
    }
  }
}

@Composable
private fun CategoryBreakdown(
    cat: String,
    payments: List<Payment>,
    showDetails: (Transaction?) -> Unit
) {
  Column(Modifier.padding(10.dp)) {
    Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Column {
        Text(
            text = cat,
            style = MaterialTheme.typography.h6,
        )
      }
      Column {
        Text(
            text = "${payments.sumOf { it.sum }}",
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.secondary,
        )
      }
    }
    Row {
      Column {
        payments.map { Transaction(payment = it) }.forEachIndexed { index, transaction ->
          TransactionListRow(transaction, showDetails = showDetails)
          if (index != payments.lastIndex) {
            ListDivider()
          }
        }
      }
    }
  }
}

private fun GroupReport.weeklyPaymentsWithoutRecurrent(): List<WeekData> {
  return payments
      .filterNot { it.isRecurrent }
      .groupBy {
        Calendar.getInstance().apply { timeInMillis = it.time }.get(Calendar.WEEK_OF_MONTH)
      }
      .entries
      .map { (weekNumber, payments) -> WeekData(weekNumber, payments) }
      .sortedByDescending { it.weekNumber }
}
