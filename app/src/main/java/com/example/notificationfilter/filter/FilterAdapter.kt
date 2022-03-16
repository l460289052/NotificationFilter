package com.example.notificationfilter.filter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationfilter.database.NotificationDatabase
import com.example.notificationfilter.database.NotificationFilter
import com.example.notificationfilter.database.NotificationFilterDao
import com.example.notificationfilter.databinding.NotificationFilterBinding
import com.example.notificationfilter.toEditable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FilterAdapter(
    private var filterList: MutableList<NotificationFilter>,
    private val dao: NotificationFilterDao
) :
    RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: NotificationFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var pos: Int = 0
        private lateinit var filter: NotificationFilter

        init {
            binding.run {
                editTextMultiLine.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                    Log.v(TAG, "text changed")
                    buttonApply.visibility =
                        if (text.toString() != filter.regex) View.VISIBLE
                        else View.GONE
                })
                switchButton.setOnCheckedChangeListener { _, enabled ->
                    filter.enabled = enabled
                    GlobalScope.launch {
                        withContext(Dispatchers.IO) { dao.update(filter) }
                        withContext(Dispatchers.Main) { notifyItemChanged(pos) }
                    }
                }

                buttonApply.setOnClickListener {
                    filter.regex = editTextMultiLine.text.toString()
                    GlobalScope.launch {
                        withContext(Dispatchers.IO) { dao.update(filter) }
                        withContext(Dispatchers.Main) {
                            notifyItemChanged(pos)
                            buttonApply.visibility = View.GONE
                        }
                    }
                }

                buttonDelete.setOnClickListener {
                    GlobalScope.launch {
                        withContext(Dispatchers.IO) {
                            dao.delete(filter)
                            filterList.removeAt(pos)
                        }
                        withContext(Dispatchers.Main) {
                            notifyItemRemoved(pos)
                            notifyItemRangeChanged(pos, filterList.size)
                        }
                    }
                }
            }
        }

        fun setFilter(p: Int, f: NotificationFilter) {
            pos = p
            filter = f
            binding.run {
                textViewId.text = filter.id.toString()
                switchButton.isChecked = filter.enabled
                buttonApply.visibility = View.GONE
                editTextMultiLine.text = filter.regex.toEditable()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            NotificationFilterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.setFilter(position, filterList[position])

    override fun getItemCount(): Int = filterList.size

    companion object {
        val TAG = "NOTI-FILTER"
    }
}

