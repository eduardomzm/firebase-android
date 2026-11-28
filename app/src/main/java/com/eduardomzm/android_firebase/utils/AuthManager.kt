package com.eduardomzm.android_firebase.utils

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.eduardomzm.android_firebase.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

/**
 * Resultado de una operación de autenticación: éxito con un dato o error con su mensaje.
 */
sealed class AuthRes<out T> {
    data class Success<T>(val data: T) : AuthRes<T>()
    data class Error(val errorMessage: String) : AuthRes<Nothing>()
}

/**
 * Concentra todas las operaciones de Firebase Authentication de la app.
 */
class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth by lazy { Firebase.auth }
    private val credentialManager by lazy { CredentialManager.create(context) }

    suspend fun signInAnonymously(): AuthRes<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            AuthRes.Success(result.user ?: throw Exception("Error al iniciar sesión"))
        } catch (e: Exception) {
            AuthRes.Error(e.message ?: "Error al iniciar sesión")
        }
    }

    suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthRes<FirebaseUser?> {
        if (email.isBlank() || password.isBlank()) return AuthRes.Error("Escribe tu correo y contraseña")
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            AuthRes.Success(authResult.user)
        } catch (e: Exception) {
            AuthRes.Error(friendlyMessage(e, "Error al crear el usuario"))
        }
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthRes<FirebaseUser?> {
        if (email.isBlank() || password.isBlank()) return AuthRes.Error("Escribe tu correo y contraseña")
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            AuthRes.Success(authResult.user)
        } catch (e: Exception) {
            AuthRes.Error(friendlyMessage(e, "Error al iniciar sesión"))
        }
    }

    suspend fun resetPassword(email: String): AuthRes<Unit> {
        if (email.isBlank()) return AuthRes.Error("Escribe tu correo electrónico")
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            AuthRes.Success(Unit)
        } catch (e: Exception) {
            AuthRes.Error(friendlyMessage(e, "Error al restablecer la contraseña"))
        }
    }

    /**
     * Muestra el selector de cuentas de Google y con el token obtenido inicia sesión en Firebase.
     * [activityContext] debe ser el contexto de la Activity (LocalContext en Compose).
     */
    suspend fun signInWithGoogle(activityContext: Context): AuthRes<FirebaseUser?> {
        return try {
            val googleOption = GetSignInWithGoogleOption
                .Builder(context.getString(R.string.default_web_client_id))
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build()

            val credential = credentialManager.getCredential(activityContext, request).credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                AuthRes.Success(authResult.user)
            } else {
                AuthRes.Error("Tipo de credencial no soportado")
            }
        } catch (e: GetCredentialCancellationException) {
            AuthRes.Error("Inicio de sesión con Google cancelado")
        } catch (e: NoCredentialException) {
            AuthRes.Error("No hay cuentas de Google en este dispositivo")
        } catch (e: Exception) {
            AuthRes.Error(friendlyMessage(e, "Error al iniciar sesión con Google"))
        }
    }

    /** Traduce los errores de Firebase a mensajes en español para el usuario. */
    private fun friendlyMessage(e: Exception, default: String): String = when (e) {
        is FirebaseNetworkException -> "Sin conexión a internet"
        is FirebaseTooManyRequestsException -> "Demasiados intentos, espera un momento"
        is FirebaseAuthException -> when (e.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato válido"
            "ERROR_WEAK_PASSWORD" -> "La contraseña debe tener al menos 6 caracteres"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe una cuenta con ese correo"
            "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" ->
                "Correo o contraseña incorrectos"
            "ERROR_USER_DISABLED" -> "Esta cuenta está deshabilitada"
            else -> e.message ?: default
        }
        else -> e.message ?: default
    }

    suspend fun signOut() {
        auth.signOut()
        // Olvida la cuenta de Google elegida para que la próxima vez vuelva a preguntar
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
