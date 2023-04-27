package simple.payment.tracker.desktop

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import simple.payment.tracker.GroupReport
import simple.payment.tracker.MonthlyStatistics
import simple.payment.tracker.Payment
import simple.payment.tracker.PaymentsRepository
import simple.payment.tracker.Settings
import simple.payment.tracker.SwipedPaymentsRepository
import simple.payment.tracker.Transactions
import simple.payment.tracker.TripStatistics
import simple.payment.tracker.compose.DetailsScreen
import simple.payment.tracker.compose.GroupDetailsScreen
import simple.payment.tracker.compose.InboxList
import simple.payment.tracker.compose.MonthlyScreen
import simple.payment.tracker.compose.TransactionsList
import simple.payment.tracker.compose.TripsScreen
import simple.payment.tracker.compose.debugBorder
import simple.payment.tracker.logging.Logger
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
  class Details(val payment: Payment) : SecondaryScreen
  class MonthDetails(val report: GroupReport) : SecondaryScreen
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
    logFlow: Flow<List<String>>,
    keyPresses: MutableSharedFlow<KeyEvent>,
) {
  var selectedScreen: Screen? by remember { mutableStateOf(Screen.List) }
  var detailsToShow: SecondaryScreen? by remember { mutableStateOf(null) }
  var monthDetailsToShow: SecondaryScreen? by remember { mutableStateOf(null) }
  val showDetails: (Payment?) -> Unit = {
    detailsToShow = if (it == null) SecondaryScreen.New else SecondaryScreen.Details(it)
  }
  val hideDetails = { detailsToShow = null }
  val showMonthDetails: (GroupReport) -> Unit = {
    monthDetailsToShow = SecondaryScreen.MonthDetails(it)
  }
  val hideMonthDetails = { monthDetailsToShow = null }

  val search: MutableState<TextFieldValue> = remember { mutableStateOf(TextFieldValue("")) }
  val inboxListState = rememberLazyListState()
  val allListState = rememberLazyListState()
  val monthDetailsState = rememberLazyListState()

  val reportLookup: (GroupReport) -> Flow<GroupReport> = { report: GroupReport ->
    monthlyStatistics.report(report.name).catch { emitAll(tripsStatistics.report(report.name)) }
  }
  val bottomBar =
      @Composable { NavigationBottomBar(selectedScreen, onChange = { selectedScreen = it }) }

  val colors: State<ExtendedColors> =
      remember { settings.data.map { it.theme.toColors() } }
          .collectAsState(initial = runBlocking { settings.data.first().theme.toColors() })

  val logger = loggers.createLogger("UI")
  val cardCorner = 32.dp
  val logs: List<String> by remember { logFlow }.collectAsState(initial = emptyList<String>())
  val logsListState = rememberLazyListState()
  LaunchedEffect(logs) {
    if (logs.size > 1) {
      logsListState.animateScrollToItem(logs.lastIndex)
    }
  }

  LaunchedEffect(keyPresses) {
    keyPresses
        .filter { it.key == Key.Escape && it.type == KeyEventType.KeyUp }
        .collect {
          logger.warning { "Escape pressed" }
          hideDetails()
          hideMonthDetails()
        }
  }

  val typography =
      themeTypography(
          FontFamily(
              Font(resource = "montserrat_regular.ttf"),
              Font(resource = "montserrat_medium.ttf", FontWeight.W500),
              Font(resource = "montserrat_semibold.ttf", FontWeight.W600)))
  ColoredTheme(colors.value, typography) {
    Surface(modifier = Modifier.background(colors.value.background)) {
      Row(Modifier.debugBorder()) {
        Column(Modifier.debugBorder().weight(1f, false)) {
          Row(Modifier.debugBorder()) {
            Card(
                modifier = Modifier.padding(16.dp).debugBorder(),
                shape = RoundedCornerShape(cardCorner)) {
                  TopBar(
                      Modifier.fillMaxWidth().padding(16.dp).debugBorder(), settings, userNameStore)
                }
          }

          Row(Modifier.debugBorder().fillMaxWidth()) {
            Column(Modifier.debugBorder().weight(.5f)) {
              Card(
                  modifier = Modifier.padding(16.dp).debugBorder(),
                  shape = RoundedCornerShape(cardCorner)) {
                    val scope = rememberCoroutineScope()
                    LeftCrossFade(
                        selectedScreen,
                        transactions,
                        swipedPaymentsRepository,
                        showDetails,
                        bottomBar,
                        inboxListState,
                        search,
                        allListState,
                        monthlyStatistics,
                        userNameStore,
                        scope,
                        showMonthDetails,
                        tripsStatistics)
                  }
            }
            Column(Modifier.debugBorder().weight(.5f)) {
              Card(
                  modifier = Modifier.padding(16.dp).debugBorder(),
                  shape = RoundedCornerShape(cardCorner)) {
                    RightCrossFade(
                        logger,
                        detailsToShow,
                        monthDetailsToShow,
                        hideDetails,
                        showDetails,
                        monthDetailsState,
                        reportLookup,
                        paymentsRepository,
                        swipedPaymentsRepository,
                        settings,
                        loggers)
                  }
            }
          }
        }
        Column(Modifier.debugBorder().fillMaxWidth(.33f)) {
          LazyColumn(state = logsListState) { items(logs) { Text(it) } }
        }
      }
    }
  }
}

