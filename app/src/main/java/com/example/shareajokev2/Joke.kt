package com.example.shareajokev2

data class Joke(
    val text: String? = "",
    var likes: Any? = 0,
    val id: String? = "",
    val likedBy: MutableList<String> = mutableListOf()
)



