package simple.payment.tracker.koin

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import simple.payment.tracker.AmazonPaymentsRepository
import simple.payment.tracker.AutomaticPaymentsRepository
import simple.payment.tracker.MonthlyStatistics
import simple.payment.tracker.NotificationsRepository
import simple.payment.tracker.PaymentsRepository
import simple.payment.tracker.RecurrentPaymentsRepository
import simple.payment.tracker.Settings
import simple.payment.tracker.SwipedPaymentsRepository
import simple.payment.tracker.TinkoffPaymentsRepository
import simple.payment.tracker.TransactionsRepository
import simple.payment.tracker.TripStatistics

fun commonModule() = module {
  single { NotificationsRepository(logger("NotificationsRepository"), get()) }
  single { PaymentsRepository(logger("PaymentsRepository"), get(), get(named("userEmail"))) }
  single { TransactionsRepository.create(logger("TransactionsRepository"), get(), get(), get()) }
  single { AmazonPaymentsRepository(get(), logger("AmazonPaymentsRepository")) }
  single { TinkoffPaymentsRepository(get()) }
  single { RecurrentPaymentsRepository(get()) }
  single { AutomaticPaymentsRepository(get()) }
  single(named("allPayments")) {
    combine(
        get<TransactionsRepository>().transactions(),
        get<AmazonPaymentsRepository>().payments,
        get<TinkoffPaymentsRepository>().payments,
        get<RecurrentPaymentsRepository>().payments,
    ) { transactions, amazonPayments, tinkoffPayments, recurrent ->
      transactions
          .asSequence()
          .plus(amazonPayments)
          .plus(tinkoffPayments)
          .plus(recurrent)
          .filterNot { it.category == "Помощь родителям" }
          .toList()
    }
  }
  single { MonthlyStatistics(get(named("allPayments"))) }
  single { TripStatistics(get(named("allPayments"))) }
  single<DataStore<Settings>>(named("settingsStore")) {
    DataStoreFactory.create(
        serializer =
            object : Serializer<Settings> {
              override val defaultValue: Settings = Settings(theme = "SynthwaveThemeColors")

              override suspend fun readFrom(input: InputStream): Settings {
                return Json.decodeFromString(
                    Settings.serializer(), input.readBytes().decodeToString())
              }

              override suspend fun writeTo(t: Settings, output: OutputStream) {
                output.write(Json.encodeToString(Settings.serializer(), t).toByteArray())
              }
            },
        produceFile = { get<(String) -> File>(named("fs"))("settings.json") })
  }
  single<SwipedPaymentsRepository> { SwipedPaymentsRepository(get(), get(named("userEmail"))) }
}
