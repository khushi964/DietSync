package com.example.dietsync.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.android.gms.tasks.Task

data class MealEntry(
    var time: Long = 0L,
    var meal: String = ""
)

object FirebaseManager {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // -------------------------------
    // Authentication
    // -------------------------------

    fun signUp(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }


    fun saveMeal(
        day: String,
        meal: MealEntry,
        onResult: (Boolean, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onResult(false, "User not logged in")

        val mealData = hashMapOf(
            "time" to meal.time,
            "meal" to meal.meal
        )

        db.collection("users")
            .document(userId)
            .collection("meals")
            .document(day)
            .collection("entries")
            .add(mealData)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    fun getMeals(
        day: String,
        onResult: (Boolean, List<MealEntry>?, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return onResult(false, null, "User not logged in")

        db.collection("users")
            .document(userId)
            .collection("meals")
            .document(day)
            .collection("entries")
            .get()
            .addOnSuccessListener { snapshot: QuerySnapshot ->
                val meals = snapshot.documents.mapNotNull { doc ->
                    val time = doc.getLong("time") ?: 0L
                    val meal = doc.getString("meal") ?: ""
                    MealEntry(time, meal)
                }
                onResult(true, meals, null)
            }
            .addOnFailureListener { e ->
                onResult(false, null, e.message)
            }
    }
}