package com.eduardomzm.android_firebase.utils

import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
class AuthManager {
    private val auth: FirebaseAuth by lazy { Firebase.auth }

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

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
