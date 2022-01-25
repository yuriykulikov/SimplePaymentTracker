package simple.payment.tracker

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import com.google.firebase.database.FirebaseDatabase
import dev.gitlive.firebase.database.database
import io.reactivex.rxkotlin.Observables
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.dsl.module
import simple.payment.tracker.compose.Backs
import simple.payment.tracker.logging.logger
import simple.payment.tracker.logging.loggerModule

class Application : Application() {
  override fun onCreate() {
    super.onCreate()
    startKoin {
      modules(
          loggerModule(),
          module {
            single { applicationContext }
            single { NotificationsRepository(logger("NotificationsRepository"), get()) }
            single { PaymentsRepository(logger("PaymentsRepository"), get()) }
            single {
              TransactionsRepository.create(logger("TransactionsRepository"), get(), get(), get())
            }
            single {
              FirebaseDatabase.getInstance().apply { setPersistenceEnabled(true) }
              dev.gitlive.firebase.Firebase.database
            }
            single { AmazonPaymentsRepository(get()) }
            single { RecurrentPaymentsRepository(get()) }
            single { AutomaticPaymentsRepository(get()) }
            single {
              MonthlyStatistics(
                  Observables.combineLatest(
                      get<TransactionsRepository>().transactions(),
                      get<AmazonPaymentsRepository>().payments,
                      get<RecurrentPaymentsRepository>().payments,
                      combineFunction = { transactions, amazonPayments, recurrent ->
                        transactions
                            .mapNotNull { it.payment }
                            .filterNot { it.category == "Помощь родителям" }
                            .plus(amazonPayments)
                            .plus(recurrent)
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
