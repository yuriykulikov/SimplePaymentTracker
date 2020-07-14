package simple.payment.tracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.ui.core.setContent
import org.koin.android.ext.android.inject
import simple.payment.tracker.compose.Backs
import simple.payment.tracker.compose.PaymentsApp


class MainActivity : AppCompatActivity() {
    private val backs: Backs by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PaymentsApp()
        }
    }

    override fun onBackPressed() {
        backs.backPressed.onNext("MainActivity")
    }
}
