package simple.payment.tracker.stores

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import simple.payment.tracker.Logger
import java.time.LocalDate

class FileDataStore<T : Any> private constructor(
  private val name: String,
  private val filer: Filer,
  private val adapter: JsonAdapter<T>,
  initial: T
) : DataStore<T> {
  private val subject: BehaviorSubject<T> = BehaviorSubject.createDefault(initial)

  override var value: T
    get() = requireNotNull(subject.value)
    set(value) {
      subject.onNext(value)
      filer.sink(name).use { sink ->
        adapter.toJson(sink, value)
      }
    }

  override fun observe(): Observable<T> {
    return subject.hide()
  }

  fun dump(logger: Logger) {
    logger.debug { "Dump $name" }
    adapter.toJson(value)
      .lines()
      .forEach {
        logger.debug { it }
      }
  }

  companion object {
    fun <T> listDataStore(
      filer: Filer,
      name: String,
      clazz: Class<T>,
      defaultValue: String,
      moshi: Moshi
    ): FileDataStore<List<T>> {
      val adapter: JsonAdapter<List<T>> = moshi
        .adapter<List<T>>(Types.newParameterizedType(List::class.java, clazz))
        .indent("  ")

      val initial: List<T> = filer.source(name)
        ?.use {
          adapter.fromJson(it)
        }
        ?: requireNotNull(adapter.fromJson(defaultValue))

      filer.source(name)?.use { source ->
        filer.sink(name = "${LocalDate.now()}_name").use { sink ->
          sink.writeAll(source)
        }
      }

      return FileDataStore(name, filer, adapter, initial)
    }
  }
}

inline fun <reified T : Any> FileDataStore.Companion.listDataStore(
  filer: Filer,
  name: String,
  defaultValue: String,
  moshi: Moshi
) = listDataStore(filer, name, T::class.java, defaultValue, moshi)

