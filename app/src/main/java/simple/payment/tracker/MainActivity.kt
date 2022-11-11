package simple.payment.tracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import org.koin.android.ext.android.inject
import simple.payment.tracker.compose.PaymentsApp
import simple.payment.tracker.firebase.FirebaseSignIn
import simple.payment.tracker.logging.LoggerFactory

class MainActivity : AppCompatActivity() {
  private val paymentsRepository: PaymentsRepository by inject()
  private val transactionsRepository: TransactionsRepository by inject()
  private val monthlyStatistics: MonthlyStatistics by inject()
  private val tripsStatistics: TripStatistics by inject()
  private val settings: DataStore<Settings> by inject()
  private val loggers: LoggerFactory by inject()
  private val firebaseSignIn: FirebaseSignIn by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (applicationContext.packageName !in
        android.provider.Settings.Secure.getString(
            contentResolver, "enabled_notification_listeners")) {
      startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }
    setContent {
      PaymentsApp(
          transactionsRepository,
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
