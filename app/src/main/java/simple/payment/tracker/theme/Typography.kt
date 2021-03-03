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

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import simple.payment.tracker.R

private val regular = Font(R.font.montserrat_regular)
private val medium = Font(R.font.montserrat_medium, FontWeight.W500)
private val semibold = Font(R.font.montserrat_semibold, FontWeight.W600)

private val appFontFamily = FontFamily(
  fonts = listOf(
    regular,
    medium,
    semibold
  )
)

val themeTypography = Typography(
  h4 = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.W600,
    fontSize = 30.sp
  ),
  h5 = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.W600,
    fontSize = 24.sp
  ),
  h6 = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.W600,
    fontSize = 20.sp
  ),
  subtitle1 = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.W600,
    fontSize = 16.sp
  ),
  subtitle2 = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.W500,
    fontSize = 14.sp
  ),
  body1 = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp
  ),
  body2 = TextStyle(
    fontFamily = appFontFamily,
    fontSize = 14.sp
  ),
  button = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.W500,
    fontSize = 14.sp
  ),
  caption = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp
  ),
  overline = TextStyle(
    fontFamily = appFontFamily,
    fontWeight = FontWeight.W500,
    fontSize = 12.sp
  )
)
