package com.example.notificationfilter

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationfilter.database.SearchLabel
import com.example.notificationfilter.database.SearchLabelDao
import com.example.notificationfilter.databinding.SearchLabelHorizontalBinding
import com.example.notificationfilter.databinding.SearchLabelVerticalBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LabelAdapter(
    private val labelList: List<SearchLabel>,
    private val callback: (Regex?) -> Unit
) : RecyclerView.Adapter<LabelAdapter.ViewHolder>() {
    inner class ViewHolder(
        val binding: SearchLabelHorizontalBinding,
        val callback: (Regex?) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.button.setOnClickListener {
                callback(searchLabel.regex.toRegex())
            }
        }

        private lateinit var searchLabel: SearchLabel
        fun setSearchLabel(s: SearchLabel) {
            searchLabel = s
            binding.button.text = searchLabel.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            SearchLabelHorizontalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            callback
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.setSearchLabel(labelList[position])

    override fun getItemCount(): Int = labelList.size

    companion object {
        val TAG = "NOTI-ACTIVITY"
    }
}
