package com.eduardomzm.android_firebase.model

/**
 * Contacto guardado en Realtime Database.
 * Los valores por defecto son necesarios para que Firebase pueda convertir los datos en este objeto.
 */
data class Contact(
    val key: String? = null,
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val uid: String = ""
)
