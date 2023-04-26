## Simple payment tracker

Learning project which also helps us to track our expenses

## Auth

[Firebase: Sign in with Google](https://firebase.google.com/docs/auth/android/google-signin#java)

## Access rules

[Firebase: Use conditions in Realtime Database Security Rules](https://firebase.google.com/docs/database/security/rules-conditions)
[Stackoverflow: Access only for particular users](https://stackoverflow.com/questions/47866419/firebase-database-rules-for-particular-user)

## Signing and uploading

[Android: Sign your app](https://developer.android.com/studio/publish/app-signing)

First, [create an upload key](https://developer.android.com/studio/publish/app-signing#generate-key).

Then, convert the key to base64:

```bash
openssl base64 < upload-keystore.jks | tr -d '\n' | tee upload-keystore.jks.base64.txt
```

Store as a secret in GitHub.

To create the keystore from the base64 string:

```bash
cat upload-keystore.jks.base64.txt | base64 --decode > upload-keystore.jks
```

Gradle config:

```kotlin
signingConfigs {
  create("release") {
    // You need to specify either an absolute path or include the
    // keystore file in the same directory as the build.gradle file.
    storeFile = file("upload-keystore.jks")
    storePassword = System.getenv("UPLOAD_KEYSTORE_PASSWORD")
    keyAlias = System.getenv("UPLOAD_KEYSTORE_KEY_ALIAS")
    keyPassword = System.getenv("UPLOAD_KEYSTORE_KEY_PASSWORD")
  }
}
```