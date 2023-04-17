package simple.payment.tracker.firebase

import android.content.Context
import android.content.Intent
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import simple.payment.tracker.R
import simple.payment.tracker.logging.Logger

/**
 * ## FirebaseSignIn
 *
 * Sign in to Firebase with Google credentials
 *
 * Tracks signed in user and provides functions to sign in and out using Google auth.
 *
 * ### Usage
 * ```
 * val signInLauncher =
 *   rememberLauncherForActivityResult(
 *       contract = ActivityResultContracts.StartIntentSenderForResult(),
 *       onResult = { activityResult ->
 *         scope.launch {
 *           firebaseSignIn.handleActivityResult(activityResult.data)
 *         }
 *       })
 *
 * Button(onClick = { scope.launch { signInLauncher.launch(firebaseSignIn.signInIntent()) } }) {
 *   Text(text = "Sign in")
 * }
 * ```
 */
class FirebaseSignIn(
    val logger: Logger,
    val context: Context,
) {
  private val signInClient
    get() = Identity.getSignInClient(context)

  private val oneTapRequest: BeginSignInRequest
    get() =
        BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(true)
                    .build())
            .build()

  private val regularSignInRequest: GetSignInIntentRequest
    get() =
        GetSignInIntentRequest.builder()
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .build()

  private val signedInEmailState = MutableStateFlow(Firebase.auth.currentUser?.email)

  fun signedInUserEmail(): StateFlow<String?> {
    return signedInEmailState
  }

  suspend fun signInIntent(): IntentSenderRequest {
    return signInClient
        .beginSignIn(oneTapRequest)
        .runCatching { await().pendingIntent }
        .recover {
          // one-tap failed
          signInClient.getSignInIntent(regularSignInRequest).await()
        }
        .map { pendingIntent -> IntentSenderRequest.Builder(pendingIntent).build() }
        .getOrThrow()
  }

  suspend fun handleActivityResult(activityResultData: Intent?): Result<String> {
    return runCatching { signInClient.getSignInCredentialFromIntent(activityResultData) }
        .mapCatching { signInCredential ->
          val idToken = signInCredential.googleIdToken ?: error("credential.googleIdToken is null")
          val firebaseCredential = GoogleAuthProvider.credential(idToken, null)
          val authResult = Firebase.auth.signInWithCredential(firebaseCredential)
          authResult.user?.email ?: "unknown email"
        }
        .onSuccess { email ->
          logger.debug { "Signed in as $email" }
          signedInEmailState.value = email
        }
        .onFailure {
          if (it is ApiException) {
            logger.error { "Google sign in failed: $it" }
          } else {
            logger.error { "Firebase sign in failed: $it" }
          }
        }
  }

  suspend fun signOut() {
    Firebase.auth.signOut()
    signedInEmailState.value = null
  }
}
