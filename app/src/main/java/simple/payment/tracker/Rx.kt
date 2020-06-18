package simple.payment.tracker

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

fun <T : Any> BehaviorSubject<T>.modify(func: T.(T) -> T) {
    value?.let { onNext(func(it, it)) }
}

fun Disposable.disposeWhenDestroyed(lifecycle: Lifecycle) {
    lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            dispose()
        }
    })
}