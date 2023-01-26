package simple.payment.tracker.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import simple.payment.tracker.GroupReport
import simple.payment.tracker.Payment
import simple.payment.tracker.RecurringPayment
import simple.payment.tracker.theme.Theme

data class CategoryData(
    val category: String,
    val payments: List<Payment>,
) {
  val sum by lazy { payments.sumOf { it.sum } }
  val byMerchant by lazy { payments.groupBy { it.merchant } }
}

@Composable
fun GroupDetailsScreen(
    groupReport: GroupReport,
    showDetails: (Payment?) -> Unit,
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
    showDetails: (Payment?) -> Unit,
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

/**
 * Category sum Merchant sum
 * ```
 *     comment  sum
 *     comment  sum
 * ```
 * Merchant sum
 * ```
 *     comment  sum
 *     comment  sum
 * ```
 */
@Composable
private fun CategoryBreakdown(categoryData: CategoryData, showDetails: (Payment?) -> Unit) {
  Card(modifier = Modifier.padding(4.dp), elevation = 4.dp) {
    Column(Modifier.padding(4.dp)) {
      CategoryHeader(categoryData.category, categoryData.sum)
      categoryData.byMerchant.forEach { (merchant, payments) ->
        MerchantBreakdown(merchant, payments, showDetails)
      }
    }
  }
}

@Composable
private fun CategoryHeader(category: String, sum: Int) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text(
        text = category,
        style = typography.h6,
        color = colors.secondary,
    )
    Text(
        text = sum.toString(),
        style = typography.h6,
        color = Theme.colors.surfaceAccent,
    )
  }
}

/** Merchant sum comment sum */
@Composable
private fun MerchantBreakdown(
    merchant: String,
    payments: List<Payment>,
    showDetails: (Payment?) -> Unit
) {
  Column {
    Row(horizontalArrangement = Arrangement.Start) {
      Text(text = merchant, style = typography.subtitle1)
    }
    payments.forEach { transaction ->
      Row(
          modifier =
              Modifier.clickable(onClick = { showDetails(transaction) })
                  .fillMaxWidth()
                  .padding(start = 4.dp),
          horizontalArrangement = SpaceBetween,
      ) {
        Text(
            text = transaction.comment,
            style = typography.subtitle2,
            color = Theme.colors.text,
        )

        Text(
            text = transaction.annotatedSumWithRefunds(),
            style = typography.body1,
            color = Theme.colors.textAccent,
        )
      }
    }
  }
}

private fun GroupReport.byCategory(): List<CategoryData> {
  val (recurrent, notRecurrent) = payments.partition { it is RecurringPayment }
  return notRecurrent
      .groupBy { it.category }
      .entries
      .map { (category, payments) -> CategoryData(category, payments) }
      .sortedByDescending { it.sum }
      .plus(CategoryData("Recurrent", recurrent))
}
