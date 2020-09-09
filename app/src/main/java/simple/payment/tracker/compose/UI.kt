package simple.payment.tracker.compose

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Box
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
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
fun MutableState<Screen>.showNavigation(): Boolean {
  return when (value) {
    Screen.List, Screen.ListAll, Screen.Monthly -> {
      true
    }
    Screen.New -> false
    is Screen.Details -> false
  }
}

@Composable
private fun AppContent(
  backs: Backs,
  transactions: TransactionsRepository,
  paymentsRepository: PaymentsRepository,
  monthlyStatistics: MonthlyStatistics,
) {
  val currentScreen: MutableState<Screen> = remember { mutableStateOf(Screen.List) }
  backs
    .backPressed
    .commitSubscribe {
      currentScreen.value = Screen.List
    }

  val search = remember { mutableStateOf(TextFieldValue("")) }
  Scaffold(
    topBar = {
      if (currentScreen.showNavigation()) {
        NavigationTopBar(search, currentScreen)
      }
    },
    bottomBar = {
      when (currentScreen.value) {
        Screen.List, Screen.ListAll, Screen.Monthly -> {
          NavigationBottomBar(currentScreen)
        }
        Screen.New -> Unit
        is Screen.Details -> Unit
      }
    },
    bodyContent = {
      Crossfade(currentScreen) { screen ->
        Surface(color = MaterialTheme.colors.background) {
          when (val scr = screen.value) {
            is Screen.List -> ListScreen(false, transactions, currentScreen, search)
            is Screen.ListAll -> ListScreen(true, transactions, currentScreen, search)
            is Screen.Details -> DetailsScreen(paymentsRepository, scr.transaction, currentScreen)
            is Screen.New -> DetailsScreen(paymentsRepository, null, currentScreen)
            is Screen.Monthly -> MonthlyScreen(monthlyStatistics)
          }
        }
      }
    }
  )
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
private fun NavigationTopBar(
  search: MutableState<TextFieldValue>,
  currentScreen: MutableState<Screen>
) {
  TopAppBar(
    content = {
      if (currentScreen.value is Screen.ListAll) {
        TextField(
          value = search.value,
          onValueChange = { search.value = it },
          label = { Text("Search") },
          textStyle = MaterialTheme.typography.body1,
          backgroundColor = Color.Transparent,
          activeColor = MaterialTheme.colors.onSurface,
          modifier = Modifier.padding(2.dp)
        )
      } else {
        Box(Modifier.debugBorder())
      }

      IconButton(
        onClick = { currentScreen.value = Screen.New }, modifier = Modifier.debugBorder()
      ) {
        Text(text = "Add", style = MaterialTheme.typography.body2)
      }
    }
  )
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
