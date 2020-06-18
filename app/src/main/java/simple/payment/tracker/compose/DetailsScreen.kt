package simple.payment.tracker.compose

import androidx.compose.Composable
import androidx.compose.MutableState
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.drawOpacity
import androidx.ui.foundation.*
import androidx.ui.graphics.Color
import androidx.ui.input.KeyboardType
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.ripple.ripple
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import org.koin.core.context.KoinContextHandler
import simple.payment.tracker.Payment
import simple.payment.tracker.PaymentsRepository
import simple.payment.tracker.Transaction
import simple.payment.tracker.manual
import simple.payment.tracker.theme.PaymentsTheme
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

@Preview("DetailsScreen preview")
@Composable
fun PreviewDetailsScreen() {
    PaymentsTheme {
        Surface {
            DetailsScreen(
                Transaction(
                    confirmed = true,
                    category = "Baby",
                    comment = "Comment",
                    merchant = "Amazon",
                    notificationId = null,
                    paymentId = 100500L,
                    sum = 101,
                    time = 0
                )
            )
        }
    }
}

@Composable
fun DetailsScreen(transaction: Transaction) {
    val category = state { transaction.category }
    val comment = state { TextFieldValue(transaction.comment ?: "") }
    Scaffold(
        topAppBar = {
            TopAppBar(
                title = {
                    Text(text = "${transaction.sum} to ${transaction.merchant}")
                },
                actions = {
                    IconButton(onClick = {
                        State.showList()
                        category.value?.let { changedCategory ->
                            KoinContextHandler.get().get<PaymentsRepository>()
                                .changePayment(
                                    transaction = transaction,
                                    category = changedCategory,
                                    comment = comment.value.text
                                )
                        }
                    }) {
                        Text(text = "Save", style = MaterialTheme.typography.body2)
                    }
                }
            )
        },
        bodyContent = { modifier ->
            Box(modifier = modifier.fillMaxSize().wrapContentSize(Alignment.TopCenter)) {
                Column {
                    NamedTextFieldInput(header = "Comment", state = comment)
                    CategorySelector(category)
                }
            }
        }
    )
}

@Preview("NewScreen preview")
@Composable
fun PreviewNewScreen() {
    PaymentsTheme {
        Surface {
            NewScreen()
        }
    }
}

val dateFormat = SimpleDateFormat(
    "dd-MM-yy HH:mm",
    Locale.GERMANY
)

/** Create a new payment, input everything */
@Composable
fun NewScreen() {
    val category: MutableState<String?> = state { null }
    val sum = state { TextFieldValue("") }
    val comment = state { TextFieldValue("") }
    val merchant = state { TextFieldValue("") }
    val time = state { TextFieldValue(dateFormat.format(Date.from(Instant.now()))) }

    Scaffold(
        topAppBar = {
            TopAppBar(
                title = {
                    Text(text = "New payment")
                },
                actions = {
                    Actions(time, sum, merchant, category, comment)
                }
            )
        },
        bodyContent = { modifier ->
            Box(modifier = modifier.fillMaxSize().wrapContentSize(Alignment.TopCenter)) {
                Column {
                    TextInputs(sum, comment, merchant, time)
                    CategorySelector(category)
                }
            }
        }
    )
}

@Composable
private fun Actions(
    time: MutableState<TextFieldValue>,
    sum: MutableState<TextFieldValue>,
    merchant: MutableState<TextFieldValue>,
    category: MutableState<String?>,
    comment: MutableState<TextFieldValue>
) {
    val canSave = runCatching { dateFormat.parse(time.value.text) }.isSuccess
            && sum.value.text.toIntOrNull() != null
            && merchant.value.text.isNotEmpty()
            && category.value != null
    IconButton(onClick = {
        if (canSave) {
            State.showList()
            KoinContextHandler.get().get<PaymentsRepository>()
                .addPayment(
                    Payment.manual(
                        category.value!!,
                        sum.value.text.toInt(),
                        comment.value.text,
                        merchant.value.text,
                        dateFormat.parse(time.value.text)?.time
                    )
                )
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
    sum: MutableState<TextFieldValue>,
    comment: MutableState<TextFieldValue>,
    merchant: MutableState<TextFieldValue>,
    time: MutableState<TextFieldValue>
) {
    NamedTextFieldInput(
        header = "€",
        state = sum,
        keyboardType = KeyboardType.Number,
        onValueChange = {
            if (it.text.toIntOrNull() != null || it.text.isEmpty()) {
                sum.value = it
            }
        }
    )
    InputDivider()
    NamedTextFieldInput(header = "to", state = merchant)
    InputDivider()
    NamedTextFieldInput(header = "for", state = comment)
    InputDivider()
    NamedTextFieldInput(header = "on", state = time)
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
    onValueChange: (TextFieldValue) -> Unit = { state.value = it }
) {
    Row(modifier = Modifier.padding(16.dp)) {
        Column {
            Text(header, style = MaterialTheme.typography.body1)
        }

        Column(modifier = Modifier.padding(start = 16.dp)) {
            TextField(
                value = state.value,
                onValueChange = onValueChange,
                keyboardType = keyboardType,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Category tiles in 2 columns */
@Composable
private fun CategorySelector(category: MutableState<String?>) {
    VerticalScroller {
        Column {
            categories.toList().chunked(2).forEach { chunk ->
                Row(modifier = Modifier.padding(16.dp)) {
                    chunk.forEach {
                        Clickable(
                            onClick = {
                                category.value = it
                            },
                            modifier = Modifier.ripple(radius = 10.dp, bounded = false)
                        ) {
                            Column(modifier = Modifier.weight(0.5F)) {
                                Text(
                                    it,
                                    color = if (it == category.value) Color.Red else Color.Black,
                                    style = MaterialTheme.typography.button
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val categories = arrayOf(
    "Кот",
    "Еда",
    "Гедонизм",
    "Столовая",
    "Снаряга",
    "Книги и образование",
    "Ресторан или Takeout",
    "Подарки",
    "Путешествия",
    "Косметика и медикаменты",
    "Бензин",
    "Развлечения",
    "Дурость",
    "Для дома",
    "Зубной, парикмахер, врач, физио",
    "Разное",
    "Хобби",
    "Одежда и вещи",
    "Девайсы",
    "Бытовая химия",
    "Доплата",
    "Помощь родителям",
    "Почта",
    "Baby",
    "Бюрократия"
)
