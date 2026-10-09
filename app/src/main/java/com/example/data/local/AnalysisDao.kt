package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.FoodAnalysis
import kotlinx.coroutines.flow.Flow

/**
 * AnalysisDao
 * Data Access Object providing reactive Flow queries and CRUD operations
 * for food and beverage scan history.
 */
@Dao
interface AnalysisDao {

    @Query("SELECT * FROM food_scans ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<FoodAnalysis>>

    @Query("SELECT * FROM food_scans WHERE id = :id LIMIT 1")
    suspend fun getScanById(id: Long): FoodAnalysis?

    @Query("SELECT * FROM food_scans WHERE isBeverage = 1 ORDER BY timestamp DESC")
    fun getBeverageScans(): Flow<List<FoodAnalysis>>

    @Query("SELECT * FROM food_scans WHERE isBeverage = 0 ORDER BY timestamp DESC")
    fun getMealScans(): Flow<List<FoodAnalysis>>

    @Query("SELECT * FROM food_scans WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteScans(): Flow<List<FoodAnalysis>>

    @Query("SELECT * FROM food_scans WHERE detectedName LIKE '%' || :query || '%' OR detectedNameEn LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchScans(query: String): Flow<List<FoodAnalysis>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: FoodAnalysis): Long

    @Update
    suspend fun updateScan(scan: FoodAnalysis)

    @Delete
    suspend fun deleteScan(scan: FoodAnalysis)

    @Query("DELETE FROM food_scans WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM food_scans")
    suspend fun clearAll()
}
