package simple.payment.tracker

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

inline fun <reified T : Any> Moshi.listAdapter(): JsonAdapter<List<T>> {
    return adapter(
        Types.newParameterizedType(
            List::class.java,
            T::class.java
        )
    )

}