package simple.payment.tracker

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.loadVectorResource

@Composable
fun LoadingVectorImage(id: Int, modifier: Modifier = Modifier, tint: Color?) {
  loadVectorResource(id = id)
    .resource.resource?.let {
      Image(asset = it, modifier = modifier, colorFilter = tint?.let { ColorFilter.tint(it) })
    }
}