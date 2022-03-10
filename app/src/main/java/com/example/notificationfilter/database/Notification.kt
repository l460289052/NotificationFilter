package com.example.notificationfilter.database

import androidx.room.*
import java.time.LocalDateTime

@Entity(indices = [Index(value = ["app", "channel"])])
data class Notification(
    @ColumnInfo(index = true) val time: LocalDateTime,
    val app: String,
    val channel: String,
    val title: String,
    val content: String,
    val intent: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    fun toBeRegex(): String = listOf(app, channel, title, content).joinToString("\n")
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification")
    fun getAll(): List<Notification>

    @Query("SELECT * FROM notification WHERE time > :left AND time < :right ORDER BY time DESC")
    fun getBetween(left: LocalDateTime, right: LocalDateTime): List<Notification>

    @Query("SELECT * FROM notification WHERE time > :left ORDER BY time DESC")
    fun getAfter(left: LocalDateTime): List<Notification>

    @Insert
    fun insertAll(vararg notification: Notification)

    @Delete
    fun delete(vararg notification: Notification)
}
