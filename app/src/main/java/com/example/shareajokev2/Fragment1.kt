package com.example.shareajokev2

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class Fragment1 : Fragment(R.layout.fragment1) {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<Joke>
    private val jokesFromFirebase = mutableListOf<Joke>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var userDatabaseReference: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.listView)
        databaseReference = FirebaseDatabase.getInstance().getReference("jokes")
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser?.uid ?: return
        userDatabaseReference =
            FirebaseDatabase.getInstance().reference.child("user_jokes").child(userId)

        fetchAndSortJokesFromFirebase()

        adapter = object : ArrayAdapter<Joke>(
            requireContext(),
            R.layout.list_item_layout,
            R.id.textView,
            jokesFromFirebase
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)

                val textView: TextView = view.findViewById(R.id.textView)
                val imageViewLike: ImageView = view.findViewById(R.id.imageViewLike)
                val counterTextView: TextView = view.findViewById(R.id.counter)

                val currentLikes = jokesFromFirebase[position].likes
                counterTextView.text = currentLikes.toString()

                imageViewLike.setOnClickListener {
                    val currentJoke = jokesFromFirebase[position]

                    val currentLikes = try {
                        (currentJoke.likes as? Number)?.toInt() ?: 0
                    } catch (e: ClassCastException) {
                        0
                    }

                    val currentUser = userDatabaseReference.key ?: ""

                    if (!currentJoke.likedBy.toString().contains(currentUser)) {
                        currentJoke.likes = currentLikes + 1

                        currentJoke.likedBy.add(currentUser)

                        databaseReference.child(currentJoke.id ?: "").child("likedBy")
                            .setValue(currentJoke.likedBy)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("Firebase", "LikedBy list updated successfully")
                                } else {
                                    // Obsłuż błąd aktualizacji danych w Firebase
                                    Log.e("Firebase", "Failed to update likedBy in Firebase")
                                }
                            }

                    } else {
                        Toast.makeText(requireContext(), "YOU LIKED IT", Toast.LENGTH_SHORT).show()

                    }

                    Log.d("zxcxzc", currentUser)
                    Log.d("zxcxzc", currentJoke.likedBy.toString())

                    databaseReference.child(currentJoke.id ?: "").child("likes")
                        .setValue(currentJoke.likes)

                    counterTextView.text = currentJoke.likes.toString()

                    fetchAndSortJokesFromFirebase()
                }

                textView.text = jokesFromFirebase[position].text
                return view
            }
        }

        listView.adapter = adapter
    }

    private fun removeJokeFromUserDatabase(userId: String, userJokeId: String) {
        val userDatabaseReference =
            FirebaseDatabase.getInstance().reference.child("user_jokes").child(userId)

        userDatabaseReference.child(userJokeId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "REMOVED", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to remove joke", Toast.LENGTH_SHORT).show()
            }
    }


    private fun addJokeToUserDatabase(userId: String, joke: String) {
        val userDatabaseReference =
            FirebaseDatabase.getInstance().reference.child("user_jokes").child(userId)

        val userJokeId = userDatabaseReference.push().key ?: return

        val userJokeData = mapOf(
            "id" to userJokeId,
            "text" to joke
        )

        userDatabaseReference.child(userJokeId).setValue(userJokeData)

        Toast.makeText(requireContext(), "LIKED AND SAVED", Toast.LENGTH_SHORT).show()
    }

    private fun fetchAndSortJokesFromFirebase() {
        val query: Query = databaseReference.orderByChild("likes").limitToLast(1000)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val reversedJokes = mutableListOf<Joke>()
                for (snapshot in dataSnapshot.children.reversed()) {
                    val joke = snapshot.getValue(Joke::class.java)
                    joke?.let {
                        // Sprawdź typ pola likes i przekonwertuj go na Int
                        val likesValue = when (val likes = it.likes) {
                            is Long -> likes.toInt()
                            is Int -> likes
                            else -> 0
                        }
                        it.likes = likesValue


                        reversedJokes.add(it)
                    }
                }

                jokesFromFirebase.clear()
                jokesFromFirebase.addAll(reversedJokes)

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

}
