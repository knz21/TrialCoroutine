package com.knz21.apikicker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import kotlin.coroutines.experimental.suspendCoroutine

class MainActivity : AppCompatActivity() {
    private val service: ApiService by lazy {
        Retrofit.Builder().baseUrl("https://api.github.com")
                .addConverterFactory(MoshiConverterFactory.create()).build().create(ApiService::class.java)
    }
    private val listView: ListView by lazy { findViewById<ListView>(R.id.list) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.button).setOnClickListener { kick() }
    }

    private fun kick() {
        launch(UI) {
            listView.adapter = ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1,
                    service.getRepos().await().map { it.name })
        }
    }
}

internal interface ApiService {
    @GET("/users/knz21/repos")
    fun getRepos(): Call<List<Repository>>
}

internal data class Repository(var name: String)

internal suspend fun Call<List<Repository>>.await(): List<Repository> =
        suspendCoroutine { c ->
            enqueue(object : Callback<List<Repository>> {
                override fun onResponse(call: Call<List<Repository>>?, response: Response<List<Repository>>?) {
                    response?.body()?.let { c.resume(it) } ?: onFailure(null, null)
                }

                override fun onFailure(call: Call<List<Repository>>?, t: Throwable?) {
                    Log.v("error", t?.toString() ?: "error")
                }
            })
        }