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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val themeTypography = themeTypography()

fun themeTypography(fontFamily: FontFamily = FontFamily.Default): Typography {
  return Typography(
      defaultFontFamily = fontFamily,
      h4 = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 30.sp, letterSpacing = 0.sp),
      h5 = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, letterSpacing = 0.sp),
      h6 = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, letterSpacing = 0.sp),
      subtitle1 =
          TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, letterSpacing = 0.15.sp),
      subtitle2 =
          TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 0.1.sp),
      body1 =
          TextStyle(
              fontFamily = fontFamily,
              fontWeight = FontWeight.Normal,
              fontSize = 16.sp,
              letterSpacing = 0.5.sp),
      body2 = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 0.25.sp),
      button =
          TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 1.25.sp),
      caption = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.4.sp),
      overline =
          TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 1.sp))
}
