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
import simple.payment.tracker.MonthlyStatistics
import simple.payment.tracker.RecurringPayments
import simple.payment.tracker.Settings
import simple.payment.tracker.TinkoffPaymentsRepository
import simple.payment.tracker.Transactions
import simple.payment.tracker.TripStatistics

fun commonModule() = module {
  single { Transactions.create(logger("TransactionsRepository"), get(), get(), get()) }
  single { RecurringPayments(get()) }
  single(named("allPayments")) {
    combine(
        get<Transactions>().transactions(),
        get<AmazonPaymentsRepository>().payments,
        get<TinkoffPaymentsRepository>().payments,
        get<RecurringPayments>().payments,
    ) { transactions, amazonPayments, tinkoffPayments, recurring ->
      transactions
          .asSequence()
          .plus(amazonPayments)
          .plus(tinkoffPayments)
          .plus(recurring)
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
}
