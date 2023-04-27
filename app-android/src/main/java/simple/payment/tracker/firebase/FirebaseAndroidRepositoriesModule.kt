package simple.payment.tracker.firebase

import org.koin.core.qualifier.named
import org.koin.dsl.module
import simple.payment.tracker.AmazonPaymentsRepository
import simple.payment.tracker.NotificationsRepository
import simple.payment.tracker.PaymentMatcherRepository
import simple.payment.tracker.PaymentsRepository
import simple.payment.tracker.RecurringPaymentsRepository
import simple.payment.tracker.SwipedPaymentsRepository
import simple.payment.tracker.TinkoffPaymentsRepository
import simple.payment.tracker.koin.logger

fun firebaseAndroidRepositoriesModule() = module {
  single<NotificationsRepository> {
    FirebaseAndroidNotificationsRepository(logger("NotificationsRepository"), get())
  }
  single<PaymentsRepository> {
    FirebaseAndroidPaymentsRepository(logger("PaymentsRepository"), get(), get(named("userEmail")))
  }
  single<AmazonPaymentsRepository> {
    FirebaseAndroidAmazonPaymentsRepository(logger("AmazonPaymentsRepository"), get())
  }
  single<TinkoffPaymentsRepository> { FirebaseAndroidTinkoffPaymentsRepository(get()) }
  single<RecurringPaymentsRepository> {
    FirebaseAndroidRecurringPaymentsRepository(
        logger("FirebaseAndroidRecurringPaymentsRepository"), get())
  }
  single<PaymentMatcherRepository> { FirebaseAndroidPaymentMatcherRepository(get()) }
  single<SwipedPaymentsRepository> {
    FirebaseAndroidSwipedPaymentsRepository(get(), get(named("userEmail")))
  }
}
