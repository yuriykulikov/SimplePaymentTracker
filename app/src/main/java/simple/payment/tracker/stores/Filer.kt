package simple.payment.tracker.stores

import android.content.Context
import java.io.FileOutputStream
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio

class Filer(private val context: Context) {
  fun source(name: String): BufferedSource? {
    return runCatching {
          val inputStream = context.openFileInput(name)
          Okio.buffer(Okio.source(inputStream))
        }
        .getOrNull()
  }

  fun sink(name: String): BufferedSink {
    val outputStream: FileOutputStream = context.openFileOutput(name, Context.MODE_PRIVATE)
    return Okio.buffer(Okio.sink(outputStream))
  }
}
