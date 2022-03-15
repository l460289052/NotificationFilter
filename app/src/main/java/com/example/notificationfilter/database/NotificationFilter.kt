package com.example.notificationfilter.database

import androidx.room.*

@Entity(tableName = "notification_filter")
data class NotificationFilter(
    var regex: String,
    var enabled: Boolean = true
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    constructor(pkg: String, channel: String) : this(getRegex(pkg, channel))

    companion object {
        fun getRegex(pkg: String, channel: String): String =
            "^${Regex.escape(pkg)}\n${Regex.escape(channel)}\n"
    }
}

@Dao
interface NotificationFilterDao {
    @Query("SELECT * FROM notification_filter")
    fun getAll(): List<NotificationFilter>

    @Update
    fun update(vararg filters: NotificationFilter)

    @Delete
    fun delete(vararg filters: NotificationFilter)

    @Insert
    fun insert(vararg filters: NotificationFilter): List<Long>
}
