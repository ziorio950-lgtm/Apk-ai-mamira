package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.model.FoodAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class GeminiFoodRecognitionServiceImpl(
    private val context: Context
) : FoodRecognitionService {

    override suspend fun recognizeFood(
        bitmap: Bitmap
    ): Result<FoodAnalysis> = withContext(Dispatchers.IO) {

        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(createHeuristicAnalysis(bitmap))
        }

        try {
            val prompt = """
                أنت NutriAI، مساعد متخصص في تحليل الطعام والتغذية.
                حلل صورة الطعام وأعد كائن JSON فقط باللغة العربية.
                
                يجب أن يحتوي JSON على الحقول التالية:
                detectedName, detectedNameEn, category, confidence,
                portionEstimate, calories, proteinGrams, carbsGrams,
                fatGrams, sugarGrams, fiberGrams, sodiumMg,
                healthScore, healthScoreReason, explanation,
                ingredients, positiveAspects, cautions,
                recommendations, isBeverage, caffeineMg.
                
                اجعل القيم الغذائية تقديرية وواقعية.
                لا تدّعِ دقة لا تسمح بها الصورة.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt),
                            Part(
                                inlineData = InlineData(
                                    mimeType = "image/jpeg",
                                    data = bitmapToBase64(bitmap)
                                )
                            )
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.2f
                )
            )

            val response = RetrofitGeminiClient.service
                .generateContent(apiKey, request)

            val text = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text

            if (text.isNullOrBlank()) {
                return@withContext Result.success(
                    createHeuristicAnalysis(bitmap)
                )
            }

            val cleanJson = text.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(cleanJson)

            Result.success(
                FoodAnalysis(
                    detectedName = json.optString(
                        "detectedName", "وجبة غذائية"
                    ),
                    detectedNameEn = json.optString(
                        "detectedNameEn", "Food"
                    ),
                    category = json.optString("category", "meal"),
                    confidence = json.optInt("confidence", 70),
                    portionEstimate = json.optString(
                        "portionEstimate", "حصة متوسطة"
                    ),
                    calories = json.optInt("calories", 300),
                    proteinGrams = json.optDouble(
                        "proteinGrams", 15.0
                    ).toFloat(),
                    carbsGrams = json.optDouble(
                        "carbsGrams", 30.0
                    ).toFloat(),
                    fatGrams = json.optDouble(
                        "fatGrams", 10.0
                    ).toFloat(),
                    sugarGrams = json.optDouble(
                        "sugarGrams", 5.0
                    ).toFloat(),
                    fiberGrams = json.optDouble(
                        "fiberGrams", 4.0
                    ).toFloat(),
                    sodiumMg = json.optDouble(
                        "sodiumMg", 300.0
                    ).toFloat(),
                    healthScore = json.optInt("healthScore", 70),
                    healthScoreReason = json.optString(
                        "healthScoreReason", "تقييم تقديري"
                    ),
                    explanation = json.optString(
                        "explanation", "تحليل غذائي تقديري."
                    ),
                    ingredients = json.optString(
                        "ingredients", "مكونات غير مؤكدة"
                    ),
                    positiveAspects = json.optString(
                        "positiveAspects", "قد تحتوي على عناصر غذائية مفيدة"
                    ),
                    cautions = json.optString(
                        "cautions", "تحقق من المكونات والحصة"
                    ),
                    recommendations = json.optString(
                        "recommendations", "تناول غذاء متوازن"
                    ),
                    isBeverage = json.optBoolean("isBeverage", false),
                    caffeineMg = json.optDouble(
                        "caffeineMg", 0.0
                    ).toFloat()
                )
            )
        } catch (e: Exception) {
            Result.success(createHeuristicAnalysis(bitmap))
        }
    }

    override suspend fun consultNutritionAdvisor(
        prompt: String,
        contextScan: FoodAnalysis?
    ): Result<String> = withContext(Dispatchers.IO) {

        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(
                getLocalAssistantReply(prompt, contextScan)
            )
        }

        try {
            val scanContext = contextScan?.let {
                """
                الوجبة: ${it.detectedName}
                السعرات: ${it.calories}
                البروتين: ${it.proteinGrams} غرام
                الكربوهيدرات: ${it.carbsGrams} غرام
                الدهون: ${it.fatGrams} غرام
                التقييم الصحي: ${it.healthScore}/100
                """.trimIndent()
            } ?: "لا توجد وجبة محددة."

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(
                                text = """
                                    أنت AURA، مساعد NutriAI الغذائي.
                                    أجب باللغة العربية بأسلوب واضح وودود.
                                    قدم معلومات تقديرية ولا تستبدل الطبيب.
                                    
                                    سياق الوجبة:
                                    $scanContext
                                    
                                    سؤال المستخدم:
                                    $prompt
                                """.trimIndent()
                            )
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.4f
                )
            )

            val response = RetrofitGeminiClient.service
                .generateContent(apiKey, request)

            val answer = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text

            Result.success(
                answer?.takeIf { it.isNotBlank() }?.trim()
                    ?: getLocalAssistantReply(prompt, contextScan)
            )
        } catch (e: Exception) {
            Result.success(getLocalAssistantReply(prompt, contextScan))
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
        return Base64.encodeToString(
            output.toByteArray(),
            Base64.NO_WRAP
        )
    }

    private fun createHeuristicAnalysis(
        bitmap: Bitmap
    ): FoodAnalysis {
        val x = bitmap.width / 2
        val y = bitmap.height / 2
        val pixel = bitmap.getPixel(x, y)

        val red = (pixel shr 16) and 0xff
        val green = (pixel shr 8) and 0xff
        val blue = pixel and 0xff

        return when {
            green > red && green > blue -> FoodAnalysis(
                detectedName = "سلطة خضروات",
                detectedNameEn = "Vegetable Salad",
                category = "meal",
                confidence = 60,
                portionEstimate = "حصة متوسطة",
                calories = 180,
                proteinGrams = 5f,
                carbsGrams = 15f,
                fatGrams = 10f,
                sugarGrams = 5f,
                fiberGrams = 5f,
                sodiumMg = 200f,
                healthScore = 80,
                healthScoreReason = "قد تحتوي على خضروات مفيدة.",
                explanation = "هذا تقدير محلي بسيط وليس تعرفاً مؤكداً على الطعام.",
                ingredients = "خضروات محتملة",
                positiveAspects = "قد توفر الألياف والفيتامينات.",
                cautions = "المكونات الفعلية غير مؤكدة.",
                recommendations = "تحقق من المكونات وحجم الحصة.",
                isBeverage = false
            )

            (redpackage com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.model.FoodAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class GeminiFoodRecognitionServiceImpl(
    private val context: Context
) : FoodRecognitionService {

    override suspend fun recognizeFood(
        bitmap: Bitmap
    ): Result<FoodAnalysis> = withContext(Dispatchers.IO) {

        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(createHeuristicAnalysis(bitmap))
        }

        try {
            val prompt = """
                أنت NutriAI، مساعد متخصص في تحليل الطعام والتغذية.
                حلل صورة الطعام وأعد كائن JSON فقط باللغة العربية.
                
                يجب أن يحتوي JSON على الحقول التالية:
                detectedName, detectedNameEn, category, confidence,
                portionEstimate, calories, proteinGrams, carbsGrams,
                fatGrams, sugarGrams, fiberGrams, sodiumMg,
                healthScore, healthScoreReason, explanation,
                ingredients, positiveAspects, cautions,
                recommendations, isBeverage, caffeineMg.
                
                اجعل القيم الغذائية تقديرية وواقعية.
                لا تدّعِ دقة لا تسمح بها الصورة.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt),
                            Part(
                                inlineData = InlineData(
                                    mimeType = "image/jpeg",
                                    data = bitmapToBase64(bitmap)
                                )
                            )
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.2f
                )
            )

            val response = RetrofitGeminiClient.service
                .generateContent(apiKey, request)

            val text = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text

            if (text.isNullOrBlank()) {
                return@withContext Result.success(
                    createHeuristicAnalysis(bitmap)
                )
            }

            val cleanJson = text.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(cleanJson)

            Result.success(
                FoodAnalysis(
                    detectedName = json.optString(
                        "detectedName", "وجبة غذائية"
                    ),
                    detectedNameEn = json.optString(
                        "detectedNameEn", "Food"
                    ),
                    category = json.optString("category", "meal"),
                    confidence = json.optInt("confidence", 70),
                    portionEstimate = json.optString(
                        "portionEstimate", "حصة متوسطة"
                    ),
                    calories = json.optInt("calories", 300),
                    proteinGrams = json.optDouble(
                        "proteinGrams", 15.0
                    ).toFloat(),
                    carbsGrams = json.optDouble(
                        "carbsGrams", 30.0
                    ).toFloat(),
                    fatGrams = json.optDouble(
                        "fatGrams", 10.0
                    ).toFloat(),
                    sugarGrams = json.optDouble(
                        "sugarGrams", 5.0
                    ).toFloat(),
                    fiberGrams = json.optDouble(
                        "fiberGrams", 4.0
                    ).toFloat(),
                    sodiumMg = json.optDouble(
                        "sodiumMg", 300.0
                    ).toFloat(),
                    healthScore = json.optInt("healthScore", 70),
                    healthScoreReason = json.optString(
                        "healthScoreReason", "تقييم تقديري"
                    ),
                    explanation = json.optString(
                        "explanation", "تحليل غذائي تقديري."
                    ),
                    ingredients = json.optString(
                        "ingredients", "مكونات غير مؤكدة"
                    ),
                    positiveAspects = json.optString(
                        "positiveAspects", "قد تحتوي على عناصر غذائية مفيدة"
                    ),
                    cautions = json.optString(
                        "cautions", "تحقق من المكونات والحصة"
                    ),
                    recommendations = json.optString(
                        "recommendations", "تناول غذاء متوازن"
                    ),
                    isBeverage = json.optBoolean("isBeverage", false),
                    caffeineMg = json.optDouble(
                        "caffeineMg", 0.0
                    ).toFloat()
                )
            )
        } catch (e: Exception) {
            Result.success(createHeuristicAnalysis(bitmap))
        }
    }

    override suspend fun consultNutritionAdvisor(
        prompt: String,
        contextScan: FoodAnalysis?
    ): Result<String> = withContext(Dispatchers.IO) {

        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(
                getLocalAssistantReply(prompt, contextScan)
            )
        }

        try {
            val scanContext = contextScan?.let {
                """
                الوجبة: ${it.detectedName}
                السعرات: ${it.calories}
                البروتين: ${it.proteinGrams} غرام
                الكربوهيدرات: ${it.carbsGrams} غرام
                الدهون: ${it.fatGrams} غرام
                التقييم الصحي: ${it.healthScore}/100
                """.trimIndent()
            } ?: "لا توجد وجبة محددة."

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(
                                text = """
                                    أنت AURA، مساعد NutriAI الغذائي.
                                    أجب باللغة العربية بأسلوب واضح وودود.
                                    قدم معلومات تقديرية ولا تستبدل الطبيب.
                                    
                                    سياق الوجبة:
                                    $scanContext
                                    
                                    سؤال المستخدم:
                                    $prompt
                                """.trimIndent()
                            )
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.4f
                )
            )

            val response = RetrofitGeminiClient.service
                .generateContent(apiKey, request)

            val answer = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text

            Result.success(
                answer?.takeIf { it.isNotBlank() }?.trim()
                    ?: getLocalAssistantReply(prompt, contextScan)
            )
        } catch (e: Exception) {
            Result.success(getLocalAssistantReply(prompt, contextScan))
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
        return Base64.encodeToString(
            output.toByteArray(),
            Base64.NO_WRAP
        )
    }

    private fun createHeuristicAnalysis(
        bitmap: Bitmap
    ): FoodAnalysis {
        val x = bitmap.width / 2
        val y = bitmap.height / 2
        val pixel = bitmap.getPixel(x, y)

        val red = (pixel shr 16) and 0xff
        val green = (pixel shr 8) and 0xff
        val blue = pixel and 0xff

        return when {
            green > red && green > blue -> FoodAnalysis(
                detectedName = "سلطة خضروات",
                detectedNameEn = "Vegetable Salad",
                category = "meal",
                confidence = 60,
                portionEstimate = "حصة متوسطة",
                calories = 180,
                proteinGrams = 5f,
                carbsGrams = 15f,
                fatGrams = 10f,
                sugarGrams = 5f,
                fiberGrams = 5f,
                sodiumMg = 200f,
                healthScore = 80,
                healthScoreReason = "قد تحتوي على خضروات مفيدة.",
                explanation = "هذا تقدير محلي بسيط وليس تعرفاً مؤكداً على الطعام.",
                ingredients = "خضروات محتملة",
                positiveAspects = "قد توفر الألياف والفيتامينات.",
                cautions = "المكونات الفعلية غير مؤكدة.",
                recommendations = "تحقق من المكونات وحجم الحصة.",
                isBeverage = false
            )

            (red
