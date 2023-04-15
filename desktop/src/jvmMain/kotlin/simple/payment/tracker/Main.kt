package simple.payment.tracker

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import ch.qos.logback.core.ConsoleAppender
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.database
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import simple.payment.tracker.koin.commonModule
import simple.payment.tracker.logging.addAppender
import simple.payment.tracker.logging.hide
import simple.payment.tracker.logging.logback
import simple.payment.tracker.logging.patternLayoutEncoder

@Composable
fun buildWindowState() =
    rememberWindowState(size = DpSize(1024.dp, 1024.dp), position = WindowPosition(100.dp, 100.dp))

fun loggerModule(): Module = module {
  single {
    logback {
          addAppender(ConsoleAppender()) {
            patternLayoutEncoder(
                "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n")
          }
        }
        .hide()
  }
}

private fun desktopModule() = module {
  val scope = CoroutineScope(Dispatchers.Default)
  single(named("fs")) { { filename: String -> File(filename) } }

  single(named("userEmailStore")) {
    DataStoreFactory.create(
        serializer =
            object : Serializer<String> {
              override val defaultValue: String = ""

              override suspend fun readFrom(input: InputStream): String {
                return input.readBytes().decodeToString()
              }

              override suspend fun writeTo(t: String, output: OutputStream) {
                output.write(t.toByteArray())
              }
            },
        produceFile = { File("userEmail") })
  }

  single(named("userEmail")) {
    get<DataStore<String>>(named("userEmailStore"))
        .data
        .stateIn(scope, started = SharingStarted.Eagerly, "")
  }

  single {
    val refreshToken =
        Thread.currentThread().contextClassLoader.getResourceAsStream("service-user.json")

    val options =
        FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(refreshToken))
            .setDatabaseUrl("https://simplepaymenttracker.firebaseio.com/")
            .build()

    FirebaseApp.initializeApp(options)

    Firebase.database
  }
}

fun main() = application {
  val koin = startKoin { modules(loggerModule(), commonModule(), desktopModule()) }
  Window(
      title = "Simple Payment Tracker",
      onCloseRequest = ::exitApplication,
      state = buildWindowState(),
  ) {
    MaterialTheme {
      with(koin.koin) {
        AppContent(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(named("settingsStore")),
            get(named("userEmailStore")))
      }
    }
  }
}
