package simple.payment.tracker

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import dev.gitlive.firebase.database.database
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import simple.payment.tracker.firebase.FirebaseSignIn
import simple.payment.tracker.koin.commonModule
import simple.payment.tracker.koin.logger
import simple.payment.tracker.logging.loggerModule

class Application : Application() {
  override fun onCreate() {
    super.onCreate()
    startKoin {
      modules(
          loggerModule(),
          commonModule(),
          module {
            single { applicationContext }
            single<FirebaseSignIn> { FirebaseSignIn(logger("GoogleSignIn"), get()) }
            single(named("userEmail")) { get<FirebaseSignIn>().signedInUserEmail() }
            single {
              FirebaseDatabase.getInstance().apply { setPersistenceEnabled(true) }
              dev.gitlive.firebase.Firebase.database
            }
            single(named("fs")) {
              { filename: String -> applicationContext.filesDir.resolve(filename) }
            }
          })
    }
  }
}
