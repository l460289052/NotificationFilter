package com.example.notificationfilter

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
            if (!notificationPermission)
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) // 这句话是请求权限的…… NotificationListenerService.requestRebind(
            ComponentName(
                applicationContext,
                NotificationCatcher::class.java
            )
        }
        binding.stopButton.setOnClickListener {
            sendBroadcast(Intent(NotificationCatcher.IntentStop))

        }
    }

    private val notificationPermission: Boolean
        get() {
            val pkgnames = NotificationManagerCompat.getEnabledListenerPackages(this)
            return pkgnames.contains(packageName)
        }
}