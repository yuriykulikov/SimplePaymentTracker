package simple.payment.tracker.compose

import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.MutableState
import androidx.compose.frames.ModelList
import androidx.compose.state
import androidx.ui.animation.Crossfade
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import simple.payment.tracker.Transaction
import simple.payment.tracker.theme.PaymentsTheme

sealed class Screen {
    object List : Screen()
    object New : Screen()
    data class Details(val transaction: Transaction) : Screen()
}

@Model
object State {
    var currentScreen: Screen = Screen.List
    val transactions = ModelList<Transaction>()

    fun showDetails(transaction: Transaction) {
        currentScreen = Screen.Details(transaction)
    }

    fun showList() {
        currentScreen = Screen.List
    }

    fun showNewPayment() {
        currentScreen = Screen.New
    }
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
    Crossfade(State.currentScreen) { screen ->
        Surface(color = MaterialTheme.colors.background) {
            when (screen) {
                is Screen.List -> ListScreen(showAll)
                is Screen.Details -> DetailsScreen(screen.transaction)
                is Screen.New -> NewScreen()
            }
        }
    }
}




