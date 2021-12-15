package simple.payment.tracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.input.key.Key.Companion.Settings
import androidx.datastore.core.DataStore
import org.koin.android.ext.android.inject
import simple.payment.tracker.compose.Backs
import simple.payment.tracker.compose.PaymentsApp

class MainActivity : AppCompatActivity() {
  private val backs: Backs by inject()
  private val paymentsRepository: PaymentsRepository by inject()
  private val transactionsRepository: TransactionsRepository by inject()
  private val monthlyStatistics: MonthlyStatistics by inject()
  private val tripsStatistics: TripStatistics by inject()
  private val settings: DataStore<Settings> by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (applicationContext.packageName !in
        android.provider.Settings.Secure.getString(
            contentResolver, "enabled_notification_listeners")) {
      startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }
    setContent {
      PaymentsApp(
          backs,
          transactionsRepository,
          paymentsRepository,
          monthlyStatistics,
          tripsStatistics,
          settings,
      )
    }
  }

  override fun onBackPressed() {
    backs.backPressed.onNext("MainActivity")
  }
}
