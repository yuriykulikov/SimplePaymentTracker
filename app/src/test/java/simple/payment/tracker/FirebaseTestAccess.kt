package simple.payment.tracker

import com.google.auth.oauth2.GoogleCredentials
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.reflect.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.serializer

class FirebaseTestAccess {
  private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    prettyPrint = true
  }

  private val serviceAccount = FileInputStream("service-user.json")

  private fun accessToken(): String {
    return GoogleCredentials.fromStream(serviceAccount)
        .createScoped(
            "https://www.googleapis.com/auth/firebase.database",
            "https://www.googleapis.com/auth/userinfo.email",
        )
        .refreshAccessToken()
        .tokenValue
  }

  private val ktor = HttpClient(CIO) { install(ContentNegotiation) { json(json) } }

  /** Download and memoize the data from firebase. */
  suspend inline fun <reified T> getOrLoad(
      firebaseUrl: String,
      cacheFile: String,
  ): T {
    return getOrLoad(firebaseUrl, cacheFile, serializer())
  }

  /** Download and memoize the data from firebase. */
  @OptIn(ExperimentalSerializationApi::class)
  suspend fun <T : Any> getOrLoad(
      firebaseUrl: String,
      cacheFile: String,
      serializer: KSerializer<T>,
  ): T {
    val file = File(cacheFile)
    return if (file.exists()) {
      json.decodeFromString(serializer, file.readText())
    } else {
      val data: T =
          ktor
              .get(firebaseUrl) { header("Authorization", "Bearer ${accessToken()}") }
              .bodyAsText()
              .let { json.decodeFromString(serializer, it) }

      file.canonicalFile.parentFile?.mkdirs()

      val scratchFile = File(file.absolutePath + hashCode())

      try {
        scratchFile.outputStream().use { stream ->
          json.encodeToStream(serializer, data, stream)
          stream.fd.sync()
        }
        check(scratchFile.renameTo(file)) { "Failed to rename $scratchFile to $file" }
      } catch (ex: IOException) {
        if (scratchFile.exists()) {
          scratchFile.delete()
        }
        throw ex
      }

      data
    }
  }

  suspend inline fun <reified T : Any> put(
      firebaseUrl: String,
      data: T,
  ) {
    put(firebaseUrl, data, typeInfo<T>())
  }

  suspend fun <T : Any> put(
      firebaseUrl: String,
      data: T,
      typeInfo: TypeInfo,
  ) {
    val result =
        ktor
            .put(firebaseUrl) {
              setBody(data, typeInfo)
              contentType(ContentType.Application.Json)
              header("Authorization", "Bearer ${accessToken()}")
            }
            .bodyAsText()
    println(result)
  }

  suspend inline fun <reified T : Any> post(firebaseUrl: String, data: T) {
    post(firebaseUrl, data, typeInfo<T>())
  }

  suspend fun <T : Any> post(
      firebaseUrl: String,
      data: T,
      typeInfo: TypeInfo,
  ) {
    val result =
        ktor
            .post(firebaseUrl) {
              setBody(data, typeInfo)
              contentType(ContentType.Application.Json)
              header("Authorization", "Bearer ${accessToken()}")
            }
            .bodyAsText()
    println(result)
  }
}
