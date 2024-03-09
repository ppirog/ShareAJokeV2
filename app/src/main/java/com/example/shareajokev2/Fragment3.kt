package com.example.shareajokev2

import android.os.Bundle
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
import com.google.firebase.database.ValueEventListener

class Fragment3 : Fragment(R.layout.fragment3) {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<Joke>
    private val userJokes = mutableListOf<Joke>()
    private lateinit var userDatabaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.listView2)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return

        // Utwórz odniesienie do bazy danych z żartami użytkownika
        userDatabaseReference =
            FirebaseDatabase.getInstance().reference.child("user_jokes").child(userId)

        // Pobierz dane z Firebase
        fetchUserJokesFromFirebase()

        adapter = object : ArrayAdapter<Joke>(
            requireContext(),
            R.layout.list_item_layout2,
            R.id.textView,
            userJokes
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)

                val textView: TextView = view.findViewById(R.id.textView)
                val imageViewLike: ImageView = view.findViewById(R.id.imageViewLike)
                val counterTextView: TextView = view.findViewById(R.id.counter)

                val currentLikes = userJokes[position].likes
                counterTextView.text = ""

                textView.text = userJokes[position].text


                userJokes[position].id?.let { jokeId ->
                    imageViewLike.setOnClickListener {
                        val jokeReference = userDatabaseReference.child(jokeId)
                        jokeReference.removeValue()
                        Toast.makeText(requireContext(), "DELETED", Toast.LENGTH_SHORT).show()
                    }
                }


                return view
            }
        }

        listView.adapter = adapter
    }


    private fun fetchUserJokesFromFirebase() {
        userDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userJokes.clear()
                for (snapshot in dataSnapshot.children) {
                    val joke = snapshot.getValue(Joke::class.java)
                    joke?.let {
                        val likesValue = when (val likes = it.likes) {
                            is Long -> likes.toInt()
                            is Int -> likes
                            else -> 0
                        }
                        it.likes = likesValue
                        userJokes.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }
}
