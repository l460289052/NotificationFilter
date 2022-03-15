package com.example.notificationfilter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationfilter.database.NotificationDatabase
import com.example.notificationfilter.database.NotificationFilter
import com.example.notificationfilter.databinding.NotificationFilterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FilterAdapter(
    private var filterList: MutableList<NotificationFilter>,
    private val db: NotificationDatabase
) :
    RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: NotificationFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            NotificationFilterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.run {
            val filter = filterList[position]
            switchButton.isChecked = filter.enabled
            editTextMultiLine.text = filter.regex.toEditable()

            editTextMultiLine.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                Log.v(TAG, "text changed")
                buttonApply.visibility =
                    if (text.toString() != filter.regex) View.VISIBLE
                    else View.GONE
            })

            switchButton.setOnCheckedChangeListener { _, enabled ->
                filter.enabled = enabled
                GlobalScope.launch {
                    withContext(Dispatchers.IO) { db.filterDao().update(filter) }
                    withContext(Dispatchers.Main) { this@FilterAdapter.notifyItemChanged(position) }
                }
            }

            buttonApply.setOnClickListener {
                filter.regex = editTextMultiLine.text.toString()
                GlobalScope.launch {
                    withContext(Dispatchers.IO) { db.filterDao().update(filter) }
                    withContext(Dispatchers.Main) {
                        this@FilterAdapter.notifyItemChanged(position)
                        buttonApply.visibility = View.GONE
                    }
                }
            }

            buttonDelete.setOnClickListener {
                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        db.filterDao().delete(filter)
                        filterList.removeAt(position)
                    }
                    withContext(Dispatchers.Main) {
                        this@FilterAdapter.notifyItemRangeRemoved(position, 1)
                    }
                }

            }
        }
    }

    override fun getItemCount(): Int = filterList.size

    companion object {
        val TAG = "NOTI-FILTER"
    }
}

