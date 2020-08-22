package simple.payment.tracker.compose

import androidx.compose.Composable
import androidx.compose.MutableState
import androidx.compose.state
import androidx.ui.animation.Crossfade
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.Text
import androidx.ui.foundation.drawBackground
import androidx.ui.foundation.drawBorder
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.input.TextFieldValue
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.padding
import androidx.ui.material.BottomAppBar
import androidx.ui.material.FilledTextField
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Scaffold
import androidx.ui.material.Surface
import androidx.ui.material.TopAppBar
import androidx.ui.unit.dp
import org.koin.core.context.KoinContextHandler
import simple.payment.tracker.Transaction
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
fun PaymentsApp() {
  PaymentsTheme {
    AppContent()
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
private fun AppContent() {
  val currentScreen: MutableState<Screen> = state<Screen> { Screen.List }
  KoinContextHandler.get().get<Backs>()
    .backPressed
    .commitSubscribe {
      currentScreen.value = Screen.List
    }

  val search = state { TextFieldValue("") }
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
            is Screen.List -> ListScreen(false, currentScreen, search)
            is Screen.ListAll -> ListScreen(true, currentScreen, search)
            is Screen.Details -> DetailsScreen(scr.transaction, currentScreen)
            is Screen.New -> DetailsScreen(null, currentScreen)
            is Screen.Monthly -> MonthlyScreen()
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
  return drawBorder(size = 1.dp, color = borderColors[nextInt(0, borderColors.lastIndex)])
}

@Composable
private fun NavigationTopBar(
  search: MutableState<TextFieldValue>,
  currentScreen: MutableState<Screen>
) {
  TopAppBar(
    content = {
      if (currentScreen.value is Screen.ListAll) {
        FilledTextField(
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
      drawBackground(
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp)
      )
    }
    else -> this
  }
}
