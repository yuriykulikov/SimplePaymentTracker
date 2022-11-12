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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import simple.payment.tracker.Icon
import simple.payment.tracker.Payment
import simple.payment.tracker.PaymentsRepository
import simple.payment.tracker.R
import simple.payment.tracker.Settings
import simple.payment.tracker.Transaction
import simple.payment.tracker.logging.Logger
import simple.payment.tracker.theme.Theme

private val dateFormat = SimpleDateFormat("dd-MM-yy HH:mm", Locale.GERMANY)

/** Transaction can be with a notification or without */
@Composable
fun DetailsScreen(
    paymentsRepository: PaymentsRepository,
    transaction: Transaction?,
    onSave: () -> Unit,
    settings: DataStore<Settings>,
    logger: Logger,
) {
  LaunchedEffect(transaction) {
    logger.debug {
      // Showing details for Transaction(payment=Payment(id=1644090240000,
      // notificationId=1644090241414, date=Sat Feb 05 20:44:00 GMT+01:00 2022, sum=61,
      // category='Ресторан', comment='', merchant='Namaste Indisches Rest', trip=null),
      // notification=null)
      "Showing details for $transaction"
    }
  }

  var category: String? by remember { mutableStateOf(transaction?.category) }
  var sum: TextFieldValue by remember {
    mutableStateOf(TextFieldValue(transaction?.sum?.toString() ?: ""))
  }
  var merchant: TextFieldValue by remember {
    mutableStateOf(TextFieldValue(transaction?.merchant ?: ""))
  }
  var time: TextFieldValue by remember {
    val initialTime = transaction?.time?.let { Instant.ofEpochMilli(it) } ?: Instant.now()
    mutableStateOf(TextFieldValue(dateFormat.format(Date.from(initialTime))))
  }

  var comment by remember { mutableStateOf(TextFieldValue(transaction?.comment ?: "")) }

  var trip by remember { mutableStateOf(TextFieldValue("")) }

  LaunchedEffect(transaction) {
    val initialTripValue =
        when {
          transaction?.payment == null -> settings.data.first().trip
          transaction.payment.trip != null -> transaction.payment.trip
          else -> ""
        }
    trip = TextFieldValue(initialTripValue)
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(text = "Payment") },
            backgroundColor = Theme.colors.topBar,
            actions = {
              val scope = rememberCoroutineScope()
              val canSave =
                  (runCatching<Date?> { dateFormat.parse(time.text) }.isSuccess &&
                      sum.text.toIntOrNull() != null &&
                      merchant.text.isNotEmpty() &&
                      !category.isNullOrEmpty())
              IconButton(
                  onClick = {
                    if (canSave) {
                      scope.launch {
                        paymentsRepository.changeOrCreatePayment(
                            transaction?.id,
                            // here the issue is that notificaiton is null for some reason when
                            // changing the existing payment
                            // changeOrCreatePayment(previousId: 1644090240000, payment:
                            // Payment(id=1644090240000, notificationId=1644090241414, date=Sat Feb
                            // 05 20:44:00 GMT+01:00 2022, sum=60, category='Ресторан', comment='',
                            // merchant='Namaste Indisches Rest', trip=null))
                            Payment(
                                notificationId = transaction?.payment?.notificationId
                                        ?: transaction?.notification?.time,
                                time = transaction?.notification?.time
                                        ?: requireNotNull(dateFormat.parse(time.text)).time,
                                category = category!!,
                                comment = comment.text,
                                merchant = merchant.text,
                                sum = sum.text.toInt(),
                                trip = trip.text.let { if (it.isEmpty()) null else it }))
                      }
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
            })
      },
      content = { paddingValues ->
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .wrapContentSize(Alignment.TopCenter)
                    .padding(paddingValues)) {
          Column {
            Column(modifier = Modifier.verticalScroll(ScrollState(0))) {
              Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row {
                  NamedTextFieldInput(
                      leadingIcon = {
                        Icon(id = R.drawable.ic_baseline_today_24, tint = colors.onSurface)
                      },
                      value = time,
                      onValueChange = { time = it },
                      enabled = transaction?.notification == null,
                      modifier = Modifier.weight(0.5f),
                  )
                  NamedTextFieldInput(
                      header = "Trip",
                      leadingIcon = {
                        Icon(id = R.drawable.ic_baseline_map_24, tint = colors.onSurface)
                      },
                      value = trip,
                      onValueChange = { trip = it },
                      modifier = Modifier.weight(0.5f).padding(start = 8.dp),
                  )
                }
                NamedTextFieldInput(
                    header = "€",
                    value = sum,
                    keyboardType = KeyboardType.Number,
                    onValueChange = {
                      if (it.text.toIntOrNull() != null || it.text.isEmpty()) {
                        sum = it
                      }
                    },
                )

                NamedTextFieldInput(
                    header = "to", value = merchant, onValueChange = { merchant = it })

                OutlinedTextField(
                    label = { Text(text = "for", style = typography.body1) },
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = typography.body1,
                )
                InputDivider()
                CategorySelector(selected = category, select = { category = it })
              }
            }
          }
        }
      })
}

@Composable
fun InputDivider() {
  Divider(color = colors.onSurface.copy(alpha = 0.08f))
}

/** Header and TextField to input text */
@Composable
private fun NamedTextFieldInput(
    modifier: Modifier = Modifier,
    header: String? = null,
    value: TextFieldValue,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    onValueChange: (TextFieldValue) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
  TextField(
      label = header?.let { { Text(it, style = typography.overline) } },
      value = value,
      onValueChange = if (enabled) onValueChange else { _ -> },
      leadingIcon = leadingIcon,
      keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
      modifier = modifier.fillMaxWidth(),
      textStyle = typography.body1,
  )
}

/** Category tiles in 2 columns */
@Composable
private fun CategorySelector(selected: String?, select: (String?) -> Unit) {
  categories.toList().chunked(2).forEach { chunk ->
    Row {
      chunk.forEach { category ->
        Column(modifier = Modifier.weight(0.5F).clickable(onClick = { select(category) })) {
          Text(
              category,
              color = if (category == selected) colors.onSecondary else colors.onBackground,
              style = typography.button,
              modifier =
                  Modifier.padding(8.dp)
                      .then(
                          if (category == selected)
                              Modifier.background(colors.secondary, CircleShape)
                          else Modifier)
                      .padding(8.dp),
          )
        }
      }
    }
  }
}

private val categories =
    arrayOf(
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
        "Помощь родителям")
