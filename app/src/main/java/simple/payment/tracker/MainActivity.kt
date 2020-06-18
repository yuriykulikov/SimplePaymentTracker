package simple.payment.tracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.ui.core.setContent
import org.koin.android.ext.android.inject
import simple.payment.tracker.compose.PaymentsApp
import simple.payment.tracker.compose.Screen
import simple.payment.tracker.compose.State

class MainActivity : AppCompatActivity() {
    private val listAggregator: ListAggregator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PaymentsApp()
        }

        listAggregator.transactions()
            .subscribe {
                State.transactions.clear()
                State.transactions.addAll(it)
            }
            .disposeWhenDestroyed(lifecycle)
    }

    override fun onBackPressed() {
        State.currentScreen = Screen.List
    }
}
