package simple.payment.tracker.stores

import io.reactivex.Observable

/**
 * Observable and persistent for a single value.
 */
interface DataStore<T> {
    var value: T
    fun observe(): Observable<T>
}

/**
 * Change the contents of the [DataStore] given the previous value.
 */
fun <T> DataStore<T>.modify(func: T.(prev: T) -> T) {
    value = func(value, value)
}

