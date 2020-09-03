package simple.payment.tracker.compose


import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
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
fun <T> Observable<T>.toState(initial: T): State<T> {
  val state = remember { mutableStateOf(initial) }
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
