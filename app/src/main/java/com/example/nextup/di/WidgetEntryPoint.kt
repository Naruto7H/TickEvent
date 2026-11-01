package com.example.nextup.di

import android.content.Context
import com.example.nextup.data.local.dao.EventDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun eventDao(): EventDao

    companion object {
        fun getEventDao(context: Context): EventDao {
            val appContext = context.applicationContext
            return EntryPointAccessors.fromApplication(
                appContext,
                WidgetEntryPoint::class.java
            ).eventDao()
        }
    }
}
