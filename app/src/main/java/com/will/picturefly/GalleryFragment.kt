package com.will.picturefly

import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.fragment_gallery.*

class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel:GalleryViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gallery,container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        galleryViewModel = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(GalleryViewModel::class.java)
        val galleryAdapter = GalleryAdapter(galleryViewModel)
        recyclerView.apply {
            adapter = galleryAdapter
            layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        }


        galleryViewModel.photoListLive.observe(viewLifecycleOwner, Observer {
            if (galleryViewModel.needToScrollToTop){
            recyclerView.scrollToPosition(0)
            galleryViewModel.needToScrollToTop = false
        }
            swipeLayoutGallery.isRefreshing = false
            galleryAdapter.submitList(it)

        })

        galleryViewModel.dataStatusLive.observe(viewLifecycleOwner, Observer {
            galleryAdapter.footerViewStatus = it
            galleryAdapter.notifyItemChanged(galleryAdapter.itemCount -1)
            if (it == DATA_STATUS_NETWORK_ERROR)
                swipeLayoutGallery.isRefreshing = false
        })
        //初始化加载时执行一次，也可以放在viewmodel中做初始化加载
        //galleryViewModel.photoListLive.value?:galleryViewModel.resetQuery()

        swipeLayoutGallery.setOnRefreshListener {
            galleryViewModel.resetQuery()

        }

        //添加滚动监听，解决了，瀑布流在最后加载更多时，两个space时，会出现加载的列表错位的问题
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy < 0) return
                val layoutManager : StaggeredGridLayoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager

                val intArray = IntArray(2)
                layoutManager.findLastVisibleItemPositions(intArray)
                if (intArray[0] == galleryAdapter.itemCount -1){
                    galleryViewModel.fetchData()
                }
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.swipeIndicator ->{
                swipeLayoutGallery.isRefreshing = true
                Handler().postDelayed(Runnable { galleryViewModel.resetQuery() },1000)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}