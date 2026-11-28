package com.eduardomzm.android_firebase.utils

import com.eduardomzm.android_firebase.model.Contact
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Operaciones de Realtime Database para los contactos.
 * Cada usuario guarda sus contactos en su propio nodo: contacts/{uid}/{key}
 */
class RealtimeManager(private val authManager: AuthManager) {

    private fun userContactsRef(): DatabaseReference {
        val uid = authManager.getCurrentUser()?.uid ?: "sin_usuario"
        return Firebase.database.reference.child("contacts").child(uid)
    }

    fun addContact(contact: Contact) {
        val ref = userContactsRef()
        val key = ref.push().key ?: return
        ref.child(key).setValue(contact.copy(key = null))
    }

    fun deleteContact(contactId: String) {
        userContactsRef().child(contactId).removeValue()
    }

    fun updateContact(contactId: String, updatedContact: Contact) {
        userContactsRef().child(contactId).setValue(updatedContact.copy(key = null))
    }

    /** Emite la lista de contactos cada vez que cambia algo en la base de datos. */
    fun getContactsFlow(): Flow<List<Contact>> = callbackFlow {
        val ref = userContactsRef()
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contacts = snapshot.children.mapNotNull { child ->
                    child.getValue(Contact::class.java)?.copy(key = child.key)
                }
                trySend(contacts)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }
}
