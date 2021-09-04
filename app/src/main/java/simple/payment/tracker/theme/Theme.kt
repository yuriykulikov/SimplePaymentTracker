package simple.payment.tracker.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColoredTheme(colors: Colors, content: @Composable() () -> Unit) {
  MaterialTheme(colors = colors, typography = themeTypography, shapes = shapes, content = content)
}

fun themeColors() =
    listOf(
        "Light" to LightColors,
        "Dark" to DarkColors,
        "Deus" to DeusExColors,
        "Synth" to SynthwaveColors,
        "Deep" to DeepColors,
    )

fun String.toColors(): Colors {
  return when (this) {
    "Light" -> LightColors
    "Dark" -> DarkColors
    "DeusEx" -> DeusExColors
    "Synth" -> SynthwaveColors
    "Deep" -> DeepColors
    else -> SynthwaveColors
  }
}

private val shapes =
    Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(8.dp))

private val blackish = Color(0xff1c1e20)
private val grey = Color(0xffaaaaaa)
private val whitish = Color(0xffdddddd)
private val white = Color(0xffeeeeee)
private val dark_grey = Color(0xff333333)
private val transparent = Color(0x00000000)
private val transparent_white = Color(0x66ffffff)
private val mat_purple_black = Color(0xFF170017)
private val mat_purple_dark = Color(0xFF332940)
private val mat_purple_lighter = Color(0xFF5D4B70)
private val mat_purple = Color(0xFFBB86FC)
private val mat_blue_purple_primary = Color(0xFF6200EE)
private val mat_blue_primary_variant = Color(0xFF3700B3)
private val mat_teal_secondary = Color(0xFF03DAC6)
private val mat_teal_secondary_variant = Color(0xFF018786)
private val mat_almost_black = Color(0xFF121212)
private val mat_teal_ws = Color(0xFF4EAC9C)
private val mat_almost_black_ws = Color(0xFF131D23)
private val mat_grey_ws = Color(0xFF242D35)
private val mat_purple_ws = Color(0xFFAE7AC2)
private val reactive_purple = Color(0xffee0e90)
private val reactive_background = Color(0xff2b323b)
private val reactive_surface = Color(0xff3f3d55)
private val reactive_white = Color(0xffffffe8)

private val LightColors = lightColors()
private val DarkColors =
    darkColors(
        onBackground = whitish,
        onSurface = whitish,
        surface = mat_grey_ws,
        background = mat_almost_black_ws,
        secondary = mat_teal_ws,
        primary = mat_teal_ws,
        primaryVariant = mat_teal_ws,
    )

private val DeusExColors =
    darkColors(
        primary = Color(0xffd0902f),
        primaryVariant = Color(0xffa15501),
        secondary = Color(0xffd0902f),
        background = blackish,
        surface = dark_grey,
        error = Color.Red,
        onPrimary = dark_grey,
        onSecondary = dark_grey,
        onBackground = whitish,
        onSurface = whitish,
        onError = Color.Black,
    )

private val SynthwaveColors =
    darkColors(
        background = mat_almost_black,
        surface = mat_purple_black,
        onBackground = whitish,
        onSurface = whitish,
        primary = mat_blue_primary_variant,
        primaryVariant = mat_purple,
        secondary = mat_purple,
        error = Color(0xFFCF6679),
    )

private val DeepColors =
    darkColors(
        background = Color(0xFF17202A),
        onBackground = whitish,
        surface = Color(0xFF151D27),
        onSurface = whitish,
        primary = Color(0xFF4D9EEB),
        primaryVariant = Color(0xFF4D9EEB),
        secondary = Color(0xFF4D9EEB),
    )
