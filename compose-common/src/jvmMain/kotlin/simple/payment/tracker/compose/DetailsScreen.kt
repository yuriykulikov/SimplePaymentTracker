package simple.payment.tracker.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.datastore.core.DataStore
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import simple.payment.tracker.Payment
import simple.payment.tracker.PaymentsRepository
import simple.payment.tracker.Settings
import simple.payment.tracker.SwipedPaymentsRepository
import simple.payment.tracker.logging.Logger
import simple.payment.tracker.theme.ColoredTheme
import simple.payment.tracker.theme.toColors

private val dateFormat = SimpleDateFormat("dd-MM-yy HH:mm", Locale.GERMANY)

var CategoriesDialogFactory:
    @Composable
    (onDismissRequest: () -> Unit, content: @Composable () -> Unit) -> Unit =
    { onDismissRequest, content ->
      Dialog(onCloseRequest = onDismissRequest) { content() }
    }

@Composable
fun CategoriesDialog(onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
  CategoriesDialogFactory(onDismissRequest, content)
}

/** Transaction can be with a notification or without */
@Composable
fun DetailsScreen(
    paymentsRepository: PaymentsRepository,
    swipedPaymentsRepository: SwipedPaymentsRepository,
    payment: Payment?,
    onSave: () -> Unit,
    settings: DataStore<Settings>,
    logger: Logger,
) {
  LaunchedEffect(payment) { logger.debug { "Showing details for $payment" } }

  // TODO make it an actual viewmodel
  val scope = rememberCoroutineScope()
  val viewModel =
      remember(payment) {
        DetailsScreenViewModel(
            payment, paymentsRepository, swipedPaymentsRepository, onSave, settings, logger, scope)
      }

  val onSaveClick = { viewModel.save() }

  val time by viewModel.time.collectAsState()
  val trip by viewModel.trip.collectAsState()
  val sum by viewModel.sum.collectAsState()
  val merchant by viewModel.merchant.collectAsState()
  val comment by viewModel.comment.collectAsState()
  val category by viewModel.category.collectAsState()
  val canSave by remember { viewModel.canSave() }.collectAsState()

  // changing refunds need to change sum...

  DetailsScreenContent(
      onSaveClick = onSaveClick,
      canSave = canSave,
      time = time,
      trip = trip,
      sum = sum,
      merchant = merchant,
      comment = comment,
      category = category,
      canChangeSum = viewModel.canChangeSum,
      state = viewModel,
  )
}

@Preview
@Composable
fun ScreenPreview() {
  ColoredTheme("DeusEx".toColors()) {
    DetailsScreenContent(
        onSaveClick = {},
        canSave = false,
        time = dateFormat.format(Date.from(Instant.now())),
        trip = "Trip",
        sum =
            Sum(
                "144",
                refunds =
                    listOf(
                        Refund("Returned", "5"),
                        Refund("Returned", "10"),
                    )),
        merchant = "Zalando",
        comment = "Stuff",
        category = "Clothes",
        canChangeSum = false,
        state = object : DetailsScreenStateCallback {},
    )
  }
}

