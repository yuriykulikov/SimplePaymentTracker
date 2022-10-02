package simple.payment.tracker.compose

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlin.random.Random.Default.nextInt
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import simple.payment.tracker.GroupReport
import simple.payment.tracker.MonthlyStatistics
import simple.payment.tracker.PaymentsRepository
import simple.payment.tracker.Settings
import simple.payment.tracker.Transaction
import simple.payment.tracker.TransactionsRepository
import simple.payment.tracker.TripStatistics
import simple.payment.tracker.firebase.FirebaseSignIn
import simple.payment.tracker.logging.LoggerFactory
import simple.payment.tracker.theme.ColoredTheme
import simple.payment.tracker.theme.ExtendedColors
import simple.payment.tracker.theme.Theme
import simple.payment.tracker.theme.toColors

sealed class Screen {
  object List : Screen()
  object ListAll : Screen()
  object New : Screen()
  object Monthly : Screen()
  object Trips : Screen()
  object Settings : Screen()

  data class Details(val transaction: Transaction) : Screen()
  data class MonthDetails(val report: GroupReport) : Screen()
}

@Composable
fun PaymentsApp(
    backs: Backs,
    transactions: TransactionsRepository,
    paymentsRepository: PaymentsRepository,
    monthlyStatistics: MonthlyStatistics,
    tripsStatistics: TripStatistics,
    settings: DataStore<Settings>,
    loggers: LoggerFactory,
    firebaseSignIn: FirebaseSignIn,
) {
  val colors: State<ExtendedColors> =
      remember { settings.data.map { it.theme.toColors() } }
          .collectAsState(initial = runBlocking { settings.data.first().theme.toColors() })

  ColoredTheme(colors.value) {
    // Remember a SystemUiController
    val systemUiController = rememberSystemUiController()
    val topColor = Theme.colors.topBar
    val bottomColor = Theme.colors.bottomBar

    SideEffect {
      systemUiController.setStatusBarColor(color = topColor)
      systemUiController.setNavigationBarColor(
          color = bottomColor, navigationBarContrastEnforced = false)
    }

    val signedInAs = firebaseSignIn.signedInUserEmail().collectAsState()

    if (signedInAs.value == null) {
      Scaffold(
          content = {
            SignInScreen(
                Modifier.padding(it),
                firebaseSignIn,
            )
          })
    } else {
      AppContent(
          backs,
          transactions,
          paymentsRepository,
          monthlyStatistics,
          tripsStatistics,
          settings,
          loggers,
          firebaseSignIn,
      )
    }
  }
}

@Composable
private fun AppContent(
    backs: Backs,
    transactions: TransactionsRepository,
    paymentsRepository: PaymentsRepository,
    monthlyStatistics: MonthlyStatistics,
    tripsStatistics: TripStatistics,
    settings: DataStore<Settings>,
    loggers: LoggerFactory,
    firebaseSignIn: FirebaseSignIn,
) {
  val selectedScreen: MutableState<Screen> = remember { mutableStateOf(Screen.List) }
  val detailsToShow: MutableState<Screen?> = remember { mutableStateOf(null) }
  val monthDetailsToShow: MutableState<Screen?> = remember { mutableStateOf(null) }
  val hideDetails = { detailsToShow.value = null }
  val showDetails: (Transaction?) -> Unit = {
    detailsToShow.value = if (it == null) Screen.New else Screen.Details(it)
  }
  val showMonthDetails: (GroupReport) -> Unit = {
    monthDetailsToShow.value = Screen.MonthDetails(it)
  }
  val hideMonthDetails = { monthDetailsToShow.value = null }

  val bottomBar = @Composable { NavigationBottomBar(selectedScreen) }

  backs.backPressed.CommitSubscribe {
    when {
      detailsToShow.value != null -> hideDetails()
      monthDetailsToShow.value != null -> hideMonthDetails()
      else -> selectedScreen.value = Screen.List
    }
  }

  val screen: Screen = detailsToShow.value ?: monthDetailsToShow.value ?: selectedScreen.value

  val search: MutableState<TextFieldValue> = remember { mutableStateOf(TextFieldValue("")) }
  val inboxListState = rememberLazyListState()
  val allListState = rememberLazyListState()
  val monthDetailsState = rememberLazyListState()

  val reportLookup = { report: GroupReport ->
    monthlyStatistics.report(report.name).onErrorResumeNext(tripsStatistics.report(report.name))
  }
  Crossfade(screen) { scr ->
    Surface(color = colors.background) {
      when (scr) {
        is Screen.List ->
            ListScreen(false, transactions, showDetails, bottomBar, search, inboxListState)
        is Screen.ListAll ->
            ListScreen(true, transactions, showDetails, bottomBar, search, allListState)
        is Screen.Details ->
            DetailsScreen(
                paymentsRepository,
                scr.transaction,
                hideDetails,
                settings,
                loggers.createLogger("DetailsScreen"),
            )
        is Screen.New ->
            DetailsScreen(
                paymentsRepository,
                null,
                hideDetails,
                settings,
                loggers.createLogger("DetailsScreen"),
            )
        is Screen.Monthly -> MonthlyScreen(monthlyStatistics, bottomBar, showMonthDetails)
        is Screen.MonthDetails ->
            GroupDetailsScreen(scr.report, showDetails, monthDetailsState, reportLookup)
        is Screen.Trips -> TripsScreen(tripsStatistics, bottomBar, showMonthDetails)
        is Screen.Settings -> SettingsScreen(settings, firebaseSignIn)
      }
    }
  }
}

val borderColors =
    listOf(
        Color.Black,
        Color.DarkGray,
        Color.Gray,
        Color.LightGray,
        Color.White,
        Color.Red,
        Color.Green,
        Color.Blue,
        Color.Yellow,
        Color.Cyan,
        Color.Magenta,
        Color.Transparent)

fun Modifier.debugBorder(): Modifier = composed {
  if (false) {
    border(width = 1.dp, color = borderColors[nextInt(0, borderColors.lastIndex)])
  }
  this
}
