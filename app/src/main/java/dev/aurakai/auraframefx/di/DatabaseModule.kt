package dev.aurakai.auraframefx.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.aurakai.auraframefx.data.local.room.AuraFrameDatabase
import dev.aurakai.auraframefx.data.local.room.ExampleDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AuraFrameDatabase {
        return AuraFrameDatabase.getDatabase(context)
    }

    @Provides
    fun provideExampleDao(database: AuraFrameDatabase): ExampleDao {
        return database.exampleDao()
    }
}
