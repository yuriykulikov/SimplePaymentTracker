package simple.payment.tracker.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.map
import simple.payment.tracker.AutomaticPayment
import simple.payment.tracker.Icon
import simple.payment.tracker.InboxPayment
import simple.payment.tracker.Payment
import simple.payment.tracker.R
import simple.payment.tracker.TransactionsRepository
import simple.payment.tracker.theme.Theme

@Composable
fun ListScreen(
    showAll: Boolean,
    transactionsRepository: TransactionsRepository,
    showDetails: (Payment?) -> Unit,
    bottomBar: @Composable () -> Unit,
    search: MutableState<TextFieldValue>,
    listState: LazyListState,
) {
  when {
    showAll ->
        TransactionsList(
            transactionsRepository = transactionsRepository,
            showDetails = showDetails,
            bottomBar = bottomBar,
            search = search,
            listState = listState,
        )
    else ->
        InboxList(
            transactionsRepository = transactionsRepository,
            showDetails = showDetails,
            bottomBar = bottomBar,
            listState = listState,
        )
  }
}

@Composable
private fun TransactionsList(
    modifier: Modifier = Modifier,
    transactionsRepository: TransactionsRepository,
    showDetails: (Payment?) -> Unit,
    bottomBar: @Composable () -> Unit,
    search: MutableState<TextFieldValue>,
    listState: LazyListState,
) {
  Scaffold(
      topBar = { SearchBar(search) },
      bottomBar = bottomBar,
      content = { innerPadding ->
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.TopStart)
                    .padding(bottom = innerPadding.calculateBottomPadding())) {
              val data =
                  remember { transactionsRepository.transactions() }
                      .collectAsState(initial = emptyList())

              val items =
                  when {
                    search.value.text.isEmpty() -> data.value
                    else -> {
                      val searchLowercase = search.value.text.lowercase()
                      data.value.filter { transaction ->
                        searchLowercase in transaction.toString().lowercase()
                      }
                    }
                  }
              if (items.isNotEmpty()) {
                LazyColumn(modifier = Modifier.debugBorder(), state = listState) {
                  items(items) { transaction ->
                    TransactionListRow(transaction, showDetails)
                    ListDivider()
                  }
                }
              }
            }
      })
}

@Composable
private fun InboxList(
    modifier: Modifier = Modifier,
    transactionsRepository: TransactionsRepository,
    showDetails: (Payment?) -> Unit,
    bottomBar: @Composable() () -> Unit,
    listState: LazyListState,
) {
  Scaffold(
      topBar = { InboxTopBar() },
      bottomBar = bottomBar,
      content = {
        Column(modifier = modifier.padding(it).fillMaxSize()) {
          val data =
              remember {
                    transactionsRepository.transactions().map { list ->
                      list.filterIsInstance<InboxPayment>()
                    }
                  }
                  .collectAsState(initial = emptyList())

          LazyColumn(modifier = Modifier.debugBorder(), state = listState) {
            items(data.value) { transaction ->
              TransactionListRow(transaction, showDetails)
              ListDivider()
            }
          }
        }
      },
      floatingActionButton = {
        FloatingActionButton(
            onClick = { showDetails(null) },
            backgroundColor = Theme.colors.surfaceAccent,
        ) {
          Icon(id = R.drawable.ic_baseline_add_24, tint = Theme.colors.text)
        }
      },
  )
}

@Composable
fun SearchBar(search: MutableState<TextFieldValue>) {
  TopAppBar(
      backgroundColor = Theme.colors.topBar,
      content = {
        val color = if (colors.isLight) colors.onPrimary else colors.onSurface
        TextField(
            value = search.value,
            onValueChange = { search.value = it },
            label = { Text("Search") },
            colors =
                TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = color.copy(0.5f),
                    unfocusedIndicatorColor = color,
                ),
            modifier = Modifier.padding(2.dp),
        )
      })
}

@Composable
fun InboxTopBar() {
  TopAppBar(backgroundColor = Theme.colors.topBar, title = { Text(text = "Inbox") })
}

@Composable
fun TransactionListRow(payment: Payment, showDetails: (Payment?) -> Unit) {
  Row(modifier = Modifier.clickable(onClick = { showDetails(payment) }).padding(16.dp)) {
    Column(modifier = Modifier.weight(1f)) {
      TransactionTitle(payment)
      TransactionSubtitle(payment)
    }
  }
}

@Composable
fun TransactionTitle(payment: Payment) {
  Row(horizontalArrangement = Arrangement.SpaceAround) {
    Text(payment.merchant, style = typography.subtitle1)
    Text(text = "", modifier = Modifier.weight(1F))
    Text(payment.annotatedSumWithRefunds(), style = typography.subtitle1)
  }
}

@Composable
fun TransactionSubtitle(payment: Payment, modifier: Modifier = Modifier) {
  Row(modifier) {
    Text(
        text = payment.category,
        style = typography.subtitle2,
        color = colors.primaryVariant,
    )

    Text(
        modifier = Modifier.padding(start = 16.dp),
        text = payment.comment,
        style = typography.subtitle2,
        color = colors.secondary,
    )
  }
  Row {
    if (payment is AutomaticPayment) {
      Text(
          text = "auto",
          style = typography.subtitle2,
          color = colors.primary,
      )
    }
    val trip = payment.trip
    if (trip != null) {
      Text(
          text = trip,
          style = typography.subtitle2,
          color = colors.primary,
      )
    }
  }
}

@Composable
fun ListDivider() {
  Divider(color = colors.onSurface.copy(alpha = 0.08f))
}
