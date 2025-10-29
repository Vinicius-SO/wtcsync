package br.com.fiap.wtcsync.data.firebase


import br.com.fiap.wtcsync.data.model.enums.UserRole
import br.com.fiap.wtcsync.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreDataSource(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    suspend fun saveUser(user: User) {
        val data = mapOf(
            "uid" to user.uid,
            "name" to user.name,
            "email" to user.email,
            "role" to user.role.name
        )
        usersCollection.document(user.uid).set(data).await()
    }

    suspend fun getUser(uid: String): User? {
        val doc = usersCollection.document(uid).get().await()
        return if (doc.exists()) {
            User(
                uid = doc.getString("uid") ?: "",
                name = doc.getString("name"),
                email = doc.getString("email"),
                role = UserRole.valueOf(doc.getString("role") ?: "CLIENTE")
            )
        } else null
    }
}