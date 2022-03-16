package com.example.notificationfilter.label

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notificationfilter.R
import com.example.notificationfilter.database.NotificationDatabase
import com.example.notificationfilter.database.NotificationFilter
import com.example.notificationfilter.database.SearchLabel
import com.example.notificationfilter.databinding.ActivityLabelBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LabelActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLabelBinding
    private lateinit var viewAdapter: LabelAdapter

    private val db: NotificationDatabase by lazy { NotificationDatabase.getDatabase(this) }
    private var labels: MutableList<SearchLabel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createView()

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                labels = db.labelDao().getAll().toMutableList()
            }
            withContext(Dispatchers.Main) {
                viewAdapter = LabelAdapter(labels, db.labelDao())
                binding.recyclerViewLabelVertical.adapter = viewAdapter
            }
        }
    }

    private fun createView() {
        binding = ActivityLabelBinding.inflate(layoutInflater)

        binding.run {
            setContentView(binding.root)
            recyclerViewLabelVertical.apply {
                layoutManager = LinearLayoutManager(this@LabelActivity)
                addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            }
            floatingActionButtonAddLabel.setOnClickListener {
                GlobalScope.launch {
                    SearchLabel(
                        "",
                        "",
                        if (labels.isNotEmpty()) labels.minOf { it.order_val } - 1 else 0)
                        .also {
                            withContext(Dispatchers.IO) {
                                it.id = db.labelDao().insert(it)[0].toInt()
                                labels.add(0, it)
                            }
                            withContext(Dispatchers.Main) {
                                viewAdapter.notifyItemInserted(0)
                                recyclerViewLabelVertical.smoothScrollToPosition(0)
                            }
                        }

                }
            }

        }
    }

    companion object {
        val TAG = "NOTI-LABEL"
    }
}