package dev.aurakai.auraframefx.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.aurakai.auraframefx.data.local.room.converters.Converters

// Define a basic entity for testing
@Entity(tableName = "example_entity")
data class ExampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Database(
    entities = [
        ExampleEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AuraFrameDatabase : RoomDatabase() {
    // Add your DAOs here
    abstract fun exampleDao(): ExampleDao

    companion object {
        @Volatile
        private var INSTANCE: AuraFrameDatabase? = null

        fun getDatabase(context: Context): AuraFrameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AuraFrameDatabase::class.java,
                    "aura_frame_database"
                )
                .fallbackToDestructiveMigration() // For development only
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
