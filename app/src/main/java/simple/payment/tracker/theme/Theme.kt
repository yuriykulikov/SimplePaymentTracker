/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package simple.payment.tracker.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightThemeColors = lightColors()
val DarkThemeColors = darkColors()

val Gold0 = Color(0xfffff69f)
val Gold1 = Color(0xfffdd870)
val Gold2 = Color(0xffd0902f)
val Gold3 = Color(0xffa15501)
val Gold4 = Color(0xff351409)

val DeusExThemeColors = darkColors(
  primary = Gold2,
  primaryVariant = Gold3,
  secondary = Gold3,
  background = Color.Black,
  surface = Color.Black,
  error = Gold0,
  onPrimary = Color.DarkGray,
  onSecondary = Color.DarkGray,
  onBackground = Gold1,
  onSurface = Gold1,
  onError = Color.Black,
)

val SynthPurpleWithDarkOverlay = Color(0xff332940)
val SynthPurpleWithDarkOverlayOriginal = Color(0xff5D4B70)
val SynthPurple = Color(0xffBB86FC)
val SynthwaveThemeColors = darkColors(
  background = SynthPurpleWithDarkOverlay,
  onBackground = SynthPurple,
)

@Composable
fun PaymentsTheme(
  colors: Colors,
  content: @Composable() () -> Unit
) {
  MaterialTheme(
    colors = colors,
    typography = themeTypography,
    shapes = shapes,
    content = content
  )
}
