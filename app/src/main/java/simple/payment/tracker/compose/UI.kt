package simple.payment.tracker.compose

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Dialog
import androidx.datastore.core.DataStore
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import simple.payment.tracker.GroupReport
import simple.payment.tracker.MonthlyStatistics
import simple.payment.tracker.Payment
import simple.payment.tracker.PaymentsRepository
import simple.payment.tracker.R
import simple.payment.tracker.Settings
import simple.payment.tracker.SwipedPaymentsRepository
import simple.payment.tracker.TransactionsRepository
import simple.payment.tracker.TripStatistics
import simple.payment.tracker.firebase.FirebaseSignIn
import simple.payment.tracker.logging.LoggerFactory
import simple.payment.tracker.theme.ColoredTheme
import simple.payment.tracker.theme.ExtendedColors
import simple.payment.tracker.theme.Theme
import simple.payment.tracker.theme.themeTypography
import simple.payment.tracker.theme.toColors

sealed class Screen {
  object List : Screen()
  object ListAll : Screen()
  object New : Screen()
  object Monthly : Screen()
  object Trips : Screen()
  object Settings : Screen()

  data class Details(val payment: Payment) : Screen()
  data class MonthDetails(val report: GroupReport) : Screen()
}

@Composable
fun PaymentsApp(
    transactions: TransactionsRepository,
    paymentsRepository: PaymentsRepository,
    monthlyStatistics: MonthlyStatistics,
    tripsStatistics: TripStatistics,
    settings: DataStore<Settings>,
    loggers: LoggerFactory,
    firebaseSignIn: FirebaseSignIn,
    swipedPaymentsRepository: SwipedPaymentsRepository,
) {
  val colors: State<ExtendedColors> =
      remember { settings.data.map { it.theme.toColors() } }
          .collectAsState(initial = runBlocking { settings.data.first().theme.toColors() })

  CategoriesDialogFactory = { onDismissRequest, content ->
    Dialog(onDismissRequest = onDismissRequest) { content() }
  }

  ColoredTheme(
      colors.value,
      typography =
          themeTypography(
              FontFamily(
                  Font(R.font.montserrat_regular),
                  Font(R.font.montserrat_medium, FontWeight.W500),
                  Font(R.font.montserrat_semibold, FontWeight.W600)))) {
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
              transactions,
              paymentsRepository,
              monthlyStatistics,
              tripsStatistics,
              swipedPaymentsRepository,
              settings,
              loggers,
              firebaseSignIn,
          )
        }
      }
}

@Composable
private fun AppContent(
    transactions: TransactionsRepository,
    paymentsRepository: PaymentsRepository,
    monthlyStatistics: MonthlyStatistics,
    tripsStatistics: TripStatistics,
    swipedPaymentsRepository: SwipedPaymentsRepository,
    settings: DataStore<Settings>,
    loggers: LoggerFactory,
    firebaseSignIn: FirebaseSignIn,
) {
  val selectedScreen: MutableState<Screen> = remember { mutableStateOf(Screen.List) }
  val detailsToShow: MutableState<Screen?> = remember { mutableStateOf(null) }
  val monthDetailsToShow: MutableState<Screen?> = remember { mutableStateOf(null) }
  val hideDetails = { detailsToShow.value = null }
  val showDetails: (Payment?) -> Unit = {
    detailsToShow.value = if (it == null) Screen.New else Screen.Details(it)
  }
  val showMonthDetails: (GroupReport) -> Unit = {
    monthDetailsToShow.value = Screen.MonthDetails(it)
  }
  val hideMonthDetails = { monthDetailsToShow.value = null }

  val bottomBar = @Composable { NavigationBottomBar(selectedScreen) }

  BackHandler {
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
    monthlyStatistics.report(report.name).catch { emitAll(tripsStatistics.report(report.name)) }
  }
  Crossfade(screen) { scr ->
    Surface(color = colors.background) {
      when (scr) {
        is Screen.List ->
            InboxList(
                transactionsRepository = transactions,
                swipedPaymentsRepository = swipedPaymentsRepository,
                showDetails = showDetails,
                bottomBar = bottomBar,
                listState = inboxListState,
            )
        is Screen.ListAll ->
            TransactionsList(
                transactionsRepository = transactions,
                showDetails = showDetails,
                bottomBar = bottomBar,
                search = search,
                listState = allListState,
            )
        is Screen.Details ->
            DetailsScreen(
                paymentsRepository,
                swipedPaymentsRepository,
                scr.payment,
                hideDetails,
                settings,
                loggers.createLogger("DetailsScreen"),
            )
        is Screen.New ->
            DetailsScreen(
                paymentsRepository,
                swipedPaymentsRepository,
                null,
                hideDetails,
                settings,
                loggers.createLogger("DetailsScreen"),
            )
        is Screen.Monthly ->
            MonthlyScreen(
                monthlyStatistics, firebaseSignIn.signedInUserEmail(), bottomBar, showMonthDetails)
        is Screen.MonthDetails ->
            GroupDetailsScreen(scr.report, showDetails, monthDetailsState, reportLookup)
        is Screen.Trips ->
            TripsScreen(
                tripsStatistics, firebaseSignIn.signedInUserEmail(), bottomBar, showMonthDetails)
        is Screen.Settings -> SettingsScreen(settings, firebaseSignIn)
      }
    }
  }
}
