package com.example.shareajokev2


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.example.test.JokeService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private val handler = Handler()
    private val serviceDelayInSeconds = 100L
    private lateinit var fragmentContainer: FragmentContainerView
    private lateinit var listOfJokesButton: Button
    private lateinit var jokeButton: Button
    private lateinit var logout: Button
    private lateinit var myListOfJokes: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        scheduleJokeService()

        fragmentContainer = findViewById(R.id.fragmentContainer)
        listOfJokesButton = findViewById(R.id.listOfJokes)
        jokeButton = findViewById(R.id.joke)
        logout = findViewById(R.id.logout)
        myListOfJokes = findViewById(R.id.moja_lista)

        if (!isUserLoggedIn()) {

            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        showFragment(Fragment2())

        listOfJokesButton.setOnClickListener {
            showFragment(Fragment1())
        }
        myListOfJokes.setOnClickListener {
            showFragment(Fragment3())
        }


        logout.setOnClickListener {

            firebaseAuth.signOut()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        jokeButton.setOnClickListener {
            val intent = Intent(this@MainActivity, JokeService::class.java)
            JokeService.enqueueWork(this@MainActivity, intent)
            showFragment(Fragment2())

        }

    }
    private fun createUserCollection() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userCollection = firestore.collection("users").document(currentUser.uid)

        }
    }
    private fun saveUserData(data: Map<String, Any>) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userCollection = firestore.collection("users").document(currentUser.uid)
            userCollection.set(data)
        }
    }
    private fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun scheduleJokeService() {
        val runnable = object : Runnable {
            override fun run() {

                val intent = Intent(this@MainActivity, JokeService::class.java)
                JokeService.enqueueWork(this@MainActivity, intent)

                handler.postDelayed(this, serviceDelayInSeconds * 1000)
            }
        }

        handler.post(runnable)
    }

}
