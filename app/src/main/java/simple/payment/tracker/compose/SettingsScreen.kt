package simple.payment.tracker.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import simple.payment.tracker.BuildConfig
import simple.payment.tracker.Settings
import simple.payment.tracker.theme.ExtendedColors
import simple.payment.tracker.theme.themeColors
import simple.payment.tracker.theme.themeTypography

@Composable
fun SettingsScreen(settings: DataStore<Settings>) {
  Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    val coroutineScope = rememberCoroutineScope()

    Text(text = "Version", style = typography.h6)
    Text(text = BuildConfig.VERSION_NAME)
    Text(text = "Theme", style = typography.h6)
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
    ) {
      themeColors().forEach { (name, colors) ->
        ThemeSelector(
            // TODO launched effect?
            onClick = { coroutineScope.launch { settings.updateData { it.copy(theme = name) } } },
            text = name,
            colors = colors)
      }
    }
    Divider()
    val device = remember { settings.data.map { it.deviceName } }.collectAsState("")
    OutlinedTextField(
        label = { Text(text = "Device", style = typography.body1) },
        value = device.value,
        onValueChange = {
          coroutineScope.launch { settings.updateData { prev -> prev.copy(deviceName = it) } }
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = typography.body1,
    )
    val trip = remember { settings.data.map { it.trip } }.collectAsState("")
    OutlinedTextField(
        label = { Text(text = "Trip", style = typography.body1) },
        value = trip.value,
        onValueChange = {
          coroutineScope.launch { settings.updateData { prev -> prev.copy(trip = it) } }
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = typography.body1,
    )
  }
}

@Composable
private fun ThemeSelector(onClick: () -> Unit, text: String, colors: ExtendedColors) {
  TextButton(
      onClick,
      modifier =
          Modifier.padding(4.dp)
              .background(
                  color = colors.background,
                  shape = CircleShape,
              )) {
    Text(
        text,
        style = themeTypography.button,
        color = colors.textAccent,
    )
  }
}
