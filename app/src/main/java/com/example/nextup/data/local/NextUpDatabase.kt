package com.example.nextup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nextup.data.local.converter.Converters
import com.example.nextup.data.local.dao.EventDao
import com.example.nextup.data.local.entity.EventEntity

@Database(
    entities = [EventEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NextUpDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        const val DATABASE_NAME = "nextup_events.db"
    }
}
