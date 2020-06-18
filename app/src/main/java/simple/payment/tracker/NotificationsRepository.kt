package simple.payment.tracker

import com.squareup.moshi.Moshi
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

data class Notification(
    val time: Long,
    val text: String
)

/**
 * Active storage for notifications. Notifications can only be added, never removed.
 */
class NotificationsRepository(
    private val logger: Logger,
    private val filer: Filer,
    private val moshi: Moshi
) {
    private val notifications: BehaviorSubject<Set<Notification>>

    private val adapter = moshi
        .listAdapter<Notification>()
        .indent("  ")

    init {
        val fileContents: String = filer.read("notifications.txt") ?: "[]"
        val initial: Set<Notification> = (adapter.fromJson(fileContents) ?: emptyList())
            .toSet()
        notifications = BehaviorSubject.createDefault(initial)

        logger.debug { "Notifications: " }
        fileContents.lines()
            .forEach { logger.debug { it } }
    }

    fun notifications(): Observable<Set<Notification>> = notifications

    fun addNotifications(newNotifications: List<Notification>) {
        logger.debug { "Adding notifications: $newNotifications" }

        notifications.modify { plus(newNotifications) }

        filer.write(
            "notifications.txt",
            adapter.toJson(notifications.value?.toList())
        )
    }
}
