package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "food_scans")
@JsonClass(generateAdapter = true)
data class FoodAnalysis(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val detectedName: String = "",
    val detectedNameEn: String = "",
    val category: String = "meal", // "meal", "beverage", "snack"
    val confidence: Int = 85, // percentage 0-100
    val portionEstimate: String = "حصة متوسطة (250 غرام)",
    val calories: Int = 350,
    val proteinGrams: Float = 25f,
    val carbsGrams: Float = 30f,
    val fatGrams: Float = 12f,
    val sugarGrams: Float = 4f,
    val fiberGrams: Float = 5f,
    val sodiumMg: Float = 380f,
    val healthScore: Int = 82, // 0-100
    val healthScoreReason: String = "توازن ممتاز بين البروتين والألياف مع انخفاض السكريات المضافة والصوديوم.",
    val explanation: String = "وجبة مغذية غنية بالعناصر الأساسية، تدعم الطاقة والشبع لفترة طويلة.",
    val ingredients: String = "دجاج مشوي، خس، طماطم كرزية، خيار، أفوكادو، بذور القرع",
    val positiveAspects: String = "غني بالبروتين، غني بالألياف ومضادات الأكسدة",
    val cautions: String = "احذر من كمية الصلصة الزيتية المضافة إذا كنت تتبع حمية منخفضة السعرات",
    val recommendations: String = "أضف عصير ليمون طازج بدلاً من الصلصات التجارية لتقليل الدهون المشبعة",
    val isBeverage: Boolean = false,
    val caffeineMg: Float = 0f,
    val isFavorite: Boolean = false,
    val localImagePath: String? = null
)

@JsonClass(generateAdapter = true)
data class HealthyAlternative(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val category: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val sugar: Float,
    val reasonAr: String,
    val benefitAr: String
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
