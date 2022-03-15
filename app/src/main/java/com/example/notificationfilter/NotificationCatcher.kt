package com.example.notificationfilter

import android.app.Notification
import android.content.*
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import com.example.notificationfilter.database.NotificationDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime

open class NotificationCatcher : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob())
    private val db: NotificationDatabase by lazy { NotificationDatabase.getDatabase(this) }
    private var regex: Regex? = null


    private lateinit var stopReceiver: BroadcastReceiver
    override fun onCreate() {
        Log.v(NOTI_SERVER, "on created")
        super.onCreate()
        stopReceiver = FuncBroadcastReceiver {
            RUNNING = false
            requestUnbind()
            Log.v(NOTI_SERVER, "stop")
            Toast.makeText(applicationContext, "server stop", Toast.LENGTH_SHORT).show()
        }
        IntentFilter().apply {
            addAction(IntentStop)
            registerReceiver(stopReceiver, this)
        }
        requestUnbind()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.v(NOTI_SERVER, "on connected")
        Toast.makeText(applicationContext, "server start", Toast.LENGTH_SHORT).show()

        scope.launch {
            val filters = db.filterDao().getAll()
            if (filters.isNotEmpty())
                regex = filters.joinToString("|") { "(${it.regex})" }.toRegex()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.run {
            val notification = notification
            val extras = notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
            val text = extras.getString(Notification.EXTRA_TEXT) ?: ""

            scope.launch {
                com.example.notificationfilter.database.Notification(
                    LocalDateTime.now(),
                    packageName,
                    notification.channelId,
                    title,
                    text,
                    "" // TODO: 需要之后实现intent的持久化与跳转！
                ).run {
                    Log.v(NOTI_SERVER, "receive")
                    if (regex?.find(toBeRegex())?.value == null) {
                        db.notificationDao().insertAll(this)
                        Log.v(NOTI_SERVER, "insert")
                    }
                }
            }
        }
    }

    private class FuncBroadcastReceiver(val func: () -> Unit) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = func()
    }

    companion object {
        val NOTI_SERVER = "NOTI-SERVER"
        var RUNNING = false
        var IntentStop = "com.example.notificationfilter.STOP"
    }
}
