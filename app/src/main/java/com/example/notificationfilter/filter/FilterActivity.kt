package com.example.notificationfilter.filter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notificationfilter.database.NotificationDatabase
import com.example.notificationfilter.database.NotificationFilter
import com.example.notificationfilter.databinding.ActivityFilterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FilterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilterBinding
    private lateinit var viewAdapter: FilterAdapter

    private val db: NotificationDatabase by lazy { NotificationDatabase.getDatabase(this) }
    private var filters: MutableList<NotificationFilter> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createView()

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                filters = db.filterDao().getAll().toMutableList()
            }
            withContext(Dispatchers.Main) {
                viewAdapter = FilterAdapter(filters, db.filterDao())
                binding.recyclerViewFilter.adapter = viewAdapter
            }
        }
    }

    private fun createView() {
        binding = ActivityFilterBinding.inflate(layoutInflater)

        binding.run {
            setContentView(root)
            recyclerViewFilter.apply {
                layoutManager = LinearLayoutManager(this@FilterActivity)
                addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            }
            floatingActionButtonAddFilter.setOnClickListener {
                GlobalScope.launch {
                    NotificationFilter("").also {
                        withContext(Dispatchers.IO) {
                            it.id = db.filterDao().insert(it)[0].toInt()
                            filters.add(it)
                        }
                        withContext(Dispatchers.Main) {
                            viewAdapter.notifyItemInserted(filters.size)
                            recyclerViewFilter.smoothScrollToPosition(filters.size)
                        }
                    }

                }
            }
        }

    }

    companion object {
        val TAG = "NOTI-FILTER"
    }
}