package simple.payment.tracker.compose

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random.Default.nextInt

val borderColors =
    listOf(
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
        Color.Transparent)

fun Modifier.debugBorder(): Modifier = composed {
  // if (false) {
  border(width = 1.dp, color = borderColors[nextInt(0, borderColors.lastIndex)])
  // }
  // this
}
