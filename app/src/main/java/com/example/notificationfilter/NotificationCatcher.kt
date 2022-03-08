package com.example.notificationfilter

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class NotificationCatcher : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob())
    private val db: NotificationDatabase by lazy { NotificationDatabase.getDatabase(this) }

//    override fun onListenerConnected() {
//        super.onListenerConnected()
//    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {

            val extras = it.notification.extras
            val pkg = it.packageName
            val title = extras.getString(Notification.EXTRA_TITLE)
            val text = extras.getString(Notification.EXTRA_TEXT)
            Log.v("NOTI-TEST", "$pkg $title $text")

            scope.launch {
                val dao = db.notificationDao()
                dao.insertAll(
                    com.example.notificationfilter.Notification(
                        LocalDateTime.now(),
                        pkg,
                        title ?: "",
                        text ?: ""
                    )
                )
                val num = dao.getAll().size
                Log.v("NOTI-TEST", "size: $num")
            }
        }
    }

}