package com.example.notificationfilter.label

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationfilter.database.SearchLabel
import com.example.notificationfilter.database.SearchLabelDao
import com.example.notificationfilter.databinding.SearchLabelVerticalBinding
import com.example.notificationfilter.toEditable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LabelAdapter(
    private val labelList: MutableList<SearchLabel>,
    private val dao: SearchLabelDao
) : RecyclerView.Adapter<LabelAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: SearchLabelVerticalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var pos: Int = 0

        init {
            binding.run {
                val watcher = object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        buttonApply.visibility =
                            if (editTextTextName.text.toString() != searchLabel.name ||
                                editTextTextMultiLine.text.toString() != searchLabel.regex
                            )
                                View.VISIBLE
                            else View.GONE
                    }

                    override fun afterTextChanged(p0: Editable?) {}
                }
                editTextTextName.addTextChangedListener(watcher)
                editTextTextMultiLine.addTextChangedListener(watcher)
                buttonApply.setOnClickListener {
                    searchLabel.name = editTextTextName.text.toString()
                    searchLabel.regex = editTextTextMultiLine.text.toString()

                    GlobalScope.launch {
                        withContext(Dispatchers.IO) { dao.update(searchLabel) }
                        withContext(Dispatchers.Main) {
                            notifyItemChanged(pos)
                            buttonApply.visibility = View.GONE
                        }
                    }
                }
                imageButtonDelete.setOnClickListener {
                    GlobalScope.launch {
                        withContext(Dispatchers.IO) {
                            dao.delete(searchLabel)
                            labelList.removeAt(pos)
                        }
                        withContext(Dispatchers.Main) {
                            notifyItemRemoved(pos)
                            notifyItemRangeChanged(pos, labelList.size)
                        }
                    }
                }
            }
        }

        private lateinit var searchLabel: SearchLabel
        fun setSearchLabel(p: Int, s: SearchLabel) {
            pos = p
            searchLabel = s

            binding.run {
                editTextTextName.text = searchLabel.name.toEditable()
                editTextTextMultiLine.text = searchLabel.regex.toEditable()
                buttonApply.visibility = View.GONE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            SearchLabelVerticalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.setSearchLabel(position, labelList[position])

    override fun getItemCount(): Int = labelList.size
}