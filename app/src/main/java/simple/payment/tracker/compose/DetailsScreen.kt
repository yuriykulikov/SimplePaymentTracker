package simple.payment.tracker.compose

import androidx.compose.Composable
import androidx.compose.MutableState
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.drawOpacity
import androidx.ui.foundation.Box
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.clickable
import androidx.ui.input.KeyboardType
import androidx.ui.input.TextFieldValue
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.text.style.TextDecoration
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import org.koin.core.context.KoinContextHandler
import simple.payment.tracker.Payment
import simple.payment.tracker.PaymentsRepository
import simple.payment.tracker.Transaction
import simple.payment.tracker.theme.PaymentsTheme
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

private val dateFormat = SimpleDateFormat(
  "dd-MM-yy HH:mm",
  Locale.GERMANY
)

@Preview("DetailsScreen preview")
@Composable
fun PreviewDetailsScreen() {
  PaymentsTheme {
    Surface {
      val transaction = Transaction(
        Payment(
          category = "Baby",
          comment = "Comment",
          merchant = "Amazon",
          notificationId = null,
          time = 100500L,
          sum = 101,
          cancelled = false,
          trip = null
        )
      )
      DetailsScreen(
        transaction,
        state { Screen.Details(transaction) }
      )
    }
  }
}

class DetailsScreenState(
  val category: MutableState<String?>,
  val cancelled: MutableState<Boolean>,
  val comment: MutableState<TextFieldValue>,
  val sum: MutableState<TextFieldValue>,
  val merchant: MutableState<TextFieldValue>,
  val trip: MutableState<TextFieldValue>,
  val time: MutableState<TextFieldValue>
) {
  companion object {
    @Composable
    fun create(): DetailsScreenState {
      return DetailsScreenState(
        category = state { null },
        cancelled = state { false },
        comment = state { TextFieldValue("") },
        sum = state { TextFieldValue("") },
        merchant = state { TextFieldValue("") },
        trip = state { TextFieldValue("") },
        time = state {
          TextFieldValue(dateFormat.format(Date.from(Instant.now())))
        }
      )
    }

    @Composable
    fun fromTransaction(transaction: Transaction): DetailsScreenState {
      return DetailsScreenState(
        category = state { transaction.category },
        cancelled = state { transaction.cancelled },
        comment = state { TextFieldValue(transaction.comment) },
        sum = state { TextFieldValue(transaction.sum.toString()) },
        merchant = state { TextFieldValue(transaction.merchant) },
        trip = state { TextFieldValue(transaction.trip ?: "") },
        time = state {
          val initialTime = transaction.time.let { Instant.ofEpochMilli(it) }
          TextFieldValue(dateFormat.format(Date.from(initialTime)))
        }
      )
    }
  }
}

/**
 * Transaction can be with a notification or without
 */
@Composable
fun DetailsScreen(
  transaction: Transaction?,
  currentScreen: MutableState<Screen>
) {
  val state =
    if (transaction != null) DetailsScreenState.fromTransaction(transaction) else DetailsScreenState.create()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          with(state) {
            Text(
              text = "${sum.value.text} to ${merchant.value.text}",
              style =
              MaterialTheme.typography.body1.copy(
                textDecoration = if (cancelled.value) TextDecoration.LineThrough else TextDecoration.None
              )
            )
          }
        },
        actions = {
          Actions(
            state, save = {
              with(state) {
                KoinContextHandler.get().get<PaymentsRepository>()
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
              }
            },
            currentScreen = currentScreen
          )
        }
      )
    },
    bodyContent = {
      Box(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.TopCenter)) {
        Column {
          VerticalScroller {
            Column {
              TextInputs(state, transaction?.notification != null)
              CategorySelector(state.category)
            }
          }
        }
      }
    }
  )
}

@Composable
private fun Actions(
  state: DetailsScreenState,
  currentScreen: MutableState<Screen>,
  save: () -> Unit
) {
  IconButton(onClick = { state.cancelled.value = !state.cancelled.value }) {
    Text(
      text = if (state.cancelled.value) "X" else "X",
      style = MaterialTheme.typography.body2
    )
  }

  val canSave = runCatching { dateFormat.parse(state.time.value.text) }.isSuccess
    && state.sum.value.text.toIntOrNull() != null
    && state.merchant.value.text.isNotEmpty()
    && state.category.value != null
  IconButton(onClick = {
    if (canSave) {
      currentScreen.value = Screen.List
      save()
    }
  }) {
    Text(
      text = "Save",
      style = MaterialTheme.typography.body2,
      modifier = Modifier.drawOpacity(if (canSave) 1f else 0.2f)
    )
  }
}

/** Inputs for a new payment */
@Composable
private fun TextInputs(
  state: DetailsScreenState,
  fromNotfication: Boolean
) {
  NamedTextFieldInput(
    header = "€",
    state = state.sum,
    keyboardType = KeyboardType.Number,
    onValueChange = {
      if (it.text.toIntOrNull() != null || it.text.isEmpty()) {
        state.sum.value = it
      }
    }
  )
  NamedTextFieldInput(header = "to", state = state.merchant)
  NamedTextFieldInput(header = "for", state = state.comment)
  NamedTextFieldInput(header = "on", state = state.time, enabled = !fromNotfication)
  NamedTextFieldInput(header = "Trip", state = state.trip)
  InputDivider()
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
  onValueChange: (TextFieldValue) -> Unit = { state.value = it }
) {
  Row {
    Column {
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        textStyle = MaterialTheme.typography.body1
      )
    }
  }
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
