package com.example.nextup.di

import android.content.Context
import androidx.room.Room
import com.example.nextup.data.local.NextUpDatabase
import com.example.nextup.data.local.dao.EventDao
import com.example.nextup.data.repository.EventRepositoryImpl
import com.example.nextup.domain.repository.EventRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEventRepository(
        eventRepositoryImpl: EventRepositoryImpl
    ): EventRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNextUpDatabase(
        @ApplicationContext context: Context
    ): NextUpDatabase {
        return Room.databaseBuilder(
            context,
            NextUpDatabase::class.java,
            NextUpDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideEventDao(database: NextUpDatabase): EventDao {
        return database.eventDao()
    }
}
