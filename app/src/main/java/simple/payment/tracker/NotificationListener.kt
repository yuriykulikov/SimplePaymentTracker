package simple.payment.tracker

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.koin.android.ext.android.inject
import simple.payment.tracker.stores.DataStore

/**
 * Listens for incoming PayPal notificationsStore. Skips all other notificationsStore as well as
 * grouped notificationsStore.
 */
class NotificationListener : NotificationListenerService() {
  private val notificationsRepository: NotificationsRepository by inject()
  private val settings: DataStore<Settings> by inject()
  override fun onListenerConnected() {
    super.onListenerConnected()
    scan()
  }

  override fun onNotificationPosted(sbn: StatusBarNotification?) {
    super.onNotificationPosted(sbn)
    scan()
  }

  private fun scan() {
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
                        device = settings.value.deviceName,
                    )
                else -> null
              }
            }
    this.notificationsRepository.addNotifications(notifications)
  }
}
