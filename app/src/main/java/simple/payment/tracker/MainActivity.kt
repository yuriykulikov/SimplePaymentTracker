package simple.payment.tracker

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat

class NotificationAdapter(
    private val paymentsRepository: PaymentsRepository
) : RecyclerView.Adapter<NotificationAdapter.Holder>() {
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

    private var dataset: List<Transaction> = emptyList()

    class Holder(val view: View) : RecyclerView.ViewHolder(view) {
        val row_sum = view.findViewById<TextView>(R.id.row_sum)
        val row_merchant = view.findViewById<TextView>(R.id.row_merchant)
        val row_category = view.findViewById<TextView>(R.id.row_category)
    }

    fun setRowData(rowData: List<Transaction>) {
        dataset = rowData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.content_main, parent, false)
            .let { Holder(it) }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val transaction: Transaction = dataset[position]
        holder.row_sum.text = transaction.sum.toString()
        holder.row_merchant.text = transaction.merchant
        holder.row_category.text = transaction.category
        holder.view.setOnClickListener {
            AlertDialog.Builder(holder.view.context)
                .setSingleChoiceItems(
                    categories,
                    if (transaction.confirmed) {
                        categories.indexOf(requireNotNull(transaction.category))
                    } else {
                        0
                    }
                ) { dialog, index ->
                    paymentsRepository.addPayment(
                        Payment.fromNotification(
                            categories[index],
                            notificationId = transaction.notificationId!!
                        )
                    )
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun getItemCount() = dataset.size
}

class MainActivity : AppCompatActivity() {

    private val notificationAdapter: NotificationAdapter by inject()
    private val logger: Logger by inject()
    private val notificationsRepository: NotificationsRepository by inject()
    private val paymentsRepository: PaymentsRepository by inject()
    private val listAggregator: ListAggregator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            val transaction = supportFragmentManager
                .beginTransaction()
                .apply {
                    supportFragmentManager.findFragmentByTag("add")?.let { remove(it) }
                }
            AddPaymentFragment.create().show(transaction, "add")
            // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            //     .setAction("Action", null).show()
        }

        findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = notificationAdapter
        }
    }

    var disposable = CompositeDisposable()
    override fun onResume() {
        super.onResume()
        disposable = CompositeDisposable()

        listAggregator.transactions()
            .subscribe { transactions ->
                notificationAdapter.setRowData(transactions)
            }.let { disposable.add(it) }
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                listAggregator.transactions()
                    .firstOrError()
                    .subscribe { transactions ->
                        val date = SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
                        val str = transactions
                            .sortedBy { it.time }
                            .joinToString("\n") {
                            // 6/19/2019 22:55:10,	108,	Снаряга,	Кате,	Спальник
                            "${date.format(it.time)},${it.sum},${it.category},,${it.comment}"
                        }
                        logger.debug { str }
                    }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
