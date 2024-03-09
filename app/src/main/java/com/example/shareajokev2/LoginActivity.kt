package com.example.shareajokev2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmailAddress: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var registerButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        editTextEmailAddress = findViewById(R.id.editTextTextEmailAddress2)
        editTextPassword = findViewById(R.id.editTextTextPassword2)
        buttonLogin = findViewById(R.id.go)
        registerButton = findViewById(R.id.register)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        registerButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        buttonLogin.setOnClickListener {
            val email = editTextEmailAddress.text.toString()
            val password = editTextPassword.text.toString()

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("LoginActivity", "Authentication successful")
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                        createUserCollection()
                    } else {
                        Log.w("LoginActivity", "Authentication failed: ${task.exception}")
                        Toast.makeText(
                            this@LoginActivity, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun createUserCollection() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userCollection = firestore.collection("users").document(currentUser.uid)
            userCollection.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (!document.exists()) {
                        userCollection.set(hashMapOf("initialKey" to "initialValue"))
                    }
                }
            }
        }
    }
}
