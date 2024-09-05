package com.example.rmas

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser


    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }
    fun loadCurrentUserData() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullName = document.getString("fullName") ?: ""

                        val points = document.getLong("points") ?: 0L

                        _currentUser.value = User(fullName = fullName,  points = points)
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("UserViewModel", "Error loading user data", e)
                }
        }
    }


    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signup(email: String, password: String, name: String, lastName: String, phoneNumber: String, imageBitmap: Bitmap?) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty()) {
            _authState.value = AuthState.Error("Sva polja moraju biti popunjena")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userData = hashMapOf(
                            "fullName" to "$name $lastName",
                            "phoneNumber" to phoneNumber,
                            "email" to email,
                            "uid" to it.uid
                        )

                        firestore.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                imageBitmap?.let { bitmap ->
                                    uploadImageToFirebase(
                                        bitmap,
                                        onSuccess = {
                                            _authState.value = AuthState.Authenticated
                                        },
                                        onFailure = { exception ->
                                            _authState.value = AuthState.Error("Failed to upload image: ${exception.message}")
                                        }
                                    )
                                } ?: run {
                                    _authState.value = AuthState.Authenticated
                                }
                            }
                            .addOnFailureListener { e ->
                                _authState.value = AuthState.Error("Failed to save user data: ${e.message}")
                            }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Doslo je do greske")
                }
            }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    private fun uploadImageToFirebase(bitmap: Bitmap, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.putBytes(data)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
