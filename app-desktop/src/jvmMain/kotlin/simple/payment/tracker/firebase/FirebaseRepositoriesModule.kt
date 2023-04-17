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

fun firebaseRepositoriesModule() = module {
  single<NotificationsRepository> {
    FirebaseAdminNotificationsRepository(logger("NotificationsRepository"), get())
  }
  single<PaymentsRepository> {
    FirebaseAdminPaymentsRepository(logger("PaymentsRepository"), get(), get(named("userEmail")))
  }
  single<AmazonPaymentsRepository> {
    FirebaseAdminAmazonPaymentsRepository(logger("AmazonPaymentsRepository"), get())
  }
  single<TinkoffPaymentsRepository> { FirebaseAdminTinkoffPaymentsRepository(get()) }
  single<RecurringPaymentsRepository> {
    FirebaseAdminRecurringPaymentsRepository(
        logger("FirebaseAdminRecurringPaymentsRepository"), get())
  }
  single<PaymentMatcherRepository> { FirebaseAdminPaymentMatcherRepository(get()) }
  single<SwipedPaymentsRepository> {
    FirebaseAdminSwipedPaymentsRepository(get(), get(named("userEmail")))
  }
}
