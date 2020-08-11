package simple.payment.tracker

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.rxkotlin.Observables
import org.koin.core.context.startKoin
import org.koin.dsl.module
import simple.payment.tracker.compose.Backs
import simple.payment.tracker.stores.Filer

class Application : Application() {
  override fun onCreate() {
    super.onCreate()
    startKoin {
      modules(module {
        single {
          Moshi.Builder().add(PaymentAdapter()).add(KotlinJsonAdapterFactory()).build()
        }
        single { Logger() }
        single { Filer(applicationContext) }
        single { NotificationsRepository(get(), get(), get(), get()) }
        single { PaymentsRepository(get(), get(), get(), get()) }
        single { ListAggregator(get(), get(), get()) }
        single { FirebaseDatabase.getInstance().apply { setPersistenceEnabled(true) } }
        single { Firebase(get(), get()) }
        single { AmazonPaymentsRepository(get()) }
        single { RecurrentPaymentsRepository(get()) }
        single {
          MonthlyStatistics(
            Observables.combineLatest(
              get<PaymentsRepository>().payments(),
              get<AmazonPaymentsRepository>().payments,
              get<RecurrentPaymentsRepository>().payments,
              combineFunction = { l1, l2, l3 ->
                (l1 + l2 + l3).filterNot { it.category == "Помощь родителям" }
              }
            )
          )
        }
        single { Backs() }
      })
    }
  }
}
