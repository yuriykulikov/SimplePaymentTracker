package simple.payment.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp
import simple.payment.tracker.theme.Theme

@Deprecated("Use function without mutablestate")
@Composable
fun NavigationBottomBar(currentScreen: MutableState<Screen>) {
  BottomAppBar(
      modifier = Modifier.fillMaxWidth(),
      backgroundColor = Theme.colors.bottomBar,
  ) {
    IconButton(
        onClick = { currentScreen.value = Screen.List },
        modifier = Modifier.weight(1f).highlightIf(currentScreen, Screen.List)) {
          Text(text = "Inbox")
        }

    IconButton(
        onClick = { currentScreen.value = Screen.ListAll },
        modifier = Modifier.weight(1f).highlightIf(currentScreen, Screen.ListAll)) {
          Text(text = "All")
        }

    IconButton(
        onClick = { currentScreen.value = Screen.Monthly },
        modifier = Modifier.weight(1f).highlightIf(currentScreen, Screen.Monthly)) {
          Text(text = "Stats")
        }

    IconButton(
        onClick = { currentScreen.value = Screen.Trips },
        modifier = Modifier.weight(1f).highlightIf(currentScreen, Screen.Trips)) {
          Text(text = "Trips")
        }
  }
}

private fun Modifier.highlightIf(currentScreen: MutableState<Screen>, target: Screen): Modifier =
    composed {
      when (currentScreen.value) {
        target -> {
          background(color = colors.onSurface.copy(alpha = 0.1f), shape = RoundedCornerShape(20.dp))
        }
        else -> this
      }
    }

@Composable
fun NavigationBottomBar(currentScreen: Screen?, onChange: (Screen) -> Unit) {
  BottomAppBar(
      modifier = Modifier.fillMaxWidth(),
      backgroundColor = Theme.colors.bottomBar,
  ) {
    IconButton(
        onClick = { onChange(Screen.List) },
        modifier = Modifier.weight(1f).highlightIf(currentScreen, Screen.List)) {
          Text(text = "Inbox")
        }

    IconButton(
        onClick = { onChange(Screen.ListAll) },
        modifier = Modifier.weight(1f).highlightIf(currentScreen, Screen.ListAll)) {
          Text(text = "All")
        }

    IconButton(
        onClick = { onChange(Screen.Monthly) },
        modifier = Modifier.weight(1f).highlightIf(currentScreen, Screen.Monthly)) {
          Text(text = "Stats")
        }

    IconButton(
        onClick = { onChange(Screen.Trips) },
        modifier = Modifier.weight(1f).highlightIf(currentScreen, Screen.Trips)) {
          Text(text = "Trips")
        }
  }
}

private fun Modifier.highlightIf(currentScreen: Screen?, target: Screen): Modifier = composed {
  when (currentScreen) {
    target -> {
      background(color = colors.onSurface.copy(alpha = 0.1f), shape = RoundedCornerShape(20.dp))
    }
    else -> this
  }
}
