package simple.payment.tracker.compose

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import simple.payment.tracker.Payment

fun Payment.annotatedSumWithRefunds(): AnnotatedString {
  return if (refunds.isEmpty()) {
    AnnotatedString("$sum")
  } else {
    buildAnnotatedString {
      append("$sum ")
      pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
      append("$initialSum")
    }
  }
}
