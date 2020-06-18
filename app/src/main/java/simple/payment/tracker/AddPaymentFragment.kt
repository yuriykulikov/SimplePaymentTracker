package simple.payment.tracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

class AddPaymentFragment : DialogFragment() {
    private val paymentsRepository: PaymentsRepository by inject()
    private val categories = arrayOf(
        "Кот",
        "Еда",
        "Гедонизм",
        "Столовая",
        "Снаряга",
        "Книги и образование",
        "Ресторан или Takeout",
        "Подарки",
        "Путешествия",
        "Косметика и медикаменты",
        "Бензин",
        "Развлечения",
        "Дурость",
        "Для дома",
        "Зубной, парикмахер, врач, физио",
        "Разное",
        "Хобби",
        "Одежда и вещи",
        "Девайсы",
        "Бытовая химия",
        "Доплата",
        "Помощь родителям",
        "Почта",
        "Baby",
        "Бюрократия"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.payment, null, false).also { bind(it) }
    }

    private fun bind(view: View) {
        val preview = view.findViewById<TextView>(R.id.payment_preview)
        val date = view.findViewById<EditText>(R.id.payment_date)
        val time = view.findViewById<EditText>(R.id.payment_time)
        val sum = view.findViewById<EditText>(R.id.payment_sum)
        val category = view.findViewById<TextView>(R.id.payment_category)
        val merchant = view.findViewById<EditText>(R.id.payment_merchant)
        val comment = view.findViewById<EditText>(R.id.payment_comment)

        val dateFormat = SimpleDateFormat("dd-MM-yy", Locale.GERMANY)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.GERMANY)

        date.setText(dateFormat.format(Calendar.getInstance().time))
        time.setText(timeFormat.format(Calendar.getInstance().time))

        category.text = "-"
        var selectedCategory = -1

        category.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setSingleChoiceItems(
                    categories,
                    selectedCategory
                ) { dialog, index ->
                    selectedCategory = index
                    category.text = categories[index]
                    dialog.dismiss()
                }
                .show()
        }

        view.findViewById<Button>(R.id.payment_commit).setOnClickListener {
            runCatching {
                val paymentTime = SimpleDateFormat("dd-MM-yy HH:mm", Locale.GERMANY)
                    .parse("${date.text} ${time.text}")!!
                    .time

                val payment = Payment(
                    category = categories[selectedCategory],
                    notificationId = null,
                    time = paymentTime,
                    comment = if (comment.text.isEmpty()) null else comment.text.toString(),
                    merchant = if (merchant.text.isEmpty()) null else merchant.text.toString(),
                    sum = sum.text.toString().toInt(),
                    id = Calendar.getInstance().timeInMillis
                )

                paymentsRepository.addPayment(payment)
                dismiss()
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    companion object {
        fun create(): AddPaymentFragment {
            return AddPaymentFragment()
        }
    }
}