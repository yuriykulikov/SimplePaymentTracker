package simple.payment.tracker

import io.reactivex.subjects.BehaviorSubject

fun <T : Any> BehaviorSubject<T>.modify(func: T.(T) -> T) {
    value?.let { onNext(func(it, it)) }
}