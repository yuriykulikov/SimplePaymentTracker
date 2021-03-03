package simple.payment.tracker.compose


import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import simple.payment.tracker.Icon
import simple.payment.tracker.Payment
import simple.payment.tracker.PaymentsRepository
import simple.payment.tracker.R
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


  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = "Payment") },
        actions = {
          val canSave = (runCatching<Date?> { dateFormat.parse(time.value.text) }.isSuccess
            && sum.value.text.toIntOrNull() != null
            && merchant.value.text.isNotEmpty()
            && !category.value.isNullOrEmpty())
          IconButton(onClick = {
            if (canSave) {
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
            }
          }) {
            Icon(
              modifier = Modifier.alpha(if (canSave) 1f else 0.2f),
              painter = painterResource(id = R.drawable.ic_baseline_done_24),
              contentDescription = null,
              tint = colors.primary,
            )
          }
        }
      )
    },
    content = {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .wrapContentSize(Alignment.TopCenter)
      ) {
        Column {
          Column(modifier = Modifier.verticalScroll(ScrollState(0))) {
            Column(
              modifier = Modifier.padding(horizontal = 16.dp)
            ) {
              Row {
                NamedTextFieldInput(
                  leadingIcon = {
                    Icon(
                      id = R.drawable.ic_baseline_today_24,
                      tint = colors.onSurface
                    )
                  },
                  state = time,
                  enabled = transaction?.notification == null,
                  modifier = Modifier.weight(0.5f),
                )
                NamedTextFieldInput(
                  header = "Trip",
                  leadingIcon = {
                    Icon(
                      id = R.drawable.ic_baseline_map_24,
                      tint = colors.onSurface
                    )
                  },
                  state = trip,
                  modifier = Modifier
                    .weight(0.5f)
                    .padding(start = 8.dp),
                )
              }
              NamedTextFieldInput(
                header = "€",
                state = sum,
                keyboardType = KeyboardType.Number,
                onValueChange = {
                  if (it.text.toIntOrNull() != null || it.text.isEmpty()) {
                    sum.value = it
                  }
                },
              )
              NamedTextFieldInput(header = "to", state = merchant)

              OutlinedTextField(
                label = { Text(text = "for", style = typography.body1) },
                value = comment.value,
                onValueChange = { comment.value = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = typography.body1,
              )
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
    color = colors.onSurface.copy(alpha = 0.08f)
  )
}

/** Header and TextField to input text */
@Composable
private fun NamedTextFieldInput(
  modifier: Modifier = Modifier,
  header: String? = null,
  state: MutableState<TextFieldValue>,
  keyboardType: KeyboardType = KeyboardType.Text,
  enabled: Boolean = true,
  onValueChange: (TextFieldValue) -> Unit = { state.value = it },
  leadingIcon: @Composable (() -> Unit)? = null,
) {
  TextField(
    label = header?.let { { Text(it, style = typography.overline) } },
    value = state.value,
    onValueChange = if (enabled) onValueChange else { _ -> },
    leadingIcon = leadingIcon,
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    modifier = modifier.fillMaxWidth(),
    textStyle = typography.body1,
//backgroundColor = Color.Transparent,
  )
}

/** Category tiles in 2 columns */
@Composable
private fun CategorySelector(selected: MutableState<String?>) {
  categories.toList().chunked(2).forEach { chunk ->
    Row {
      chunk.forEach { category ->
        Column(
          modifier = Modifier
            .weight(0.5F)
            .clickable(onClick = { selected.value = category })
        ) {
          Text(
            category,
            color = if (category == selected.value) colors.onSecondary else colors.onBackground,
            style = typography.button,
            modifier = Modifier
              .padding(8.dp)
              .then(
                if (category == selected.value) Modifier.background(
                  colors.secondary,
                  CircleShape
                ) else Modifier
              )
              .padding(8.dp),
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
