package com.example.notificationfilter

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.text.BoringLayout
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notificationfilter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewAdapter: NotificationItemAdapter

    private var notificationItemList: MutableList<NotificationItem> =
        mutableListOf(NotificationItem("123"), NotificationItem("456"))

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        viewAdapter = NotificationItemAdapter(notificationItemList)
        binding.logView.let {
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = viewAdapter
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel("normal", "Normal", NotificationManager.IMPORTANCE_DEFAULT)
        )


        binding.notiButton.setOnClickListener {
            manager.notify(
                1,
                NotificationCompat.Builder(this, "normal")
                    .apply {
                        setContentTitle("This is content title")
                        setContentText("This is text")
                        setSmallIcon(R.drawable.archives)
                        setAutoCancel(true)
                    }.build()
            )
        }

        binding.startButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
//        binding.startButton.setOnClickListener { notificationActive = true }
//        binding.stopButton.setOnClickListener { notificationActive = false }
    }

//    private val notificationServiceEnabled: Boolean
//        get() {
//            val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
//            flat?.let {
//                for (name in it.split(":"))
//                    if (ComponentName.unflattenFromString(name)?.packageName == packageName)
//                        return true
//            }
//            return false
//        }

//    private var notificationCatcherActive: Boolean
//        set(value) {
//            if (value)
//
//                NotificationListenerService.requestRebind()
//            else
//                NotificationListenerService.
//
//        }
//
//    private var notificationActive: Boolean
//        set(value) {
//            if (value && !notificationActive)
//                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
//            if (!value && notificationActive)
//                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
//        }
}