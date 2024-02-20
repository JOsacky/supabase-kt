package io.github.jan.supabase.compose.auth.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.applicationContext
import io.github.jan.supabase.compose.auth.getActivity
import io.github.jan.supabase.compose.auth.getGoogleIDOptions
import io.github.jan.supabase.compose.auth.signInWithGoogle
import io.ktor.util.Digest

/**
 * Composable function that implements Native Google Auth.
 *
 * On unsupported platforms it will use the [fallback]
 *
 * @param onResult Callback for the result of the login
 * @param fallback Fallback function for unsupported platforms
 * @return [NativeSignInState]
 */
@Composable
actual fun ComposeAuth.rememberSignInWithGoogle(onResult: (NativeSignInResult) -> Unit, fallback: suspend () -> Unit): NativeSignInState {
    return signInWithCM(onResult, fallback)
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
internal fun ComposeAuth.signInWithCM(onResult: (NativeSignInResult) -> Unit, fallback: suspend () -> Unit): NativeSignInState{
    val state = remember { NativeSignInState(serializer) }
    val context = LocalContext.current

    LaunchedEffect(key1 = state.status) {
        if (state.status is NativeSignInStatus.Started) {
            val activity = context.getActivity()
            val status = state.status as NativeSignInStatus.Started
            try {
                if (activity != null && config.googleLoginConfig != null) {
                    val digest = Digest("SHA-256")
                    digest += status.nonce!!.toByteArray()
                    val hashedNonce = digest.build().toHexString()
                    val response = makeRequest(context, activity, config, hashedNonce)
                    if(response == null) {
                        onResult.invoke(NativeSignInResult.ClosedByUser)
                        return@LaunchedEffect
                    }
                    when (response.credential) {
                        is CustomCredential -> {
                            if (response.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                try {
                                    val googleIdTokenCredential =
                                        GoogleIdTokenCredential.createFrom(response.credential.data)
                                    signInWithGoogle(googleIdTokenCredential.idToken, status.nonce, status.extraData)
                                    onResult.invoke(NativeSignInResult.Success)
                                } catch (e: GoogleIdTokenParsingException) {
                                    onResult.invoke(
                                        NativeSignInResult.Error(
                                            e.localizedMessage ?: "Google id parsing exception",
                                            e
                                        )
                                    )
                                } catch(e: Exception) {
                                    onResult.invoke(
                                        NativeSignInResult.Error(
                                            e.localizedMessage ?: "error",
                                            e
                                        )
                                    )
                                }
                            } else {
                                onResult.invoke(NativeSignInResult.Error("Unexpected type of credential"))
                            }
                        } else -> {
                            onResult.invoke(NativeSignInResult.Error("Unsupported credentials"))
                        }
                    }
                } else {
                    fallback.invoke()
                }
            } catch (e: GetCredentialException) {
                when (e) {
                    is GetCredentialCancellationException -> onResult.invoke(NativeSignInResult.ClosedByUser)
                    else -> onResult.invoke(
                        NativeSignInResult.Error(
                            e.localizedMessage ?: "Credential exception",
                            e
                        )
                    )
                }
            } catch (e: Exception) {
                onResult.invoke(NativeSignInResult.Error(e.localizedMessage ?: "error", e))
            } finally {
                state.reset()
            }
        }
    }

    return state
}

internal actual suspend fun handleGoogleSignOut() {
    CredentialManager.create(applicationContext()).clearCredentialState(ClearCredentialStateRequest())
}

private suspend fun tryRequest(
    context: android.content.Context,
    activity: android.app.Activity,
    config: ComposeAuth.Config,
    nonce: String?,
    withFilter: Boolean
): GetCredentialResponse {
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(getGoogleIDOptions(config.googleLoginConfig, withFilter, nonce))
        .build()
    return CredentialManager.create(context).getCredential(activity, request)
}

private suspend fun makeRequest(
    context: android.content.Context,
    activity: android.app.Activity,
    config: ComposeAuth.Config,
    nonce: String?
): GetCredentialResponse? {
    return try {
        tryRequest(context, activity, config, nonce, true)
    } catch(e: GetCredentialCancellationException) {
        return null
    } catch(e: GetCredentialException) {
        tryRequest(context, activity, config, nonce, false)
    }
}