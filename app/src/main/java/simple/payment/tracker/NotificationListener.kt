package simple.payment.tracker

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Listens for incoming PayPal notificationsStore. Skips all other notificationsStore as well as
 * grouped notificationsStore.
 */
class NotificationListener : NotificationListenerService() {
  private val notificationsRepository: NotificationsRepository by inject()
  private val settings: DataStore<Settings> by inject()
  private val scope = CoroutineScope(Dispatchers.Main)
  override fun onListenerConnected() {
    super.onListenerConnected()
    scan()
  }

  override fun onNotificationPosted(sbn: StatusBarNotification?) {
    super.onNotificationPosted(sbn)
    scan()
  }

  private fun scan() {
    scope.launch {
      val settings = settings.data.first()

      val notifications =
          activeNotifications
              .filter { it.packageName == "com.paypal.android.p2pmobile" }
              .mapNotNull { notification ->
                val text =
                    notification.notification?.extras?.getCharSequence("android.text")?.toString()
                when {
                  text != null ->
                      Notification(
                          time = notification.postTime,
                          text = text,
                          device = settings.deviceName,
                      )
                  else -> null
                }
              }
      notificationsRepository.addNotifications(notifications)
    }
  }
}
