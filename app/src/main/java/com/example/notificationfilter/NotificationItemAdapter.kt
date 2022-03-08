package com.example.notificationfilter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationfilter.databinding.NotificationItemBinding

class NotificationItem(val text: String)

class NotificationItemAdapter(var notificationItemList: MutableList<NotificationItem>) :
    RecyclerView.Adapter<NotificationItemAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: NotificationItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            NotificationItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.run {
            notificationItemList[position].run {
                textView.text = this.text
            }
        }
    }

    override fun getItemCount() = notificationItemList.count()
}