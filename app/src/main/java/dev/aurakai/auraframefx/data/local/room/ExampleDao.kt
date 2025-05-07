package dev.aurakai.auraframefx.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExampleDao {
    @Insert
    suspend fun insert(example: ExampleEntity)

    @Query("SELECT * FROM example_entity")
    fun getAll(): List<ExampleEntity>
}
