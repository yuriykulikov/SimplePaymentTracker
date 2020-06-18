package simple.payment.tracker

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.koin.core.context.startKoin
import org.koin.dsl.module

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(module {
                single { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
                single { Logger() }
                single { Filer(applicationContext) }
                single { NotificationsRepository(get(), get(), get()) }
                single { PaymentsRepository(get(), get(), get()) }
                single { NotificationAdapter(get()) }
                single { ListAggregator(get(), get(), get()) }
            })
        }
    }
}
