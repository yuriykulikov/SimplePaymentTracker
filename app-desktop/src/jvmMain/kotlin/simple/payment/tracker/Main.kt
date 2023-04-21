@file:OptIn(ExperimentalComposeUiApi::class)

package simple.payment.tracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.UnsynchronizedAppenderBase
import ch.qos.logback.core.rolling.RollingFileAppender
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import simple.payment.tracker.firebase.firebaseRepositoriesModule
import simple.payment.tracker.koin.commonModule
import simple.payment.tracker.logging.LoggerFactory
import simple.payment.tracker.logging.addAppender
import simple.payment.tracker.logging.hide
import simple.payment.tracker.logging.logback
import simple.payment.tracker.logging.patternLayoutEncoder
import simple.payment.tracker.logging.timeBasedRollingPolicy

@Composable
fun buildWindowState() =
    rememberWindowState(
        size = DpSize(width = 1536.dp, height = 1024.dp), position = WindowPosition(100.dp, 100.dp))

fun loggerModule(): Module = module {
  val mutableStateFlow = MutableStateFlow(emptyList<String>())
  single<Flow<List<String>>>(named("logs")) { mutableStateFlow }
  single {
    logback {
          addAppender(ConsoleAppender()) {
            patternLayoutEncoder(
                "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n")
          }
          addAppender(RollingFileAppender(), async = true) {
            isAppend = true
            rollingPolicy = timeBasedRollingPolicy {
              fileNamePattern = "build/rolling-%d{yyyy-MM-dd}.log"
              maxHistory = 3
              isCleanHistoryOnStart = true
            }

            encoder =
                patternLayoutEncoder(
                    "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n")
          }

          addAppender(
              object : UnsynchronizedAppenderBase<ILoggingEvent>() {
                override fun append(eventObject: ILoggingEvent?) {
                  if (eventObject != null) {
                    mutableStateFlow.update { prev ->
                      (prev + eventObject.formattedMessage).takeLast(100)
                    }
                  }
                }
              }) {
                patternLayoutEncoder("%-5level %logger{36} - %msg%n")
              }

          getLogger("com.google.firebase").level = Level.WARN
          getLogger("io.netty").level = Level.WARN
        }
        .hide()
  }
}

private fun desktopModule() = module {
  val scope = CoroutineScope(Dispatchers.Default)
  single(named("fs")) { { filename: String -> File("build/$filename") } }
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
        produceFile = { get<(String) -> File>(named("fs"))("userEmail") })
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

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
  val koin = startKoin {
    modules(
        loggerModule(),
        commonModule(),
        desktopModule(),
        firebaseRepositoriesModule(),
    )
  }
  val keyPresses = MutableSharedFlow<KeyEvent>(extraBufferCapacity = 1)
  Window(
      title = "Simple Payment Tracker",
      onCloseRequest = ::exitApplication,
      state = buildWindowState(),
      onKeyEvent = {
        when {
          (it.key == Key.Escape && it.type == KeyEventType.KeyUp) -> {
            keyPresses.tryEmit(it)
            true
          }
          else -> false
        }
      },
  ) {
    with(koin.koin) {
      val prev = Thread.getDefaultUncaughtExceptionHandler()
      Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        get<LoggerFactory>().createLogger("main").error(throwable) {
          "Uncaught exception in thread $thread"
        }
        prev.uncaughtException(thread, throwable)
      }
      AppContent(
          get(),
          get(),
          get(),
          get(),
          get(),
          get(),
          get(named("settingsStore")),
          get(named("userEmailStore")),
          get(named("logs")),
          keyPresses,
      )
    }
  }
}
