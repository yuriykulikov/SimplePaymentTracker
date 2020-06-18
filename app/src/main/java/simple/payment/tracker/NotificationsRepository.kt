package simple.payment.tracker

import com.squareup.moshi.Moshi
import io.reactivex.Observable
import simple.payment.tracker.stores.FileDataStore
import simple.payment.tracker.stores.Filer
import simple.payment.tracker.stores.listDataStore
import simple.payment.tracker.stores.modify

data class Notification(
    val time: Long,
    val text: String
)

/**
 * Notifications can only be added, never removed.
 */
class NotificationsRepository(
    private val logger: Logger,
    private val filer: Filer,
    private val moshi: Moshi
) {
    private val notifications: FileDataStore<List<Notification>> = FileDataStore.listDataStore(
        filer,
        "notifications.txt",
        "[]",
        moshi
    )

    init {
        notifications.dump(logger)
    }

    fun notifications(): Observable<List<Notification>> = notifications.observe()

    fun addNotifications(newNotifications: List<Notification>) {
        logger.debug { "Adding notifications: $newNotifications" }
        notifications.modify { plus(newNotifications) }
    }
}
