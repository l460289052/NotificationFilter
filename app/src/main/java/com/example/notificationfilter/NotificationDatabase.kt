package com.example.notificationfilter

import android.content.Context
import androidx.room.*
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): LocalDateTime {
        return ChronoUnit.SECONDS.addTo(BASE, value)
    }

    @TypeConverter()
    fun toTimestamp(date: LocalDateTime): Long {
        return ChronoUnit.SECONDS.between(BASE, date)
    }

    companion object {
        val BASE: LocalDateTime = LocalDateTime.of(1970, 1, 1, 0, 0, 0)
    }
}

@Entity
data class Notification(
    @ColumnInfo(index = true) val time: LocalDateTime,
    @ColumnInfo(index = true) val app: String,
    val title: String,
    val content: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification")
    fun getAll(): List<Notification>

    @Query("SELECT * FROM notification WHERE time > :left AND time < :right")
    fun getBetween(left: LocalDateTime, right: LocalDateTime): List<Notification>

    @Query("SELECT * FROM notification WHERE time > :left")
    fun getAfter(left: LocalDateTime): List<Notification>

    @Insert
    fun insertAll(vararg notifications: Notification)
}

@Database(entities = [Notification::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NotificationDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: NotificationDatabase? = null
        fun getDatabase(context: Context): NotificationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotificationDatabase::class.java,
                    "notification_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
