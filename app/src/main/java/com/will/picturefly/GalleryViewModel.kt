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
import kotlin.math.ceil
//网络数据加载的状态
const val DATA_STATUS_CAN_LOAD_MORE = 0
const val DATA_STATUS_NO_MORE = 1
const val DATA_STATUS_NETWORK_ERROR = 2
class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private companion object val TAG = "GalleryViewModel"


    private val _dataStatusLive = MutableLiveData<Int>()
    val dataStatusLive : LiveData<Int>
    get() = _dataStatusLive
    private val _photoListLive = MutableLiveData<List<PhotoItem>>()
    val photoListLive : LiveData<List<PhotoItem>>
    get() = _photoListLive

    //代码执行的有序性
    //随机一个关键字
    private val keyWords = arrayOf("cat","dog","car","beauty","phone","computer","flower","animal");
    private val perPage = 50
    private var currentPage = 1
    private var totalPage = 1
    private var currentKey = "cat"
    private var isNewQuery = true
    private var isLoading = false
    //解决第一次加载数据，scroll滚动到最后，应该滚动到顶部
    var needToScrollToTop = true

    init {
        resetQuery()
    }

    fun resetQuery(){
        currentPage = 1
        totalPage = 1
        currentKey = keyWords.random()
        isNewQuery = true
        fetchData()
        needToScrollToTop = true
    }

    fun fetchData(){
        if (isLoading) return

        if (currentPage > totalPage) {
            _dataStatusLive.value = DATA_STATUS_NO_MORE
            return
        }
        //放在页面大小的后面，放在在加载全部数据后，刷新页面时，刷新不到数据
        isLoading = true
        val stringRequest = StringRequest(
            Request.Method.GET,
            getUrl(),
            Response.Listener {

                with(Gson().fromJson(it,Pixabay::class.java)){
                    totalPage = ceil(totalHits.toDouble() / perPage).toInt()
                    if (isNewQuery){
                        _photoListLive.value = this.hits.asList()
                    } else {
                        // !! 两个感叹号，表示不为空的，flatten 扁平化处理，把后来到数据追加到新的数据当中，
                        _photoListLive.value = arrayListOf(_photoListLive.value!!,this.hits.asList()).flatten()
                    }
                }
                _dataStatusLive.value = DATA_STATUS_CAN_LOAD_MORE
                isLoading = false
                isNewQuery = false
                currentPage++
            },
            Response.ErrorListener {
                Log.e(TAG,it.toString())
                isLoading = false
                _dataStatusLive.value = DATA_STATUS_NETWORK_ERROR
            }
        )

        VolleySingleton.getInstance(getApplication()).requestQueue.add(stringRequest)
    }

    private fun getUrl(): String? {
        return "https://pixabay.com/api/?key=18052656-ccd8aa3aa1747309043e02819&q=${currentKey}&per_page=${perPage}&page=${currentPage}"
    }


}