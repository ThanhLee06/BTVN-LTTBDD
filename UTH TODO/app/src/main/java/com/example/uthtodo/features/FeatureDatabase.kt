package com.example.uthtodo.features

import androidx.room.*

@Entity(tableName = "task_extras", primaryKeys = ["owner", "taskId"])
data class FeatureRecord(val owner: String, val taskId: String, val payload: String)

@Dao
interface FeatureDao {
    @Query("SELECT * FROM task_extras WHERE owner = :owner")
    suspend fun all(owner: String): List<FeatureRecord>
    @Query("SELECT * FROM task_extras")
    suspend fun allAccounts(): List<FeatureRecord>
    @Query("SELECT * FROM task_extras WHERE owner = :owner AND taskId = :id")
    suspend fun find(owner: String, id: String): FeatureRecord?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(record: FeatureRecord)
    @Query("DELETE FROM task_extras WHERE owner = :owner")
    suspend fun clear(owner: String)
}

@Database(entities = [FeatureRecord::class], version = 1, exportSchema = true)
abstract class FeatureDatabase : RoomDatabase() { abstract fun records(): FeatureDao }
