package simple.payment.tracker.compose

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import simple.payment.tracker.firebase.FirebaseSignIn

/** Shows sign in button */
@Composable
fun SignInScreen(
    modifier: Modifier,
    firebaseSignIn: FirebaseSignIn,
) {
  Column(
      modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
    val scope = rememberCoroutineScope()
    val signInLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
            onResult = { activityResult ->
              scope.launch {
                // TODO show snackbar if this fails?
                firebaseSignIn.handleActivityResult(activityResult.data)
              }
            })

    Button(onClick = { scope.launch { signInLauncher.launch(firebaseSignIn.signInIntent()) } }) {
      Text(text = "Sign in")
    }
  }
}
