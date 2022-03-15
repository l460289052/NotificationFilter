package com.example.notificationfilter

import android.content.ComponentName
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.text.Editable
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

val BaseTime: LocalTime = LocalTime.of(0, 0)
fun LocalDate.toLocalDateTime(): LocalDateTime = LocalDateTime.of(this, BaseTime)

fun joinToRegex(vararg args: String): String = args.joinToString("\n")
fun joinToSearch(vararg args: String): String = args.joinToString("\n").lowercase()
fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

fun AppCompatActivity.rebindListenerService() {
    sendBroadcast(Intent(NotificationCatcher.IntentStop))
    NotificationListenerService.requestRebind(
        ComponentName(
            applicationContext,
            NotificationCatcher::class.java
        )
    )
}