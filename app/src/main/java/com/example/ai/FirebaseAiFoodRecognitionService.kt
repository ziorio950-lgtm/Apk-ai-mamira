package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import com.example.model.FoodAnalysis
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerationConfig
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.TextPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * FirebaseAiFoodRecognitionService
 * Implementation of FoodRecognitionService leveraging Firebase AI (Gemini Flash)
 * with structured JSON schema prompting and robust fallback handling.
 */
class FirebaseAiFoodRecognitionService(
    private val context: Context,
    private val fallbackProvider: GeminiFoodAnalysisProvider = GeminiFoodAnalysisProvider(context)
) : FoodRecognitionService {

    private val structuredPrompt = """
        You are NutriAI, an advanced futuristic AI food and nutrition analyzer on a spacecraft.
        Analyze the food or beverage in this image in Arabic. Return ONLY a valid JSON object with EXACTLY this structure:
        {
          "detectedName": "اسم الوجبة أو المشروب بالعربية",
          "detectedNameEn": "Name in English",
          "category": "meal" or "beverage" or "snack",
          "confidence": integer 50-99,
          "portionEstimate": "تقدير الحصة بالجرام أو الحجم",
          "calories": integer,
          "proteinGrams": float,
          "carbsGrams": float,
          "fatGrams": float,
          "sugarGrams": float,
          "fiberGrams": float,
          "sodiumMg": float,
          "healthScore": integer 0-100,
          "healthScoreReason": "سبب التقييم الصحي باللغة العربية",
          "explanation": "شرح علمي غذائي تفصيلي للوجبة باللغة العربية",
          "ingredients": "المكونات المفصولة بفواصل بالعربية",
          "positiveAspects": "أبرز الجوانب الإيجابية والمغذيات",
          "cautions": "نقاط الانتباه مثل السكريات أو الدهون المشبعة أو الصوديوم",
          "recommendations": "توصيات ذكية لتحسين القيمة الغذائية أو بدائل",
          "isBeverage": boolean,
          "caffeineMg": float
        }
        Do not wrap in markdown quotes if possible, return plain JSON.
    """.trimIndent()

    override suspend fun recognizeFood(bitmap: Bitmap): Result<FoodAnalysis> = withContext(Dispatchers.IO) {
        try {
            val genConfig = GenerationConfig.Builder()
                .setTemperature(0.2f)
                .setResponseMimeType("application/json")
                .build()

            val generativeModel: GenerativeModel = Firebase.ai.generativeModel(
                "gemini-2.5-flash",
                genConfig
            )

            val inputContent = Content(
                "user",
                listOf(ImagePart(bitmap), TextPart(structuredPrompt))
            )

            val response = generativeModel.generateContent(inputContent)
            val jsonText = response.text

            if (jsonText != null && jsonText.isNotBlank()) {
                val cleanJson = jsonText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()
                val json = JSONObject(cleanJson)
                val analysis = parseJsonToFoodAnalysis(json)
                Result.success(analysis)
            } else {
                fallbackProvider.analyzeFood(bitmap)
            }
        } catch (e: Exception) {
            // Seamlessly fall back to REST or local heuristic analysis
            fallbackProvider.analyzeFood(bitmap)
        }
    }

    override suspend fun consultNutritionAdvisor(prompt: String, contextScan: FoodAnalysis?): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contextNote = if (contextScan != null) {
                "سياق الوجبة الحالية: ${contextScan.detectedName}، السعرات: ${contextScan.calories}، البروتين: ${contextScan.proteinGrams}g، التقييم: ${contextScan.healthScore}/100."
            } else {
                "لا توجد وجبة ممسوحة حالياً."
            }

            val systemPrompt = """
                أنت AURA المساعد التغذوي الذكي في نظام NutriAI. أجب باللغة العربية بأسلوب علمي واضح وموجز ومفيد.
                $contextNote
                سؤال المستخدم: $prompt
            """.trimIndent()

            val genConfig = GenerationConfig.Builder()
                .setTemperature(0.6f)
                .build()

            val generativeModel: GenerativeModel = Firebase.ai.generativeModel(
                "gemini-2.5-flash",
                genConfig
            )

            val response = generativeModel.generateContent(systemPrompt)
            val reply = response.text
            if (reply != null && reply.isNotBlank()) {
                Result.success(reply.trim())
            } else {
                fallbackProvider.askAssistant(prompt, contextScan)
            }
        } catch (e: Exception) {
            fallbackProvider.askAssistant(prompt, contextScan)
        }
    }

    private fun parseJsonToFoodAnalysis(json: JSONObject): FoodAnalysis {
        return FoodAnalysis(
            detectedName = json.optString("detectedName", "وجبة مجهولة"),
            detectedNameEn = json.optString("detectedNameEn", "Unknown Dish"),
            category = json.optString("category", "meal"),
            confidence = json.optInt("confidence", 85),
            portionEstimate = json.optString("portionEstimate", "حصة متوسطة"),
            calories = json.optInt("calories", 350),
            proteinGrams = json.optDouble("proteinGrams", 20.0).toFloat(),
            carbsGrams = json.optDouble("carbsGrams", 30.0).toFloat(),
            fatGrams = json.optDouble("fatGrams", 10.0).toFloat(),
            sugarGrams = json.optDouble("sugarGrams", 5.0).toFloat(),
            fiberGrams = json.optDouble("fiberGrams", 4.0).toFloat(),
            sodiumMg = json.optDouble("sodiumMg", 300.0).toFloat(),
            healthScore = json.optInt("healthScore", 80),
            healthScoreReason = json.optString("healthScoreReason", "تقييم غذائي متوازن"),
            explanation = json.optString("explanation", "تحليل القيمة الغذائية للمكونات المكتشفة."),
            ingredients = json.optString("ingredients", "مكونات متنوعة"),
            positiveAspects = json.optString("positiveAspects", "مصدر جيد للطاقة"),
            cautions = json.optString("cautions", "تأكد من توازن الحصة"),
            recommendations = json.optString("recommendations", "تناول كميات كافية من الماء"),
            isBeverage = json.optBoolean("isBeverage", false),
            caffeineMg = json.optDouble("caffeineMg", 0.0).toFloat()
        )
    }
}
