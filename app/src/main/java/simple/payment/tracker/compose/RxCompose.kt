package simple.payment.tracker.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.reactivex.Observable

@Composable
fun <T> Observable<T>.CommitSubscribe(onNext: (T) -> Unit) {
  DisposableEffect(true) {
    val subscription = subscribe { onNext(it) }
    onDispose { subscription.dispose() }
  }
}

/**
 * Remembers a subscription to a flux, returns a corresponding [State].
 *
 * @param initial initial value of the [State].
 */
@Composable
fun <T> rememberRxState(initial: T, observable: () -> Observable<T>): State<T> {
  val remObservable = remember { observable() }
  val state = remember { mutableStateOf(initial) }
  // execute callback every time the input (observable) has changed
  DisposableEffect(true) {
    val subscription = remObservable.subscribe { state.value = it }
    onDispose { subscription.dispose() }
  }
  return state
}

/** Remembers a subscription to a flux, returns a corresponding [State]. */
@Composable
fun <T> rememberRxStateBlocking(observable: () -> Observable<T>): State<T> {
  val remObservable = remember { observable() }
  val state = remember { mutableStateOf(remObservable.blockingFirst()) }
  // execute callback every time the input (observable) has changed
  DisposableEffect(true) {
    val subscription = remObservable.subscribe { state.value = it }
    onDispose { subscription.dispose() }
  }
  return state
}
