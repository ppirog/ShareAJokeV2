package com.example.shareajokev2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextEmailAddress: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonRegister: Button
    private lateinit var backButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        editTextEmailAddress = findViewById(R.id.editTextTextEmailAddressRegister)
        editTextPassword = findViewById(R.id.editTextTextPasswordRegister)
        buttonRegister = findViewById(R.id.createAccount)
        backButton = findViewById(R.id.back)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        backButton.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonRegister.setOnClickListener {

            val email = editTextEmailAddress.text.toString()
            val password = editTextPassword.text.toString()

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("RegisterActivity", "Registration successful")

                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()

                        createUserCollection()
                    } else {
                        Log.w("RegisterActivity", "Registration failed: ${task.exception}")
                        Toast.makeText(
                            this@RegisterActivity, "Registration failed.",
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
