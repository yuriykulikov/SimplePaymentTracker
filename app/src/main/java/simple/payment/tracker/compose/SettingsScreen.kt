package simple.payment.tracker.compose

import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Colors
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import simple.payment.tracker.Settings
import simple.payment.tracker.stores.DataStore
import simple.payment.tracker.stores.modify
import simple.payment.tracker.theme.DarkThemeColors
import simple.payment.tracker.theme.DeusExThemeColors
import simple.payment.tracker.theme.LightThemeColors
import simple.payment.tracker.theme.SynthwaveThemeColors
import simple.payment.tracker.theme.themeTypography

@Composable
fun SettingsScreen(settings: DataStore<Settings>) {
  Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    Text(text = "Theme", style = MaterialTheme.typography.h5)
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
      ) {
      ThemeSelector(
        { settings.modify { it.copy(theme = "LightThemeColors") } },
        "Light",
        LightThemeColors
      )
      ThemeSelector(
        { settings.modify { it.copy(theme = "DarkThemeColors") } },
        "Dark",
        DarkThemeColors
      )
      ThemeSelector(
        { settings.modify { it.copy(theme = "DeusExThemeColors") } },
        "Deus Ex",
        DeusExThemeColors
      )
      ThemeSelector(
        { settings.modify { it.copy(theme = "SynthwaveThemeColors") } },
        "Synth",
        SynthwaveThemeColors
      )
    }
    Divider()
  }
}

@Composable
private fun ThemeSelector(onClick: () -> Unit, text: String, colors: Colors) {
  TextButton(
    onClick, modifier = Modifier
      .padding(4.dp)
      .background(
        color = colors.background,
        shape = CircleShape,
      )
  ) {
    Text(
      text,
      style = themeTypography.button,
      color = colors.onBackground,
    )
  }
}