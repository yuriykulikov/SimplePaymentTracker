package simple.payment.tracker.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import simple.payment.tracker.InboxPayment
import simple.payment.tracker.Notification
import simple.payment.tracker.Payment
import simple.payment.tracker.SwipedPaymentsRepository
import simple.payment.tracker.Transactions
import simple.payment.tracker.theme.Theme

/** Inbox list with FAB and rows with swipe to move to another user */
@Composable
fun InboxList(
    modifier: Modifier = Modifier,
    transactions: Transactions,
    swipedPaymentsRepository: SwipedPaymentsRepository,
    showDetails: (Payment?) -> Unit,
    bottomBar: @Composable () -> Unit,
    listState: LazyListState,
) {
  Scaffold(
      topBar = { InboxTopBar() },
      bottomBar = bottomBar,
      content = { paddingValues ->
        Column(modifier = modifier.padding(paddingValues).fillMaxSize()) {
          val data =
              remember {
                    combine(
                        transactions.inbox,
                        swipedPaymentsRepository.swiped(),
                    ) { inbox, swiped ->
                      inbox
                          .mapNotNull { it as? InboxPayment }
                          .map { inboxPayment ->
                            InboxRowData(inboxPayment, inboxPayment.notification in swiped)
                          }
                    }
                  }
                  .collectAsState(initial = emptyList())
          val scope = rememberCoroutineScope()
          InboxColumn(
              listState,
              data,
              showDetails,
              onSwipe = { notification ->
                scope.launch { swipedPaymentsRepository.swipe(notification) }
              })
        }
      },
      floatingActionButton = {
        FloatingActionButton(
            onClick = { showDetails(null) },
            backgroundColor = Theme.colors.surfaceAccent,
        ) {
          Icon(Icons.Default.Add, contentDescription = "", tint = Theme.colors.text)
        }
      },
  )
}

data class InboxRowData(
    val payment: InboxPayment,
    val swiped: Boolean,
) {
  val notification = payment.notification
}

@Composable
private fun InboxColumn(
    listState: LazyListState,
    data: State<List<InboxRowData>>,
    showDetails: (Payment?) -> Unit,
    onSwipe: (Notification) -> Unit,
) {
  LazyColumn(state = listState) {
    items(
        items = data.value,
        key = { it.notification.time },
    ) { rowData: InboxRowData ->
      val transaction = rowData.payment
      val swiped = rowData.swiped
      InboxRow(onSwipe, transaction, rowData, swiped, showDetails)
    }
  }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun InboxRow(
    onSwipe: (Notification) -> Unit,
    transaction: InboxPayment,
    rowData: InboxRowData,
    swiped: Boolean,
    showDetails: (Payment?) -> Unit
) {
  val dismissState = rememberDismissState { value ->
    if (value == DismissValue.DismissedToEnd) {
      onSwipe(transaction.notification)
    }
    value == DismissValue.DismissedToEnd
  }

  LaunchedEffect(rowData) {
    if (!swiped && dismissState.currentValue != DismissValue.Default) {
      dismissState.snapTo(DismissValue.Default)
    }
  }

  AnimatedVisibility(
      visible = !swiped,
      exit = shrinkVertically(TweenSpec(300, 0, FastOutLinearInEasing)),
      enter = expandVertically(TweenSpec(300, 0, FastOutLinearInEasing)),
  ) {
    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd),
        background = {
          Row(
              Modifier.fillMaxSize().background(Theme.colors.surfaceAccent).padding(20.dp),
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(Icons.Default.ArrowDropDown, "")
            Text(text = "Other user")
          }
        },
    ) {
      Row(
          modifier =
              Modifier.clickable(onClick = { showDetails(transaction) })
                  .background(Theme.colors.background)
                  .padding(16.dp),
      ) {
        Column(modifier = Modifier.weight(1f)) {
          TransactionTitle(transaction)
          TransactionSubtitle(transaction)
        }
      }
    }
  }
}
