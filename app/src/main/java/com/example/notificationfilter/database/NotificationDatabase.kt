package com.example.notificationfilter.database

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


@Database(
    entities = [
        Notification::class,
        NotificationFilter::class,
        SearchLabel::class
    ],
    autoMigrations = [
        AutoMigration(from = 2, to = 3)
    ],
    version = 3, exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NotificationDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun filterDao(): NotificationFilterDao
    abstract fun labelDao(): SearchLabelDao

    companion object {
        @Volatile
        private var INSTANCE: NotificationDatabase? = null
        fun getDatabase(context: Context): NotificationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotificationDatabase::class.java,
                    "notification_database.db"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