@Composable
private fun RightCrossFade(
    logger: Logger,
    detailsToShow: SecondaryScreen?,
    monthDetailsToShow: SecondaryScreen?,
    hideDetails: () -> Unit,
    showDetails: (Payment?) -> Unit,
    monthDetailsState: LazyListState,
    reportLookup: (GroupReport) -> Flow<GroupReport>,
    paymentsRepository: PaymentsRepository,
    swipedPaymentsRepository: SwipedPaymentsRepository,
    settings: DataStore<Settings>,
    loggers: LoggerFactory
) {
  Crossfade(detailsToShow ?: monthDetailsToShow) { scr ->
    Surface {
      when (scr) {
        is SecondaryScreen.MonthDetails ->
            GroupDetailsScreen(scr.report, showDetails, monthDetailsState, reportLookup)
        is SecondaryScreen.Details ->
            DetailsScreen(
                paymentsRepository,
                swipedPaymentsRepository,
                scr.payment,
                hideDetails,
                settings,
                loggers.createLogger("DetailsScreen"),
            )
        is SecondaryScreen.New ->
            DetailsScreen(
                paymentsRepository,
                swipedPaymentsRepository,
                null,
                hideDetails,
                settings,
                loggers.createLogger("DetailsScreen"),
            )
        else -> {
          Text("Nothing to see here")
        }
      }
    }
  }
}

@Composable
private fun LeftCrossFade(
    screen: Screen?,
    transactions: Transactions,
    swipedPaymentsRepository: SwipedPaymentsRepository,
    showDetails: (Payment?) -> Unit,
    bottomBar: @Composable () -> Unit,
    inboxListState: LazyListState,
    search: MutableState<TextFieldValue>,
    allListState: LazyListState,
    monthlyStatistics: MonthlyStatistics,
    userNameStore: DataStore<String>,
    scope: CoroutineScope,
    showMonthDetails: (GroupReport) -> Unit,
    tripsStatistics: TripStatistics
) {
  Crossfade(screen) { scr ->
    Surface {
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
        is Screen.Trips ->
            TripsScreen(
                tripsStatistics,
                userNameStore.data.stateIn(scope, SharingStarted.Eagerly, ""),
                bottomBar,
                showMonthDetails)
        null -> Text("Nothing to see here")
      }
    }
  }
}

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    settings: DataStore<Settings>,
    userName: DataStore<String>,
) {
  Column(modifier = modifier) {
    val coroutineScope = rememberCoroutineScope()

    // Text(text = "Theme", style = MaterialTheme.typography.h6)

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth(),
    ) {
      themeColors().forEach { (name, colors) ->
        ThemeSelector(
            onClick = { coroutineScope.launch { settings.updateData { it.copy(theme = name) } } },
            text = name,
            colors = colors,
        )
      }
    }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth(),
    ) {
      val device = remember { settings.data.map { it.deviceName } }.collectAsState("")
      OutlinedTextField(
          value = device.value,
          onValueChange = {
            coroutineScope.launch { settings.updateData { prev -> prev.copy(deviceName = it) } }
          },
          textStyle = MaterialTheme.typography.body1,
          label = { Text(text = "Device", style = MaterialTheme.typography.body1) },
      )

      val trip = remember { settings.data.map { it.trip } }.collectAsState("")
      OutlinedTextField(
          value = trip.value,
          onValueChange = {
            coroutineScope.launch { settings.updateData { prev -> prev.copy(trip = it) } }
          },
          textStyle = MaterialTheme.typography.body1,
          label = { Text(text = "Trip", style = MaterialTheme.typography.body1) },
      )

      val userNameState = remember { userName.data }.collectAsState("")
      OutlinedTextField(
          value = userNameState.value,
          onValueChange = { newValue ->
            coroutineScope.launch { userName.updateData { newValue } }
          },
          textStyle = MaterialTheme.typography.body1,
          label = { Text(text = "User", style = MaterialTheme.typography.body1) },
      )
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
