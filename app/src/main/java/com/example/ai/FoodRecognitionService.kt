package com.example.ai

import android.graphics.Bitmap
import com.example.model.FoodAnalysis

interface FoodRecognitionService {

    suspend fun recognizeFood(
        bitmap: Bitmap
    ): Result<FoodAnalysis>

    suspend fun consultNutritionAdvisor(
        prompt: String,
        contextScan: FoodAnalysis? = null
    ): Result<String>
}
