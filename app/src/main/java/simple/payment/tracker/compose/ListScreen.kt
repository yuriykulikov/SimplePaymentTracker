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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import simple.payment.tracker.Icon
import simple.payment.tracker.R
import simple.payment.tracker.Transaction
import simple.payment.tracker.TransactionsRepository
import java.util.Locale

@Composable
fun ListScreen(
  showAll: Boolean,
  transactionsRepository: TransactionsRepository,
  showDetails: (Transaction?) -> Unit,
  bottomBar: @Composable() () -> Unit
) {
  when {
    showAll -> TransactionsList(
      transactionsRepository = transactionsRepository,
      showDetails = showDetails,
      bottomBar = bottomBar,
    )
    else -> InboxList(
      transactionsRepository = transactionsRepository,
      showDetails = showDetails,
      bottomBar = bottomBar,
    )
  }
}

@Composable
private fun TransactionsList(
  modifier: Modifier = Modifier,
  transactionsRepository: TransactionsRepository,
  showDetails: (Transaction?) -> Unit,
  bottomBar: @Composable() () -> Unit
) {
  val search = remember { mutableStateOf(TextFieldValue("")) }

  Scaffold(
    topBar = {
      SearchBar(search)
    },
    bottomBar = bottomBar,
    content = {
      Box(
        modifier = modifier
          .fillMaxSize()
          .wrapContentSize(Alignment.Center)
      ) {
        val data = rememberRxState(initial = emptyList()) { transactionsRepository.transactions() }

        val items = when {
          search.value.text.isEmpty() -> data.value
          else -> {
            val searchLowercase = search.value.text.toLowerCase(Locale.getDefault())
            data.value.filter { transaction ->
              searchLowercase in transaction.toString().toLowerCase(Locale.getDefault())
            }
          }
        }

        LazyColumn(
          modifier = Modifier.debugBorder()
        ) {
          items(items) { transaction ->
            TransactionListRow(transaction, showDetails)
            ListDivider()
          }
        }
      }
    }
  )
}

@Composable
private fun InboxList(
  modifier: Modifier = Modifier,
  transactionsRepository: TransactionsRepository,
  showDetails: (Transaction?) -> Unit,
  bottomBar: @Composable() () -> Unit
) {
  Scaffold(
    topBar = {
      InboxTopBar()
    },
    bottomBar = bottomBar,
    content = {
      Column(modifier = modifier.fillMaxSize()) {
        val data =
          rememberRxState(initial = emptyList()) {
            transactionsRepository
              .transactions()
              .map { list -> list.filter { it.payment == null } }
          }

        LazyColumn(
          modifier = Modifier.debugBorder()
        ) {
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
      ) {
        Icon(
          id = R.drawable.ic_baseline_add_24,
          tint = colors.onPrimary
        )
      }
    },
  )
}

@Composable
fun SearchBar(search: MutableState<TextFieldValue>) {
  TopAppBar(
    content = {
      val color = if (colors.isLight) colors.onPrimary else colors.onSurface
      TextField(
        value = search.value,
        onValueChange = { search.value = it },
        label = { Text("Search") },
        colors = TextFieldDefaults.textFieldColors(
          backgroundColor = Color.Transparent,
          focusedIndicatorColor = color.copy(0.5f),
          unfocusedIndicatorColor = color,
        ),
        modifier = Modifier.padding(2.dp),
      )
    }
  )
}

@Composable
fun InboxTopBar() {
  TopAppBar(title = { Text(text = "Inbox") })
}

@Composable
fun TransactionListRow(transaction: Transaction, showDetails: (Transaction?) -> Unit) {
  Row(
    modifier = Modifier
      .clickable(onClick = { showDetails(transaction) })
      .padding(16.dp)
  ) {
    Column(modifier = Modifier.weight(1f)) {
      TransactionTitle(transaction)
      TransactionSubtitle(transaction)
    }
  }
}

@Composable
fun TransactionTitle(transaction: Transaction) {
  Row(horizontalArrangement = Arrangement.SpaceAround) {
    Text(
      transaction.merchant,
      style = when {
        transaction.cancelled -> typography.subtitle1.copy(textDecoration = TextDecoration.LineThrough)
        else -> typography.subtitle1
      }
    )
    Text(text = "", modifier = Modifier.weight(1F))
    Text(
      "${transaction.sum}",
      style = typography.subtitle1
    )
  }
}

@Composable
fun TransactionSubtitle(
  transaction: Transaction,
  modifier: Modifier = Modifier
) {
  Row(modifier) {
    Text(
      text = transaction.category,
      style = typography.subtitle2,
      color = colors.primaryVariant,
    )

    val subtitleComment = when {
      transaction.payment?.auto == true -> "auto"
      transaction.trip != null -> transaction.trip
      else -> transaction.comment
    }
    Text(
      modifier = Modifier.padding(start = 16.dp),
      text = subtitleComment,
      style = typography.subtitle2,
      color = colors.secondary,
    )
  }
}

@Composable
fun ListDivider() {
  Divider(
    color = colors.onSurface.copy(alpha = 0.08f)
  )
}