@Composable
private fun DetailsScreenContent(
    onSaveClick: () -> Unit,
    canSave: Boolean,
    time: String,
    trip: String,
    sum: Sum,
    merchant: String,
    comment: String,
    category: String?,
    canChangeSum: Boolean,
    state: DetailsScreenStateCallback,
) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(text = "Payment") },
            backgroundColor = colors.primaryVariant,
            actions = {
              IconButton(onClick = onSaveClick, enabled = canSave) {
                Icon(
                    // modifier = Modifier.alpha(if (canSave) 1f else 0.2f),
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = colors.primary,
                )
              }
            },
        )
      },
      floatingActionButton = { Button(onClick = onSaveClick, enabled = canSave) { Text("Save") } },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .wrapContentSize(Alignment.TopCenter)
                    .padding(paddingValues)
                    .verticalScroll(ScrollState(0)),
        ) {
          Surface(elevation = 3.dp, color = colors.primarySurface) {
            Column {
              Row {
                TextField(
                    value = time,
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                      Icon(Icons.Default.List, contentDescription = null, tint = colors.onSurface)
                    },
                    colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
                    keyboardOptions = KeyboardOptions(),
                    modifier = Modifier.weight(0.5f).padding(5.dp).fillMaxWidth(),
                    textStyle = typography.body1,
                )
                TextField(
                    value = trip,
                    onValueChange = { state.change(trip = it) },
                    modifier = Modifier.weight(0.5f).padding(5.dp).fillMaxWidth(),
                    label = { Text("Trip", style = typography.overline) },
                    leadingIcon = {
                      Icon(
                          Icons.Default.ExitToApp, contentDescription = "", tint = colors.onSurface)
                    },
                    colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
                    keyboardOptions = KeyboardOptions(),
                    textStyle = typography.body1,
                )
              }
              TextField(
                  modifier = Modifier.padding(5.dp).fillMaxWidth(),
                  label = { Text("to") },
                  value = merchant,
                  colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
                  onValueChange = { state.change(merchant = it) },
              )
              Row(
                  Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
              ) {
                if (canChangeSum) {
                  TextField(
                      value = sum.initialSum,
                      onValueChange = { state.change(sum = it) },
                      modifier = Modifier.weight(0.5f).padding(5.dp),
                      label = { Text("€") },
                      colors =
                          TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  )
                } else {
                  TextField(
                      value =
                          TextFieldValue(
                              buildAnnotatedString {
                                append(sum.actualSum.toString())
                                if (sum.refunded != 0) {
                                  append(" ")
                                  pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                                  append(sum.initialSum)
                                }
                              }),
                      onValueChange = {},
                      modifier = Modifier.fillMaxWidth(0.5f).padding(5.dp),
                      colors =
                          TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
                      label = { Text("€") },
                      readOnly = true,
                  )
                }

                Button(
                    onClick = { state.addRefund() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = colors.secondary),
                    modifier = Modifier.padding(horizontal = 5.dp)) {
                      Text(text = "Add refund")
                    }
              }
            }
          }

          Surface(elevation = 2.dp, color = colors.primarySurface) {
            Column {
              sum.refunds.forEachIndexed { index, refund ->
                Row(Modifier.fillMaxWidth()) {
                  TextField(
                      value = refund.sum,
                      onValueChange = { value ->
                        state.changeRefund(index, refund.copy(sum = value))
                      },
                      label = { Text("€") },
                      readOnly = false, // TODO
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                      colors =
                          TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
                      modifier = Modifier.weight(1F).padding(5.dp),
                      textStyle = typography.body1,
                  )
                  TextField(
                      value = refund.comment,
                      onValueChange = { value ->
                        state.changeRefund(index, refund.copy(comment = value))
                      },
                      label = { Text("Refund for") },
                      readOnly = false, // TODO
                      modifier = Modifier.weight(1F).padding(5.dp),
                      colors =
                          TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
                      textStyle = typography.body1,
                  )
                }
              }
            }
          }

          TextField(
              label = { Text(text = "for", style = typography.body1) },
              value = comment,
              onValueChange = { state.change(comment = it) },
              modifier = Modifier.fillMaxWidth().padding(25.dp),
              colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
              textStyle = typography.body1,
          )
          var showPop by remember { mutableStateOf(false) }
          Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { showPop = true }, modifier = Modifier.fillMaxWidth(0.5f)) {
              Text(text = category?.takeIf { it.isNotEmpty() } ?: "Select category")
            }
          }
          if (showPop) {
            CategoriesDialog(onDismissRequest = { showPop = false }) {
              Surface {
                Column {
                  CategorySelector(
                      selected = category,
                      select = {
                        state.change(category = it)
                        showPop = false
                      })
                }
              }
            }
          }
        }
      },
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

val categories =
    listOf(
        "Еда",
        "Ресторан",
        "Гедонизм",
        "Подарки",
        "Транспорт",
        "Машина",
        "Для дома",
        "Снаряга",
        "Развлечения",
        "Baby",
        "Косметика",
        "Кот",
        "Одежда и вещи",
        "Разное",
        "Аптека",
        "Девайсы",
        "Проживание",
        "Образование",
        "Бытовая химия",
        "Дурость",
        "Парикмахер",
        "Зубной",
    )
