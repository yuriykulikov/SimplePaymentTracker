package simple.payment.tracker.compose

import androidx.compose.Composable
import androidx.compose.MutableState
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.input.TextFieldValue
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.Row
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.padding
import androidx.ui.layout.wrapContentSize
import androidx.ui.material.Divider
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ProvideEmphasis
import androidx.ui.text.style.TextDecoration
import androidx.ui.unit.dp
import org.koin.core.context.KoinContextHandler
import simple.payment.tracker.ListAggregator
import simple.payment.tracker.Transaction

@Composable
fun ListScreen(
  showAll: Boolean,
  currentScreen: MutableState<Screen>,
  search: MutableState<TextFieldValue>
) {
  TransactionsList(showAll = showAll, currentScreen = currentScreen, search = search)
}

@Composable
private fun TransactionsList(
  modifier: Modifier = Modifier,
  showAll: Boolean,
  currentScreen: MutableState<Screen>,
  search: MutableState<TextFieldValue>
) {
  Box(modifier = modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
    val data = KoinContextHandler.get()
      .get<ListAggregator>()
      .transactions()
      .toMutableState(initial = emptyList())

    val items = data.value
      .let { values -> if (showAll) values else values.filter { it.payment == null } }
      .let { values ->
        when {
          search.value.text.isEmpty() -> values
          else -> {
            val searchLowercase = search.value.text.toLowerCase()
            values.filter { transaction ->
              searchLowercase in transaction.toString().toLowerCase()
            }
          }
        }
      }

    LazyColumnItems(items, itemContent = { transaction ->
      TransactionListRow(transaction, currentScreen)
      ListDivider()
    })
  }
}

@Composable
fun TransactionListRow(transaction: Transaction, currentScreen: MutableState<Screen>) {
  Row(
    modifier = Modifier
      .clickable(onClick = { currentScreen.value = Screen.Details(transaction) })
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
  ProvideEmphasis(EmphasisAmbient.current.high) {
    Row(horizontalArrangement = Arrangement.SpaceAround) {
      Text(
        transaction.merchant,
        style = when {
          transaction.cancelled -> MaterialTheme.typography.subtitle1.copy(textDecoration = TextDecoration.LineThrough)
          else -> MaterialTheme.typography.subtitle1
        }
      )
      Text(text = "", modifier = Modifier.weight(1F))
      Text(
        "${transaction.sum}",
        style = MaterialTheme.typography.subtitle1
      )
    }
  }
}

@Composable
fun TransactionSubtitle(
  transaction: Transaction,
  modifier: Modifier = Modifier
) {
  Row(modifier) {
    ProvideEmphasis(EmphasisAmbient.current.medium) {
      Text(
        text = transaction.category,
        style = MaterialTheme.typography.body2
      )
      Text(
        modifier = Modifier.padding(start = 16.dp),
        text = transaction.comment,
        style = MaterialTheme.typography.body2
      )
    }
  }
}

@Composable
fun ListDivider() {
  Divider(
    color = MaterialTheme.colors.onSurface.copy(alpha = 0.08f)
  )
}