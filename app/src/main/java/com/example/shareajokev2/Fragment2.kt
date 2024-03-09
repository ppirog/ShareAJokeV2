package com.example.shareajokev2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.test.JokeService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Fragment2 : Fragment(R.layout.fragment2) {

    private lateinit var jokeTextView: TextView
    private var lastReceivedJoke: String? = null
    private lateinit var xDbutton: ImageView
    private lateinit var heartButton: ImageView

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth


    private val jokeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == JokeService.ACTION_JOKE_RECEIVED) {
                val joke = intent.getStringExtra(JokeService.EXTRA_JOKE_TEXT)
                Log.d("Fragment2", "Received joke: $joke")

                updateJokeText(joke)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jokeTextView = view.findViewById(R.id.textView)
        xDbutton = view.findViewById(R.id.xDbutton)
        heartButton = view.findViewById(R.id.heartButton)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("jokes")

        val userId = auth.currentUser?.uid.toString()

        val userDatabaseReference = FirebaseDatabase.getInstance().reference.child("user_jokes").child(userId)

        xDbutton.setOnClickListener {
            val joke = lastReceivedJoke ?: return@setOnClickListener
            val userId = auth.currentUser?.uid ?: return@setOnClickListener

            val likesCount = 1

            addJokeToFirebase(joke, likesCount,userDatabaseReference.key ?:"")
        }

        heartButton.setOnClickListener {
            val joke = lastReceivedJoke ?: return@setOnClickListener
            val userId = auth.currentUser?.uid ?: return@setOnClickListener

            addJokeToUserDatabase(userId, joke)
        }



    }

    private fun addJokeToUserDatabase(userId: String, joke: String) {
        val userDatabaseReference = FirebaseDatabase.getInstance().reference.child("user_jokes").child(userId)

        val userJokeId = userDatabaseReference.push().key ?: return

        val userJokeData = mapOf(
            "id" to userJokeId,
            "text" to joke
        )

        userDatabaseReference.child(userJokeId).setValue(userJokeData)

        Toast.makeText(requireContext(), "SAVED", Toast.LENGTH_SHORT).show()
    }

    private fun addJokeToFirebase(joke: String, likesCount: Any, userJokeId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("jokes")

        // Utwórz nowy unikalny identyfikator dla żartu
        val jokeId = databaseReference.push().key ?: return

        // Sprawdź, czy likesCount można bezpiecznie rzutować na Int
        val likes = (likesCount as? Int) ?: 0

        val jokeData = mapOf(
            "id" to jokeId,  // Dodaj pole id
            "text" to joke,
            "likes" to likes,
            "likedBy"  to mutableListOf(userJokeId)
        )

        // Wstaw dane żartu do Firebase pod nowym identyfikatorem
        databaseReference.child(jokeId).setValue(jokeData)

        Toast.makeText(requireContext(), "SHARED", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(jokeReceiver, IntentFilter(JokeService.ACTION_JOKE_RECEIVED))
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(jokeReceiver)
        super.onStop()
    }

    private fun updateJokeText(joke: String?) {
        Log.d("Fragment2", "Updating joke text: $joke")
        jokeTextView.text = joke
        lastReceivedJoke = joke
    }
}