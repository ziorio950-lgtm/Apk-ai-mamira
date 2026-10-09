package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.ai.GeminiFoodAnalysisProvider
import com.example.data.local.AppDatabase
import com.example.model.ChatMessage
import com.example.model.FoodAnalysis
import com.example.model.HealthyAlternative
import com.example.voice.FuturisticVoiceAssistant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Scanning : ScanUiState
    data class Success(val analysis: FoodAnalysis) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.analysisDao()
    private val foodRecognitionService: FoodRecognitionService = FirebaseAiFoodRecognitionService(application)
    val voiceAssistant = FuturisticVoiceAssistant(application)

    // Current Screen Navigation: 0=Camera, 1=History, 2=Assistant, 3=Nutrition Stats, 4=Alternatives, 5=Settings
    private val _currentNavIndex = MutableStateFlow(0)
    val currentNavIndex: StateFlow<Int> = _currentNavIndex.asStateFlow()

    // Scan State
    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    // Active or selected scan detail
    private val _selectedAnalysis = MutableStateFlow<FoodAnalysis?>(null)
    val selectedAnalysis: StateFlow<FoodAnalysis?> = _selectedAnalysis.asStateFlow()

    // Captured image bitmap in memory
    private val _currentCapturedBitmap = MutableStateFlow<Bitmap?>(null)
    val currentCapturedBitmap: StateFlow<Bitmap?> = _currentCapturedBitmap.asStateFlow()

    // History Flow from Room
    val scanHistory: StateFlow<List<FoodAnalysis>> = dao.getAllScans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat Messages for Assistant
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                isUser = false,
                text = "مرحباً بك في محطة NutriAI الذكية! أنا مساعدك التغذوي الاصطناعي. كيف يمكنني مساعدتك في تحليل طعامك، تقييم السعرات، أو تقديم بدائل صحية متوازنة؟"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // User Preferences
    private val _isVoiceAutoPlay = MutableStateFlow(true)
    val isVoiceAutoPlay: StateFlow<Boolean> = _isVoiceAutoPlay.asStateFlow()

    private val _isEffectsEnabled = MutableStateFlow(true)
    val isEffectsEnabled: StateFlow<Boolean> = _isEffectsEnabled.asStateFlow()

    // Healthy Alternatives Catalog
    private val _alternativesList = MutableStateFlow<List<HealthyAlternative>>(
        listOf(
            HealthyAlternative(
                id = "alt_1",
                nameAr = "وعاء الكينوا بالخضار المشوية وصدر الدجاج",
                nameEn = "Quinoa Veggie & Grilled Chicken Bowl",
                category = "غداء متكامل",
                calories = 380,
                protein = 35f,
                carbs = 36f,
                fat = 9f,
                sugar = 2.5f,
                reasonAr = "بديل ممتاز للوجبات السريعة؛ كربوهيدرات معقدة ذات مؤشر سكري منخفض مع دهون صحية قليلة.",
                benefitAr = "شبع يدوم 4-5 ساعات وطاقة مستقرة بدون خمول بعد الأكل."
            ),
            HealthyAlternative(
                id = "alt_2",
                nameAr = "عصير الليمون والزنجبيل والنعناع الطازج",
                nameEn = "Fresh Lemon Ginger Mint Infusion",
                category = "مشروب منعش",
                calories = 15,
                protein = 0.5f,
                carbs = 3f,
                fat = 0f,
                sugar = 1.0f,
                reasonAr = "بديل فائق الجودة للمشروبات الغازية والعصائر المصنعة المليئة بالسكر المكرر.",
                benefitAr = "ترطيب فائق للجسم، مضادات أكسدة، وتنشيط الجهاز الهضمي بدون سعرات زائدة."
            ),
            HealthyAlternative(
                id = "alt_3",
                nameAr = "زبادي يوناني طبيعي مع التوت الأزرق وبذور الشيا",
                nameEn = "Greek Yogurt with Blueberries & Chia",
                category = "وجبة خفيفة",
                calories = 190,
                protein = 20f,
                carbs = 14f,
                fat = 4f,
                sugar = 6f,
                reasonAr = "بديل للحلويات الدسمة ورقائق البطاطس؛ يمنح بروتين عالي وبروبيوتك للأمعاء.",
                benefitAr = "دعم صحة الجهاز الهضمي وبناء العضلات مع التحكم التام بالشهية."
            ),
            HealthyAlternative(
                id = "alt_4",
                nameAr = "شاي الماتشا الأخضر الياباني العضوي",
                nameEn = "Ceremonial Japanese Matcha Tea",
                category = "مشروب طاقة طبيعي",
                calories = 5,
                protein = 1f,
                carbs = 0.5f,
                fat = 0f,
                sugar = 0f,
                reasonAr = "بديل لمشروبات الطاقة الكيميائية والقهوة المحلاة بالكراميل.",
                benefitAr = "طاقة نقية مستمرة بفضل الـ L-Theanine ومضادات الأكسدة القوية (EGCG)."
            )
        )
    )
    val alternativesList: StateFlow<List<HealthyAlternative>> = _alternativesList.asStateFlow()

    init {
        // Pre-populate with high quality demo scan if database is empty
        viewModelScope.launch {
            if (scanHistory.value.isEmpty()) {
                val demo = FoodAnalysis(
                    detectedName = "سلطة الدجاج المشوي والأفوكادو",
                    detectedNameEn = "Grilled Chicken & Avocado Salad",
                    category = "meal",
                    confidence = 94,
                    portionEstimate = "طبق متوسط (280 غرام)",
                    calories = 365,
                    proteinGrams = 32f,
                    carbsGrams = 14f,
                    fatGrams = 18f,
                    sugarGrams = 4f,
                    fiberGrams = 7f,
                    sodiumMg = 320f,
                    healthScore = 88,
                    healthScoreReason = "تركيز بروتيني ممتاز، غنية بالدهون الأحادية غير المشبعة الصحية والألياف الطبيعية.",
                    explanation = "وجبة مثالية متوازنة تدعم بناء الكتلة العضلية وتحافظ على استقرار مستويات الجلوكوز والأنسولين في الدم.",
                    ingredients = "صدر دجاج مشوي، خس روماني، شرائح أفوكادو، طماطم كرزية، خيار، بذور يقطين، زيت زيتون بكر",
                    positiveAspects = "32 غرام بروتين صافي، دهون مفيدة للقلب، ألياف تحسن الهضم",
                    cautions = "مراقبة كمية الزيت المضاف عند الرغبة في عجز حاد بالسعرات",
                    recommendations = "أضف عصير الليمون أو خل التفاح لتحفيز امتصاص المغذيات",
                    isBeverage = false,
                    isFavorite = true
                )
                dao.insertScan(demo)
                _selectedAnalysis.value = demo
            }
        }
    }

    fun setNavIndex(index: Int) {
        _currentNavIndex.value = index
    }

    fun toggleFavorite(scan: FoodAnalysis) {
        viewModelScope.launch {
            val updated = scan.copy(isFavorite = !scan.isFavorite)
            dao.updateScan(updated)
            if (_selectedAnalysis.value?.id == scan.id) {
                _selectedAnalysis.value = updated
            }
        }
    }

    fun deleteScan(scan: FoodAnalysis) {
        viewModelScope.launch {
            dao.deleteScan(scan)
            if (_selectedAnalysis.value?.id == scan.id) {
                _selectedAnalysis.value = null
            }
        }
    }

    fun selectScanDetail(scan: FoodAnalysis) {
        _selectedAnalysis.value = scan
        _scanState.value = ScanUiState.Success(scan)
    }

    fun processCapturedImage(bitmap: Bitmap) {
        _currentCapturedBitmap.value = bitmap
        _scanState.value = ScanUiState.Scanning

        viewModelScope.launch {
            try {
                // Save image locally
                val filename = "scan_${System.currentTimeMillis()}.jpg"
                val file = File(getApplication<Application>().cacheDir, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }

                val result = aiProvider.analyzeFood(bitmap)
                result.onSuccess { analysis ->
                    val completeAnalysis = analysis.copy(localImagePath = file.absolutePath)
                    val id = dao.insertScan(completeAnalysis)
                    val saved = completeAnalysis.copy(id = id)
                    _selectedAnalysis.value = saved
                    _scanState.value = ScanUiState.Success(saved)

                    if (_isVoiceAutoPlay.value) {
                        speakAnalysis(saved)
                    }
                }.onFailure { err ->
                    _scanState.value = ScanUiState.Error(err.message ?: "تعذر إكمال التحليل الذكي")
                }
            } catch (e: Exception) {
                _scanState.value = ScanUiState.Error("حدث خطأ أثناء فحص الصورة: ${e.localizedMessage}")
            }
        }
    }

    fun triggerSampleFoodScan() {
        val context = getApplication<Application>()
        try {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sample_food_scan)
            if (bitmap != null) {
                processCapturedImage(bitmap)
            }
        } catch (e: Exception) {
            _scanState.value = ScanUiState.Error("تعذر تحميل العينة")
        }
    }

    fun speakAnalysis(analysis: FoodAnalysis) {
        val speechText = """
            مرحباً بك في NutriAI. تم تحليل العينة بنجاح: ${analysis.detectedName}.
            التقييم الصحي العام هو ${analysis.healthScore} من مئة.
            تحتوي على حوالي ${analysis.calories} سعرة حرارية، واثنين وثلاثين غرام بروتين.
            ${analysis.healthScoreReason}
            ${analysis.recommendations}
        """.trimIndent()
        voiceAssistant.speak(speechText)
    }

    fun stopVoice() {
        voiceAssistant.stop()
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(id = UUID.randomUUID().toString(), isUser = true, text = userText)
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch {
            val result = aiProvider.askAssistant(userText, _selectedAnalysis.value)
            val replyText = result.getOrElse { "عذراً، يرجى المحاولة مرة أخرى لاحقاً." }
            val botMsg = ChatMessage(id = UUID.randomUUID().toString(), isUser = false, text = replyText)
            _chatMessages.value = _chatMessages.value + botMsg
            _isChatLoading.value = false

            if (_isVoiceAutoPlay.value) {
                voiceAssistant.speak(replyText)
            }
        }
    }

    fun toggleVoiceAutoPlay() {
        _isVoiceAutoPlay.value = !_isVoiceAutoPlay.value
    }

    fun toggleEffects() {
        _isEffectsEnabled.value = !_isEffectsEnabled.value
    }

    fun setSpeechRate(rate: Float) {
        voiceAssistant.speechRate = rate
    }

    override fun onCleared() {
        super.onCleared()
        voiceAssistant.shutdown()
    }
}
