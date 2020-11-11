package simple.payment.tracker.compose

import androidx.compose.foundation.Box
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import simple.payment.tracker.Payment
import simple.payment.tracker.PaymentsRepository
import simple.payment.tracker.Transaction
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat(
  "dd-MM-yy HH:mm",
  Locale.GERMANY
)

/**
 * Transaction can be with a notification or without
 */
@Composable
fun DetailsScreen(
  paymentsRepository: PaymentsRepository,
  transaction: Transaction?,
  onSave: () -> Unit
) {
  val category: MutableState<String?> = remember {
    mutableStateOf(transaction?.category)
  }
  val sum: MutableState<TextFieldValue> = remember {
    mutableStateOf(TextFieldValue(transaction?.sum?.toString() ?: ""))
  }
  val merchant: MutableState<TextFieldValue> = remember {
    mutableStateOf(TextFieldValue(transaction?.merchant ?: ""))
  }
  val time: MutableState<TextFieldValue> = remember {
    val initialTime = transaction?.time?.let { Instant.ofEpochMilli(it) } ?: Instant.now()
    mutableStateOf(
      TextFieldValue(dateFormat.format(Date.from(initialTime)))
    )
  }

  val cancelled = remember { mutableStateOf(transaction?.cancelled ?: false) }
  val comment = remember { mutableStateOf(TextFieldValue(transaction?.comment ?: "")) }
  val trip = remember { mutableStateOf(TextFieldValue(transaction?.trip ?: "")) }

  val strike = MaterialTheme.typography.body1.copy(textDecoration = TextDecoration.LineThrough)

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "${sum.value.text} to ${merchant.value.text} ${if (cancelled.value) " (cancelled)" else ""}",
            style = if (cancelled.value) strike else MaterialTheme.typography.body1
          )
        },
        actions = {
          val canSave = (runCatching<Date?> { dateFormat.parse(time.value.text) }.isSuccess
            && sum.value.text.toIntOrNull() != null
            && merchant.value.text.isNotEmpty()
            && category.value != null)
          IconButton(onClick = {
            if (canSave) ({
              paymentsRepository
                .changeOrCreatePayment(
                  transaction?.id,
                  Payment(
                    notificationId = transaction?.payment?.notificationId
                      ?: transaction?.notification?.time,
                    time = transaction?.notification?.time
                      ?: requireNotNull(
                        dateFormat.parse(time.value.text)
                      ).time,
                    category = category.value!!,
                    comment = comment.value.text,
                    merchant = merchant.value.text,
                    sum = sum.value.text.toInt(),
                    cancelled = cancelled.value,
                    trip = trip.value.text.let { if (it.isEmpty()) null else it }
                  )
                )
              onSave()
            })()
          }) {
            Text(
              text = "Save",
              style = MaterialTheme.typography.body2,
              modifier = Modifier.drawOpacity(if (canSave) 1f else 0.2f)
            )
          }
        }
      )
    },
    bodyContent = {
      Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.TopCenter)) {
        Column {
          ScrollableColumn {
            Column {
              Row {
                NamedTextFieldInput(
                  header = "€",
                  state = sum,
                  keyboardType = KeyboardType.Number,
                  modifier = Modifier.fillMaxWidth(0.8f).padding(horizontal = 16.dp),
                  onValueChange = {
                    if (it.text.toIntOrNull() != null || it.text.isEmpty()) {
                      sum.value = it
                    }
                  }
                )

                Checkbox(
                  modifier = Modifier.debugBorder().padding(16.dp),
                  checked = !cancelled.value,
                  onCheckedChange = { cancelled.value = !cancelled.value }
                )
              }
            }
            Column {
              NamedTextFieldInput(header = "to", state = merchant)
              NamedTextFieldInput(header = "for", state = comment)
              Row {
                NamedTextFieldInput(
                  header = "on",
                  state = time,
                  enabled = transaction?.notification == null,
                  modifier = Modifier.weight(0.5f).padding(start = 16.dp),
                )
                NamedTextFieldInput(
                  header = "Trip",
                  state = trip,
                  modifier = Modifier.weight(0.5f).padding(end = 16.dp).padding(start = 8.dp),
                )
              }
              InputDivider()
              CategorySelector(category)
            }
          }
        }
      }
    }
  )
}

@Composable
fun InputDivider() {
  Divider(
    color = MaterialTheme.colors.onSurface.copy(alpha = 0.08f)
  )
}

/** Header and TextField to input text */
@Composable
private fun NamedTextFieldInput(
  header: String,
  state: MutableState<TextFieldValue>,
  keyboardType: KeyboardType = KeyboardType.Text,
  enabled: Boolean = true,
  modifier: Modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
  onValueChange: (TextFieldValue) -> Unit = { state.value = it }
) {
  OutlinedTextField(
    label = {
      Text(
        header,
        style = MaterialTheme.typography.body1
      )
    },
    value = state.value,
    onValueChange = if (enabled) onValueChange else { _ -> },
    keyboardType = keyboardType,
    modifier = modifier,
    textStyle = MaterialTheme.typography.body1,
    //backgroundColor = Color.Transparent,
  )
}

/** Category tiles in 2 columns */
@Composable
private fun CategorySelector(category: MutableState<String?>) {
  categories.toList().chunked(2).forEach { chunk ->
    Row {
      chunk.forEach {
        Column(
          modifier = Modifier
            .weight(0.5F)
            .clickable(onClick = { category.value = it })
        ) {
          Text(
            it,
            color = if (it == category.value) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground,
            style = MaterialTheme.typography.button,
            modifier = Modifier.padding(16.dp)
          )
        }
      }
    }
  }
}

private val categories = arrayOf(
  "Еда",
  "Ресторан",
  "Гедонизм",
  "Транспорт",
  "Для дома",
  "Снаряга",
  "Развлечения",
  "Baby",
  "Подарки",
  "Машина",
  "Косметика",
  "Кот",
  "Одежда и вещи",
  "Разное",
  "Аптека",
  "Девайсы",
  "Хобби",
  "Путешествия",
  "Проживание",
  "Образование",
  "Бытовая химия",
  "Дурость",
  "Парикмахер",
  "Зубной",
  "Линзы",
  "Квартира",
  "Помощь родителям"
)
