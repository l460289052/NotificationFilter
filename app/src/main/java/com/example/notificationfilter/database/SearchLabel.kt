package com.example.notificationfilter.database

import  androidx.room.*

@Entity(tableName = "search_label")
data class SearchLabel(
    var name: String,
    var regex: String,
    @ColumnInfo(index = true) var order_val: Int = 0
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}

@Dao
interface SearchLabelDao {
    @Query("SELECT * FROM search_label ORDER BY order_val")
    fun getAll(): List<SearchLabel>

    @Update
    fun update(vararg labels: SearchLabel)

    @Delete
    fun delete(vararg labels: SearchLabel)

    @Insert
    fun insert(vararg labels: SearchLabel): List<Long>
}