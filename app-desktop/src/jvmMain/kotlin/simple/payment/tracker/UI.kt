package simple.payment.tracker

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import simple.payment.tracker.compose.InboxList
import simple.payment.tracker.compose.MonthlyScreen
import simple.payment.tracker.compose.TransactionsList
import simple.payment.tracker.compose.TripsScreen
import simple.payment.tracker.compose.debugBorder
import simple.payment.tracker.logging.LoggerFactory
import simple.payment.tracker.theme.ColoredTheme
import simple.payment.tracker.theme.ExtendedColors
import simple.payment.tracker.theme.themeColors
import simple.payment.tracker.theme.themeTypography
import simple.payment.tracker.theme.toColors

sealed interface Screen {
  object List : Screen
  object ListAll : Screen
  object Monthly : Screen
  object Trips : Screen
}

sealed interface SecondaryScreen {
  object New : SecondaryScreen
  class Details(payment: Payment) : SecondaryScreen
  class MonthDetails(report: GroupReport) : SecondaryScreen
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppContent(
    transactions: Transactions,
    paymentsRepository: PaymentsRepository,
    monthlyStatistics: MonthlyStatistics,
    tripsStatistics: TripStatistics,
    swipedPaymentsRepository: SwipedPaymentsRepository,
    loggers: LoggerFactory,
    settings: DataStore<Settings>,
    userNameStore: DataStore<String>,
) {
  val selectedScreen: MutableState<Screen> = remember { mutableStateOf(Screen.List) }
  val detailsToShow: MutableState<SecondaryScreen?> = remember { mutableStateOf(null) }
  val monthDetailsToShow: MutableState<SecondaryScreen?> = remember { mutableStateOf(null) }
  val hideDetails = { detailsToShow.value = null }
  val showDetails: (Payment?) -> Unit = {
    detailsToShow.value = if (it == null) SecondaryScreen.New else SecondaryScreen.Details(it)
  }
  val showMonthDetails: (GroupReport) -> Unit = {
    monthDetailsToShow.value = SecondaryScreen.MonthDetails(it)
  }
  val hideMonthDetails = { monthDetailsToShow.value = null }

  val screen: Screen = selectedScreen.value
  val secScreen: SecondaryScreen? = detailsToShow.value ?: monthDetailsToShow.value

  val search: MutableState<TextFieldValue> = remember { mutableStateOf(TextFieldValue("")) }
  val inboxListState = rememberLazyListState()
  val allListState = rememberLazyListState()
  val monthDetailsState = rememberLazyListState()

  val reportLookup = { report: GroupReport ->
    monthlyStatistics.report(report.name).catch { emitAll(tripsStatistics.report(report.name)) }
  }
  val bottomBar = @Composable { NavigationBottomBar(selectedScreen) }

  val colors: State<ExtendedColors> =
      remember { settings.data.map { it.theme.toColors() } }
          .collectAsState(initial = runBlocking { settings.data.first().theme.toColors() })

  val userName = userNameStore.data.collectAsState(initial = "").value
  val scope = rememberCoroutineScope()

  ColoredTheme(colors.value) {
    Surface {
      Column(Modifier.debugBorder()) {
        Row(Modifier.debugBorder()) { TopBar(settings, userNameStore) }
        Row(Modifier.debugBorder()) {
          Column(Modifier.debugBorder()) {
            Crossfade(screen) { scr ->
              Surface(
                  modifier =
                      Modifier.onKeyEvent {
                        when {
                          (it.key == Key.Escape && it.type == KeyEventType.KeyUp) -> {
                            hideDetails()
                            true
                          }
                          else -> false
                        }
                      }) {
                    when (scr) {
                      is Screen.List ->
                          InboxList(
                              transactions = transactions,
                              swipedPaymentsRepository = swipedPaymentsRepository,
                              showDetails = showDetails,
                              bottomBar = bottomBar,
                              listState = inboxListState,
                          )
                      is Screen.ListAll ->
                          TransactionsList(
                              transactions = transactions,
                              showDetails = showDetails,
                              bottomBar = bottomBar,
                              search = search,
                              listState = allListState,
                          )
                      is Screen.Monthly ->
                          MonthlyScreen(
                              monthlyStatistics,
                              userNameStore.data.stateIn(scope, SharingStarted.Eagerly, ""),
                              bottomBar,
                              showMonthDetails)
                      // is Screen.MonthDetails ->
                      //     GroupDetailsScreen(scr.report, showDetails, monthDetailsState,
                      // reportLookup)
                      // is Screen.Details ->
                      //     DetailsScreen(
                      //         paymentsRepository,
                      //         swipedPaymentsRepository,
                      //         scr.payment,
                      //         hideDetails,
                      //         settings,
                      //         loggers.createLogger("DetailsScreen"),
                      //     )
                      // is Screen.New ->
                      //     DetailsScreen(
                      //         paymentsRepository,
                      //         swipedPaymentsRepository,
                      //         null,
                      //         hideDetails,
                      //         settings,
                      //         loggers.createLogger("DetailsScreen"),
                      //     )
                      is Screen.Trips ->
                          TripsScreen(
                              tripsStatistics,
                              userNameStore.data.stateIn(scope, SharingStarted.Eagerly, ""),
                              bottomBar,
                              showMonthDetails)
                    }
                  }
            }
          }
          Column {}
        }
      }
    }
  }
}

@Composable
fun TopBar(
    settings: DataStore<Settings>,
    userName: DataStore<String>,
) {
  Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
    val coroutineScope = rememberCoroutineScope()

    Text(text = "Theme", style = MaterialTheme.typography.h6)
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
    ) {
      themeColors().forEach { (name, colors) ->
        ThemeSelector(
            onClick = { coroutineScope.launch { settings.updateData { it.copy(theme = name) } } },
            text = name,
            colors = colors,
        )
      }
    }
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
      val device = remember { settings.data.map { it.deviceName } }.collectAsState("")
      OutlinedTextField(
          label = { Text(text = "Device", style = MaterialTheme.typography.body1) },
          value = device.value,
          onValueChange = {
            coroutineScope.launch { settings.updateData { prev -> prev.copy(deviceName = it) } }
          },
          textStyle = MaterialTheme.typography.body1,
      )
      val trip = remember { settings.data.map { it.trip } }.collectAsState("")
      OutlinedTextField(
          label = { Text(text = "Trip", style = MaterialTheme.typography.body1) },
          value = trip.value,
          onValueChange = {
            coroutineScope.launch { settings.updateData { prev -> prev.copy(trip = it) } }
          },
          textStyle = MaterialTheme.typography.body1,
      )

      val scope = rememberCoroutineScope()
      val userNameState = userName.data.collectAsState("")
      OutlinedTextField(
          userNameState.value, onValueChange = { scope.launch { userName.updateData { it } } })
    }
  }
}

@Composable
private fun ThemeSelector(onClick: () -> Unit, text: String, colors: ExtendedColors) {
  TextButton(
      onClick,
      modifier =
          Modifier.padding(4.dp)
              .background(
                  color = colors.background,
                  shape = CircleShape,
              )) {
        Text(
            text,
            style = themeTypography.button,
            color = colors.textAccent,
        )
      }
}
