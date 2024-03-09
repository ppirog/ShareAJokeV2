package com.example.test

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class JokeService : JobIntentService() {
    
    companion object {
        private const val JOB_ID = 1
        const val ACTION_JOKE_RECEIVED = "com.example.test.ACTION_JOKE_RECEIVED"
        const val EXTRA_JOKE_TEXT = "extra_joke_text"

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, JokeService::class.java, JOB_ID, work)
        }
    }

    private val apiUrl = "https://v2.jokeapi.dev/joke/Any"//Any, Misc, Programming, Dark, Pun, Spooky, Christmas

    private lateinit var notificationManager: TextNotification
    private val CHANNEL_ID = "Custom Channel"
    override fun onHandleWork(intent: Intent) {

        val joke = fetchJoke()

        val broadcastIntent = Intent(ACTION_JOKE_RECEIVED)
        broadcastIntent.putExtra(EXTRA_JOKE_TEXT, joke)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)

        Log.d("JokeService", "Joke: $joke")

        notificationManager = TextNotification(this, CHANNEL_ID)
        notificationManager.showNotification(joke, "New Joke")

    }

    private fun fetchJoke(): String {
        val url = URL(apiUrl)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection

        try {
            val inputStream = connection.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }

            val jsonObject = JSONObject(response.toString())
            val type = jsonObject.optString("type", "")

            val jokeText: String = when (type) {
                "single" -> jsonObject.optString("joke", "")
                "twopart" -> {
                    val setup = jsonObject.optString("setup", "")
                    val delivery = jsonObject.optString("delivery", "")
                    "$setup \n\n $delivery"
                }
                else -> ""
            }

            return jokeText
        } finally {
            connection.disconnect()
        }
    }
}