package com.example.notificationfilter.database

import androidx.room.*

@Entity(tableName = "notification_filter")
data class NotificationFilter(
    var regex: String
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
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
    fun insert(vararg filters: NotificationFilter)
}
