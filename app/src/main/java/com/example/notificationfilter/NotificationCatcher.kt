package com.example.notificationfilter

import android.app.Notification
import android.content.*
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime

open class NotificationCatcher : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob())
    private val db: NotificationDatabase by lazy { NotificationDatabase.getDatabase(this) }


    private lateinit var stopReceiver: BroadcastReceiver
    override fun onCreate() {
        Log.v(NOTI_SERVER, "on created")
        super.onCreate()
        stopReceiver = FuncBroadcastReceiver {
            RUNNING = false
            requestUnbind()
            Log.v(NOTI_SERVER, "stop")
        }
        IntentFilter().apply {
            addAction(IntentStop)
            registerReceiver(stopReceiver, this)
        }
//        requestUnbind()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.v(NOTI_SERVER, "on connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {

            val notification = it.notification
            val extras = notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE)
            val text = extras.getString(Notification.EXTRA_TEXT)


            scope.launch {
                val dao = db.notificationDao()
                dao.insertAll(
                    Notification(
                        LocalDateTime.now(),
                        it.packageName,
                        notification.channelId,
                        title ?: "",
                        text ?: "",
                        "" // TODO: 需要之后实现intent的持久化与跳转！
                    )
                )
                Log.v(NOTI_SERVER, "insert")
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
