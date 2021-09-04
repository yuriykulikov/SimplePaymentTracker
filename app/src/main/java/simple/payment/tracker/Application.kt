package simple.payment.tracker

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.rxkotlin.Observables
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.dsl.module
import simple.payment.tracker.compose.Backs

class Application : Application() {
  override fun onCreate() {
    super.onCreate()
    startKoin {
      modules(
          module {
            single { Logger() }
            single { NotificationsRepository(get(), get()) }
            single { PaymentsRepository(get(), get()) }
            single { TransactionsRepository(get(), get(), get(), get()) }
            single { FirebaseDatabase.getInstance().apply { setPersistenceEnabled(true) } }
            single { Firebase(get(), get()) }
            single { AmazonPaymentsRepository(get()) }
            single { RecurrentPaymentsRepository(get()) }
            single { AutomaticPaymentsRepository() }
            single {
              MonthlyStatistics(
                  Observables.combineLatest(
                      get<PaymentsRepository>().payments(),
                      get<AmazonPaymentsRepository>().payments,
                      get<RecurrentPaymentsRepository>().payments,
                      combineFunction = { l1, l2, l3 ->
                        (l1 + l2 + l3).filterNot { it.category == "Помощь родителям" }
                      }))
            }
            single {
              TripStatistics(
                  Observables.combineLatest(
                      get<PaymentsRepository>().payments(),
                      get<AmazonPaymentsRepository>().payments,
                      get<RecurrentPaymentsRepository>().payments,
                      combineFunction = { l1, l2, l3 -> l1 + l2 + l3 }))
            }
            single { Backs() }
            single<DataStore<Settings>> {
              DataStoreFactory.create(
                  serializer =
                      object : Serializer<Settings> {
                        override val defaultValue: Settings =
                            Settings(theme = "SynthwaveThemeColors")

                        override suspend fun readFrom(input: InputStream): Settings {
                          return Json.decodeFromString(
                              Settings.serializer(), input.readBytes().decodeToString())
                        }

                        override suspend fun writeTo(t: Settings, output: OutputStream) {
                          output.write(Json.encodeToString(Settings.serializer(), t).toByteArray())
                        }
                      },
                  produceFile = { applicationContext.filesDir.resolve("settings.txt") })
            }
          })
    }
  }
}
