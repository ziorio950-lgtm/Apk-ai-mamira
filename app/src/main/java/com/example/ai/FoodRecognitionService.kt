package com.example.ai

import android.graphics.Bitmap
import com.example.model.FoodAnalysis

/**
 * FoodRecognitionService
 * Core domain service interface for AI-driven food and beverage multimodal recognition,
 * nutritional analysis, and macro estimations.
 */
interface FoodRecognitionService {
    /**
     * Analyzes a food or beverage image using multimodal generative AI and returns
     * structured nutritional insights.
     */
    suspend fun recognizeFood(bitmap: Bitmap): Result<FoodAnalysis>

    /**
     * Answers nutritional questions in the context of a previous food scan or general dietetics.
     */
    suspend fun consultNutritionAdvisor(prompt: String, contextScan: FoodAnalysis? = null): Result<String>
}
