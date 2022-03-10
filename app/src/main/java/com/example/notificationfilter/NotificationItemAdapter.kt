package com.example.notificationfilter

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationfilter.database.NotificationDatabase
import com.example.notificationfilter.databinding.NotificationItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class NotificationItem(
    val pkg: String,
    val app: String,
    val channel: String,
    val title: String,
    val content: String,
    val intent: String,
    val time: LocalDateTime,
    val icon: Drawable? = null,
)

class NotificationItemAdapter(private var notificationItemList: List<NotificationItem>) :
    RecyclerView.Adapter<NotificationItemAdapter.ViewHolder>() {
    var currentPosition: Int = 0

    inner class ViewHolder(val binding: NotificationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var updatePosition: (() -> Unit)? = null

        init {
            itemView.isLongClickable = true
            itemView.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN)
                    updatePosition?.invoke()
                view.onTouchEvent(event)
                true
            }
        }
    }

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
                appnameView.text = app
                textViewDatetime.text =
                    time.format(DateTimeFormatter.ofPattern("HH:mm:ss yyyy-MM-dd"))
                titleView.text = title
                contentView.text = "$channel: $content"
                iconView.setImageDrawable(icon)
            }
        }
        holder.updatePosition = {
            currentPosition = position
        }
    }

    override fun getItemCount() = notificationItemList.count()

}