package simple.payment.tracker.compose

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
import simple.payment.tracker.Icon
import simple.payment.tracker.R
import simple.payment.tracker.theme.Theme

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

    IconButton(
        onClick = { currentScreen.value = Screen.Settings },
        modifier = Modifier.highlightIf(currentScreen, Screen.Settings)) {
      Icon(
          id = R.drawable.ic_baseline_more_vert_24,
          tint = if (colors.isLight) colors.onPrimary else colors.onSurface,
      )
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
