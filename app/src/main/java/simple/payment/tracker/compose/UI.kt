package simple.payment.tracker.compose

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import simple.payment.tracker.MonthlyStatistics
import simple.payment.tracker.PaymentsRepository
import simple.payment.tracker.Transaction
import simple.payment.tracker.TransactionsRepository
import simple.payment.tracker.theme.PaymentsTheme
import kotlin.random.Random.Default.nextInt

sealed class Screen {
  object List : Screen()
  object ListAll : Screen()
  object New : Screen()
  object Monthly : Screen()
  data class Details(val transaction: Transaction) : Screen()
}

@Composable
fun PaymentsApp(
  backs: Backs,
  transactions: TransactionsRepository,
  paymentsRepository: PaymentsRepository,
  monthlyStatistics: MonthlyStatistics,
) {
  PaymentsTheme {
    AppContent(
      backs,
      transactions,
      paymentsRepository,
      monthlyStatistics,
    )
  }
}

@Composable
private fun AppContent(
  backs: Backs,
  transactions: TransactionsRepository,
  paymentsRepository: PaymentsRepository,
  monthlyStatistics: MonthlyStatistics,
) {
  val selectedScreen: MutableState<Screen> = remember { mutableStateOf(Screen.List) }
  val detailsToShow: MutableState<Screen?> = remember { mutableStateOf(null) }
  val hideDetails = { detailsToShow.value = null }
  val showDetails: (Transaction?) -> Unit =
    { detailsToShow.value = if (it == null) Screen.New else Screen.Details(it) }

  val bottomBar = @Composable { NavigationBottomBar(selectedScreen) }

  backs
    .backPressed
    .commitSubscribe {
      if (detailsToShow.value != null) {
        hideDetails()
      } else {
        selectedScreen.value = Screen.List
      }
    }

  val screen: Screen = detailsToShow.value ?: selectedScreen.value

  Crossfade(screen) { scr ->
    Surface(color = MaterialTheme.colors.background) {
      when (scr) {
        is Screen.List -> ListScreen(false, transactions, showDetails, bottomBar)
        is Screen.ListAll -> ListScreen(true, transactions, showDetails, bottomBar)
        is Screen.Details -> DetailsScreen(paymentsRepository, scr.transaction, hideDetails)
        is Screen.New -> DetailsScreen(paymentsRepository, null, hideDetails)
        is Screen.Monthly -> MonthlyScreen(monthlyStatistics, bottomBar)
      }
    }
  }
}

val borderColors = listOf(
  Color.Black,
  Color.DarkGray,
  Color.Gray,
  Color.LightGray,
  Color.White,
  Color.Red,
  Color.Green,
  Color.Blue,
  Color.Yellow,
  Color.Cyan,
  Color.Magenta,
  Color.Transparent
)

@Composable
fun Modifier.debugBorder(): Modifier {
  return this
  return border(width = 1.dp, color = borderColors[nextInt(0, borderColors.lastIndex)])
}

@Composable
private fun NavigationBottomBar(currentScreen: MutableState<Screen>) {
  BottomAppBar(
    modifier = Modifier.fillMaxWidth()
  ) {
    IconButton(
      onClick = { currentScreen.value = Screen.List },
      modifier = Modifier.weight(1f)
        .highlightIf(currentScreen, Screen.List)
    ) {
      Text(
        text = "Inbox", style = MaterialTheme.typography.body2
      )
    }

    IconButton(
      onClick = { currentScreen.value = Screen.ListAll },
      modifier = Modifier.weight(1f)
        .highlightIf(currentScreen, Screen.ListAll)
    ) {
      Text(text = "All", style = MaterialTheme.typography.body2)
    }

    IconButton(
      onClick = { currentScreen.value = Screen.Monthly },
      modifier = Modifier.weight(1f)
        .highlightIf(currentScreen, Screen.Monthly)
    ) {
      Text(text = "Stats", style = MaterialTheme.typography.body2)
    }
  }
}

@Composable
private fun Modifier.highlightIf(
  currentScreen: MutableState<Screen>,
  target: Screen
): Modifier {
  return when (currentScreen.value) {
    target -> {
      background(
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp)
      )
    }
    else -> this
  }
}
