package simple.payment.tracker.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LocalExtendedColors = staticCompositionLocalOf { NeonColors }

// Use with eg. ExtendedTheme.colors.tertiary
object Theme {
  val colors: ExtendedColors
    @Composable get() = LocalExtendedColors.current
}

@Composable
fun ColoredTheme(
    extendedColors: ExtendedColors,
    typography: Typography = themeTypography,
    content: @Composable () -> Unit
) {
  CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
    MaterialTheme(
        colors = extendedColors.material(),
        typography = typography,
        shapes = shapes,
        content = content)
  }
}

@Immutable
data class ExtendedColors(
    val dark: Boolean,
    val background: Color,
    val text: Color,
    val textAccent: Color,
    val surfaceAccent: Color,
    val bar: Color,
) {
  val bottomBar: Color = bar
  val topBar: Color = bar
}

fun ExtendedColors.material(): Colors {
  return if (dark) {
    darkColors(
        background = background,
        onBackground = text,
        surface = background,
        onSurface = text,
        // ?
        primary = surfaceAccent,
        primaryVariant = textAccent,
        // ?
        secondary = surfaceAccent,
        secondaryVariant = textAccent,
    )
  } else {
    lightColors(
        background = background,
        onBackground = text,
        surface = background,
        onSurface = text,
        // ?
        primary = surfaceAccent,
        primaryVariant = textAccent,
        // ?
        secondary = surfaceAccent,
        secondaryVariant = textAccent,
    )
  }
}

fun themeColors() =
    listOf(
        "Light" to LightColors,
        "Neon" to NeonColors,
        "DeusEx" to DeusExColors,
        "Synth" to SynthwaveColors,
        "Deep" to DeepColors,
    )

fun String.toColors(): ExtendedColors {
  return when (this) {
    "Light" -> LightColors
    "Neon" -> NeonColors
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

private val LightColors =
    ExtendedColors(
        dark = false,
        background = grey,
        bar = blackish,
        surfaceAccent = mat_blue_purple_primary,
        text = mat_almost_black,
        textAccent = mat_teal_secondary_variant,
    )

private val NeonColors =
    ExtendedColors(
        dark = true,
        background = mat_almost_black_ws,
        bar = mat_grey_ws,
        surfaceAccent = mat_teal_ws,
        text = whitish,
        textAccent = mat_teal_ws,
    )

private val DeusExColors =
    ExtendedColors(
        dark = true,
        background = blackish,
        bar = dark_grey,
        surfaceAccent = Color(0xffa15501),
        text = whitish,
        textAccent = Color(0xffd0902f),
    )

private val SynthwaveColors =
    ExtendedColors(
        dark = true,
        background = blackish,
        bar = mat_purple_dark,
        surfaceAccent = mat_purple,
        text = whitish,
        textAccent = mat_purple,
    )

private val DeepColors =
    ExtendedColors(
        dark = true,
        background = Color(0xff1d2733),
        bar = Color(0xff202d3b),
        surfaceAccent = Color(0xff5fa3de),
        text = whitish,
        textAccent = Color(0xff5fa3de),
    )
