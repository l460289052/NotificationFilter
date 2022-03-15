package com.example.notificationfilter

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.intent.IntentStubberRegistry
import com.example.notificationfilter.database.MIGRATION_1_2
import  org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        com.example.notificationfilter.database.NotificationDatabase::class.java
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        helper.createDatabase(TEST_DB, 1).apply {
            for (i in 0 until 10)
                execSQL("INSERT INTO notification_filter(regex) VALUES(\"some regex\")")
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
            .run {
                query("SELECT * FROM notification_filter").run {
                    var i = 0
                    while (moveToNext()) {
                        i++
                        assertEquals(true, 0 != getInt(getColumnIndexOrThrow("enabled")))
                    }
                    assertEquals(10, i)
                }
                close()
            }


    }
}