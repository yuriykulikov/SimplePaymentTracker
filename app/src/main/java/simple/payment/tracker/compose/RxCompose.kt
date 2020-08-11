package simple.payment.tracker.compose

import androidx.compose.Composable
import androidx.compose.MutableState
import androidx.compose.onCommit
import androidx.compose.state
import io.reactivex.Observable

@Composable
fun <T> Observable<T>.commitSubscribe(onNext: (T) -> Unit) {
  onCommit {
    val subscription = subscribe { onNext(it) }
    onDispose {
      subscription.dispose()
    }
  }
}

@Composable
fun <T> Observable<T>.toMutableState(initial: T): MutableState<T> {
  val state = state { initial }
  onCommit {
    val subscription = subscribe {
      state.value = it
    }
    onDispose {
      subscription.dispose()
    }
  }
  return state
}
