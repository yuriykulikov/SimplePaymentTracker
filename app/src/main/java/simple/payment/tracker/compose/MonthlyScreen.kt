package simple.payment.tracker.compose

import androidx.compose.Composable
import androidx.compose.onCommit
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.AdapterList
import androidx.ui.foundation.Box
import androidx.ui.foundation.Text
import androidx.ui.layout.Column
import androidx.ui.layout.Row
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.wrapContentSize
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Scaffold
import androidx.ui.material.TopAppBar
import org.koin.core.context.KoinContextHandler
import simple.payment.tracker.MonthlyReport
import simple.payment.tracker.MonthlyStatistics

@Composable
fun MonthlyScreen() {
    Scaffold(
        topAppBar = {
            TopAppBar(
                title = { Text(text = "Monthly statistics") },
                actions = {
                    IconButton(onClick = { State.currentScreen = Screen.List }) {
                        Text(text = "Back", style = MaterialTheme.typography.body2)
                    }
                }
            )
        },
        bodyContent = { modifier ->
            MonthList(modifier)
        }
    )
}

@Composable
private fun MonthList(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize().wrapContentSize(Alignment.Center)) {
        val list = state { emptyList<MonthlyReport>() }

        onCommit {
            val subscription = KoinContextHandler.get().get<MonthlyStatistics>()
                .reports()
                .map { reports -> reports.sortedByDescending { it.month } }
                .subscribe { list.value = it }
            onDispose {
                subscription.dispose()
            }
        }

        AdapterList(list.value, itemCallback = { stats ->
            Row {
                Column(Modifier.weight(2F)) {
                    Text(stats.month, style = MaterialTheme.typography.h4)
                }
                Column(Modifier.weight(1F)) {
                    Text(
                        stats.payments.sumBy { it.sum }.toString(),
                        style = MaterialTheme.typography.h5
                    )
                }
            }
            stats.categorySums
                .sortedByDescending { (cat, sum) -> sum }
                .forEach { (cat, sum) ->
                    Row {
                        Column(Modifier.weight(2F)) {
                            Text(
                                cat,
                                style = MaterialTheme.typography.body1
                            )
                        }
                        Column(Modifier.weight(1F)) {
                            Text(
                                sum.toString(),
                                style = MaterialTheme.typography.body1
                            )
                        }
                    }
                }
            ListDivider()
        })
    }
}


