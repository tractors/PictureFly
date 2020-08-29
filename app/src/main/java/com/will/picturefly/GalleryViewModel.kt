package com.will.picturefly

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private companion object val TAG = "GalleryViewModel"
    private val _photoListLive = MutableLiveData<List<PhotoItem>>()
    val photoListLive : LiveData<List<PhotoItem>>
    get() = _photoListLive

    fun fetchData(){
        val stringRequest = StringRequest(
            Request.Method.GET,
            getUrl(),
            Response.Listener {
                _photoListLive.value = Gson().fromJson(it,Pixabay::class.java).hits.asList()
            },
            Response.ErrorListener {
                Log.e(TAG,it.toString())
            }
        )

        VolleySingleton.getInstance(getApplication()).requestQueue.add(stringRequest)
    }

    private fun getUrl(): String? {
        return "https://pixabay.com/api/?key=18052656-ccd8aa3aa1747309043e02819&q=${keyWords.random()}&per_page=100"
    }

    //随机一个关键字
    private val keyWords = arrayOf("cat","dog","car","beauty","phone","computer","flower","animal");
}