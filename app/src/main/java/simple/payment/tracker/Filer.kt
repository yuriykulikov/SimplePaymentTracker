package simple.payment.tracker

import android.content.Context

class Filer(private val context: Context) {
    fun read(name: String): String? {
        return runCatching {
            context.openFileInput(name)
                .bufferedReader()
                .readText()
        }.getOrNull()
    }

    fun write(name: String, content: String) {
        context.openFileOutput(name, Context.MODE_PRIVATE)
            .bufferedWriter()
            .apply {
                write(content)
                flush()
            }.close()
    }
}