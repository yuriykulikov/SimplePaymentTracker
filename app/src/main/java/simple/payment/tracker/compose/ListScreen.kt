package simple.payment.tracker.compose

import androidx.compose.Composable
import androidx.compose.MutableState
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.AdapterList
import androidx.ui.foundation.Box
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.Text
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.ripple.ripple
import androidx.ui.text.style.TextDecoration
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import simple.payment.tracker.Transaction
import simple.payment.tracker.theme.PaymentsTheme

@Preview("ListScreen preview")
@Composable
fun PreviewListScreen() {
    PaymentsTheme {
        Surface {
            ListScreen(state { true })
        }
    }
}

@Composable
fun ListScreen(showAll: MutableState<Boolean>) {
    Scaffold(
        topAppBar = {
            TopAppBar(
                title = { Text(text = if (showAll.value) "All payments" else "New payments") },
                actions = {
                    IconButton(onClick = { State.currentScreen = Screen.Monthly }) {
                        Text(text = "Stats", style = MaterialTheme.typography.body2)
                    }

                    IconButton(onClick = { showAll.value = !showAll.value }) {
                        Text(text = "View", style = MaterialTheme.typography.body2)
                    }

                    IconButton(onClick = { State.showNewPayment() }) {
                        Text(text = "Add", style = MaterialTheme.typography.body2)
                    }
                }
            )
        },
        bodyContent = { modifier ->
            TransactionsList(modifier, showAll)
        }
    )
}

@Composable
private fun TransactionsList(modifier: Modifier, showAll: MutableState<Boolean>) {
    Box(modifier = modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
        val data = when {
            showAll.value -> State.transactions
            else -> State.transactions.filter { it.payment == null }
        }
        AdapterList(data, itemCallback = { transaction ->
            TransactionListRow(transaction)
            ListDivider()
        })
    }
}

@Composable
fun TransactionListRow(transaction: Transaction) {
    Clickable(
        modifier = Modifier.ripple(),
        onClick = { State.showDetails(transaction) }
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                TransactionTitle(transaction)
                TransactionSubtitle(transaction)
            }
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
                text = transaction.category ?: "",
                style = MaterialTheme.typography.body2
            )
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = transaction.comment ?: "",
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