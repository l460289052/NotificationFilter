package com.example.notificationfilter

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
                viewAdapter = FilterAdapter(filters, db)
                binding.recyclerViewFilter.adapter = viewAdapter
            }
        }
    }

    private fun createView() {
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.run {
            recyclerViewFilter.apply {
                layoutManager = LinearLayoutManager(this@FilterActivity)
                addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            }
        }

    }

    companion object {
        val TAG = "NOTI-FILTER"
    }
}