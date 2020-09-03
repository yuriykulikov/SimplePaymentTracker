package simple.payment.tracker.compose

import androidx.compose.foundation.Box
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope.weight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.Divider
import androidx.compose.material.EmphasisAmbient
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideEmphasis
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.koin.core.context.KoinContextHandler
import simple.payment.tracker.ListAggregator
import simple.payment.tracker.Transaction

@Composable
fun ListScreen(
  showAll: Boolean,
  currentScreen: MutableState<Screen>,
  search: State<TextFieldValue>
) {
  TransactionsList(showAll = showAll, currentScreen = currentScreen, search = search)
}

@Composable
private fun TransactionsList(
  modifier: Modifier = Modifier,
  showAll: Boolean,
  currentScreen: MutableState<Screen>,
  search: State<TextFieldValue>
) {
  Box(modifier = modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
    val data = KoinContextHandler.get()
      .get<ListAggregator>()
      .transactions()
      .toState(initial = emptyList())

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

    LazyColumnFor(
      items = items,
      modifier = Modifier.debugBorder().weight(1f),
      itemContent = { transaction ->
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