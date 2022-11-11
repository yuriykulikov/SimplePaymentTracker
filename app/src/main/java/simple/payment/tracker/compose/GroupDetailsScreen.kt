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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.Flow
import simple.payment.tracker.GroupReport
import simple.payment.tracker.Payment
import simple.payment.tracker.Transaction

data class CategoryData(
    val category: String,
    val payments: List<Payment>,
) {
  val sum by lazy { payments.sumOf { it.sum } }
  val byWeek: List<Pair<Int, List<Payment>>> by lazy {
    payments
        .groupBy {
          Calendar.getInstance().apply { timeInMillis = it.time }.get(Calendar.WEEK_OF_YEAR)
        }
        .entries
        .map { (weekOfYear, payments) -> weekOfYear to payments }
        .sortedByDescending { (_, payments) -> payments.sumOf { it.sum } }
  }
}

@Composable
fun GroupDetailsScreen(
    groupReport: GroupReport,
    showDetails: (Transaction?) -> Unit,
    state: LazyListState,
    reportLookup: (GroupReport) -> Flow<GroupReport>,
) {
  Scaffold(
      topBar = { TopAppBar(title = { Text(text = groupReport.name) }) },
      content = {
        GroupDetailsColumn(
            Modifier.padding(it),
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
    modifier: Modifier,
    groupReport: GroupReport,
    showDetails: (Transaction?) -> Unit,
    state: LazyListState,
    reportLookup: (GroupReport) -> Flow<GroupReport>,
) {
  val report = remember { reportLookup(groupReport) }.collectAsState(initial = groupReport)
  LazyColumn(modifier = modifier, state = state) {
    items(report.value.byCategory()) { categoryData: CategoryData ->
      CategoryBreakdown(categoryData, showDetails)
    }
  }
}

@Composable
private fun CategoryBreakdown(categoryData: CategoryData, showDetails: (Transaction?) -> Unit) {
  Card(modifier = Modifier.fillMaxWidth().padding(15.dp), elevation = 10.dp) {
    Column {
      CategoryHeader(categoryData)
      categoryData.byWeek.forEachIndexed { index, (weekNumber, payments) ->
        Card(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = if (index == categoryData.byWeek.lastIndex) 16.dp else 8.dp),
            elevation = 10.dp) {
          val tag = remember {
            val calendar = Calendar.getInstance().apply { set(Calendar.WEEK_OF_YEAR, weekNumber) }
            calendar.get(Calendar.WEEK_OF_MONTH)
            val month = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.timeInMillis)
            val week = SimpleDateFormat("W", Locale.getDefault()).format(calendar.timeInMillis)
            "$month, week $week"
          }
          WeekBreakdown(tag, payments, showDetails)
        }
      }
    }
  }
}

@Composable
private fun CategoryHeader(categoryData: CategoryData) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween) {
    Column {
      Text(
          text = categoryData.category,
          style = MaterialTheme.typography.h5,
          color = MaterialTheme.colors.secondary,
      )
    }
    Column {
      Text(
          text = categoryData.sum.toString(),
          style = MaterialTheme.typography.h5,
          color = MaterialTheme.colors.secondary,
      )
    }
  }
}

@Composable
private fun WeekBreakdown(
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
        payments
            .map { Transaction(payment = it) }
            .forEachIndexed { index, transaction ->
              TransactionListRow(transaction, showDetails = showDetails)
              if (index != payments.lastIndex) {
                ListDivider()
              }
            }
      }
    }
  }
}

private fun GroupReport.byCategory(): List<CategoryData> {
  val (recurrent, notRecurrent) = payments.partition { it.isRecurrent }
  return notRecurrent
      .groupBy { it.category }
      .entries
      .map { (category, payments) -> CategoryData(category, payments) }
      .sortedByDescending { it.sum }
      .plus(CategoryData("Recurrent", recurrent))
}
