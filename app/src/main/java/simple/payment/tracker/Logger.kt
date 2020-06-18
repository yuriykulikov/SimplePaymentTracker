package simple.payment.tracker

import android.util.Log

class Logger {
    fun debug(supplier: () -> String) {
        Log.d("PAYMENTS", supplier())
    }
}