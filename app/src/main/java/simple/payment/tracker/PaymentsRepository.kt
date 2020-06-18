package simple.payment.tracker

import com.squareup.moshi.Moshi
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

data class Payment(
    val category: String,
    val notificationId: Long?,
    val time: Long?,
    val comment: String?,
    val merchant: String?,
    val sum: Int?,
    val id: Long = notificationId ?: time!!
) {
    companion object // functions below
}

class PaymentsRepository(
    private val logger: Logger,
    private val filer: Filer,
    private val moshi: Moshi
) {
    private val payments: BehaviorSubject<List<Payment>>

    private val adapter = moshi
        .listAdapter<Payment>()
        .indent("  ")

    init {
        val fileContents: String = filer.read("payments.txt") ?: "[]"
        val initial: List<Payment> = adapter.fromJson(fileContents)
            ?.distinct()
            ?: emptyList()
        payments = BehaviorSubject.createDefault(initial)

        categories.forEach { (k, v) ->
            addPayment(
                Payment.fromNotification(
                    notificationId = k,
                    category = v
                )
            )
        }

        logger.debug { "Payments: " }
        fileContents.lines()
            .forEach { logger.debug { it } }
    }

    fun payments(): Observable<List<Payment>> = payments

    fun addPayment(payment: Payment) {
        logger.debug { "Adding payment: $payment" }

        payments.modify {
            filterNot { it.id == payment.id }
                .plus(payment)
        }

        filer.write(
            "payments.txt",
            adapter.toJson(payments.value?.toList())
        )
    }
}

fun Payment.Companion.fromNotification(
    category: String,
    notificationId: Long,
    sum: Int? = null,
    comment: String? = null
): Payment {
    return Payment(
        category = category,
        notificationId = notificationId,
        time = null,
        comment = comment,
        merchant = null,
        sum = sum
    )
}

fun Payment.Companion.manual(
    category: String,
    sum: Int,
    comment: String? = null,
    merchant: String?,
    time: Long?
): Payment {
    return Payment(
        category = category,
        notificationId = null,
        time = time,
        comment = comment,
        merchant = merchant,
        sum = sum
    )
}

private val categories = mapOf(
    1582842089945 to "Ресторан",
    1582909513905 to "Продукты",
    1582907983402 to "Продукты",
    1582907892627 to "Подарки",
    1582905848255 to "Продукты",
    1582905681862 to "Продукты",
    1582905287999 to "Продукты",
    1582905004996 to "Подарки",
    1582904548728 to "Подарки",
    1582904231846 to "Подарки",
    1582911931252 to "Продукты",
    1582927869976 to "Ресторан",
    1582972298072 to "Продукты",
    1582970959345 to "Подарки",
    1583006221593 to "Экскурсии",
    1583017134429 to "Ресторан",
    1583017488720 to "Ресторан",
    1583053841216 to "Ресторан",
    1583059023710 to "Подарки",
    1583070498151 to "Экскурсии",
    1583173455707 to "Продукты",
    1583173576796 to "Продукты",
    1583148163251 to "Ресторан",
    1583148040837 to "Ресторан",
    1583147314520 to "Ресторан",
    1583249860969 to "Экскурсии",
    1583249919996 to "Продукты",
    1583249932659 to "Продукты",
    1583250403121 to "Продукты",
    1583260165507 to "Продукты",
    1583260572292 to "Ресторан",
    1583251544437 to "Продукты",
    1583261047999 to "Экскурсии",
    1583261759574 to "Экскурсии",
    1583433905466 to "Экскурсии",
    1583408447357 to "Подарки",
    1583351422871 to "Экскурсии",
    1583316226974 to "Подарки",
    1583440476502 to "Ресторан",
    1583696860224 to "Продукты",
    1583613558370 to "Экскурсии"
)
