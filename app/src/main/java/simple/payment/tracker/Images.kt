package simple.payment.tracker

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

@Composable
fun Icon(id: Int, modifier: Modifier = Modifier, tint: Color) {
  Icon(
    modifier = modifier,
    painter = painterResource(id),
    contentDescription = null,
  )
}