package simple.payment.tracker.compose

import androidx.compose.Composable
import androidx.compose.MutableState
import androidx.compose.state
import androidx.ui.animation.Crossfade
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import org.koin.core.context.KoinContextHandler
import simple.payment.tracker.Transaction
import simple.payment.tracker.theme.PaymentsTheme

sealed class Screen {
  object List : Screen()
  object New : Screen()
  object Monthly : Screen()
  data class Details(val transaction: Transaction) : Screen()
}

@Composable
fun PaymentsApp() {
  PaymentsTheme {
    AppContent()
  }
}

@Composable
private fun AppContent() {
  val showAll: MutableState<Boolean> = state { false }
  val currentScreen: MutableState<Screen> = state<Screen> { Screen.List }
  KoinContextHandler.get().get<Backs>()
    .backPressed
    .commitSubscribe {
      currentScreen.value = Screen.List
    }

  Crossfade(currentScreen) { screen ->
    Surface(color = MaterialTheme.colors.background) {
      when (val scr = screen.value) {
        is Screen.List -> ListScreen(showAll, currentScreen)
        is Screen.Details -> DetailsScreen(scr.transaction, currentScreen)
        is Screen.New -> DetailsScreen(null, currentScreen)
        is Screen.Monthly -> MonthlyScreen(currentScreen)
      }
    }
  }
}
