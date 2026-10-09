package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.ai.FirebaseAiFoodRecognitionService
import com.example.ai.FoodRecognitionService
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

    private val foodRecognitionService: FoodRecognitionService =
        FirebaseAiFoodRecognitionService(application)

    val voiceAssistant = FuturisticVoiceAssistant(application)

    private val _currentNavIndex = MutableStateFlow(0)
    val currentNavIndex: StateFlow<Int> = _currentNavIndex.asStateFlow()

    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private val _selectedAnalysis = MutableStateFlow<FoodAnalysis?>(null)
    val selectedAnalysis: StateFlow<FoodAnalysis?> =
        _selectedAnalysis.asStateFlow()

    private val _currentCapturedBitmap = MutableStateFlow<Bitmap?>(null)
    val currentCapturedBitmap: StateFlow<Bitmap?> =
        _currentCapturedBitmap.asStateFlow()

    val scanHistory: StateFlow<List<FoodAnalysis>> =
        dao.getAllScans().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                isUser = false,
                text = "مرحباً بك في محطة NutriAI الذكية! أنا مساعدك التغذوي الاصطناعي. كيف يمكنني مساعدتك في تحليل طعامك، تقييم السعرات، أو تقديم بدائل صحية متوازنة؟"
            )
        )
    )

    val chatMessages: StateFlow<List<ChatMessage>> =
        _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _isVoiceAutoPlay = MutableStateFlow(true)
    val isVoiceAutoPlay: StateFlow<Boolean> =
        _isVoiceAutoPlay.asStateFlow()

    private val _isEffectsEnabled = MutableStateFlow(true)
    val isEffectsEnabled: StateFlow<Boolean> =
        _isEffectsEnabled.asStateFlow()

    private val _alternativesList = MutableStateFlow(
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
                sugar = 1f,
                reasonAr = "بديل للمشروبات الغازية والعصائر المصنعة المليئة بالسكر المكرر.",
                benefitAr = "ترطيب الجسم ومضادات أكسدة دون سعرات زائدة."
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
                reasonAr = "بديل للحلويات الدسمة ورقائق البطاطس.",
                benefitAr = "يساعد على الشبع ويوفر البروتين."
            ),
            HealthyAlternative(
                id = "alt_4",
                nameAr = "شاي الماتشا الأخضر الياباني",
                nameEn = "Japanese Matcha Tea",
                category = "مشروب",
                calories = 5,
                protein = 1f,
                carbs = 0.5f,
                fat = 0f,
                sugar = 0f,
                reasonAr = "بديل للمشروبات المحلاة.",
                benefitAr = "يحتوي على مضادات الأكسدة والكافيين."
            )
        )
    )

    val alternativesList: StateFlow<List<HealthyAlternative>> =
        _alternativesList.asStateFlow()

    init {
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
                    healthScoreReason = "تركيز بروتيني ممتاز ودهون غير مشبعة وألياف.",
                    explanation = "وجبة متوازنة تحتوي على البروتين والدهون الصحية والخضروات.",
                    ingredients = "دجاج مشوي، خس، أفوكادو، طماطم، خيار، زيت زيتون",
                    positiveAspects = "بروتين وألياف ودهون مفيدة.",
                    cautions = "راقب كمية الزيت والملح المضاف.",
                    recommendations = "أضف الخضروات الطازجة وعصير الليمون.",
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
                val filename = "scan_${System.currentTimeMillis()}.jpg"
                val file = File(
                    getApplication<Application>().cacheDir,
                    filename
                )

                FileOutputStream(file).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                }

                val result = foodRecognitionService.recognizeFood(bitmap)

                result.onSuccess { analysis ->
                    val completeAnalysis = analysis.copy(
                        localImagePath = file.absolutePath
                    )

                    val id = dao.insertScan(completeAnalysis)
                    val saved = completeAnalysis.copy(id = id)

                    _selectedAnalysis.value = saved
                    _scanState.value = ScanUiState.Success(saved)

                    if (_isVoiceAutoPlay.value) {
                        speakAnalysis(saved)
                    }
                }.onFailure { error ->
                    _scanState.value = ScanUiState.Error(
                        error.message ?: "تعذر إكمال التحليل الذكي"
                    )
                }
            } catch (error: Exception) {
                _scanState.value = ScanUiState.Error(
                    "حدث خطأ أثناء فحص الصورة: ${error.localizedMessage}"
                )
            }
        }
    }

    fun triggerSampleFoodScan() {
        val context = getApplication<Application>()

        try {
            val bitmap = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.sample_food_scan
            )

            if (bitmap != null) {
                processCapturedImage(bitmap)
            } else {
                _scanState.value = ScanUiState.Error(
                    "تعذر تحميل صورة العينة"
                )
            }
        } catch (error: Exception) {
            _scanState.value = ScanUiState.Error(
                "تعذر تحميل العينة"
            )
        }
    }

    fun speakAnalysis(analysis: FoodAnalysis) {
        val speechText = """
            مرحباً بك في NutriAI.
            تم تحليل العينة: ${analysis.detectedName}.
            التقييم الصحي التقديري ${analysis.healthScore} من مئة.
            تحتوي على حوالي ${analysis.calories} سعرة حرارية.
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

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = userText
        )

        _chatMessages.value = _chatMessages.value + userMessage
        _isChatLoading.value = true

        viewModelScope.launch {
            try {
                val result = foodRecognitionService.consultNutritionAdvisor(
                    prompt = userText,
                    contextScan = _selectedAnalysis.value
                )

                val reply = result.getOrElse {
                    "عذراً، تعذر الحصول على إجابة. يرجى المحاولة مرة أخرى."
                }

                val botMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = reply
                )

                _chatMessages.value = _chatMessages.value + botMessage

                if (_isVoiceAutoPlay.value) {
                    voiceAssistant.speak(reply)
                }
            } catch (error: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "حدث خطأ أثناء معالجة رسالتك."
                )
            } finally {
                _isChatLoading.value = false
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
}package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.ai.FirebaseAiFoodRecognitionService
import com.example.ai.FoodRecognitionService
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

    private val foodRecognitionService: FoodRecognitionService =
        FirebaseAiFoodRecognitionService(application)

    val voiceAssistant = FuturisticVoiceAssistant(application)

    private val _currentNavIndex = MutableStateFlow(0)
    val currentNavIndex: StateFlow<Int> = _currentNavIndex.asStateFlow()

    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private val _selectedAnalysis = MutableStateFlow<FoodAnalysis?>(null)
    val selectedAnalysis: StateFlow<FoodAnalysis?> =
        _selectedAnalysis.asStateFlow()

    private val _currentCapturedBitmap = MutableStateFlow<Bitmap?>(null)
    val currentCapturedBitmap: StateFlow<Bitmap?> =
        _currentCapturedBitmap.asStateFlow()

    val scanHistory: StateFlow<List<FoodAnalysis>> =
        dao.getAllScans().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                isUser = false,
                text = "مرحباً بك في محطة NutriAI الذكية! أنا مساعدك التغذوي الاصطناعي. كيف يمكنني مساعدتك في تحليل طعامك، تقييم السعرات، أو تقديم بدائل صحية متوازنة؟"
            )
        )
    )

    val chatMessages: StateFlow<List<ChatMessage>> =
        _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _isVoiceAutoPlay = MutableStateFlow(true)
    val isVoiceAutoPlay: StateFlow<Boolean> =
        _isVoiceAutoPlay.asStateFlow()

    private val _isEffectsEnabled = MutableStateFlow(true)
    val isEffectsEnabled: StateFlow<Boolean> =
        _isEffectsEnabled.asStateFlow()

    private val _alternativesList = MutableStateFlow(
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
                sugar = 1f,
                reasonAr = "بديل للمشروبات الغازية والعصائر المصنعة المليئة بالسكر المكرر.",
                benefitAr = "ترطيب الجسم ومضادات أكسدة دون سعرات زائدة."
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
                reasonAr = "بديل للحلويات الدسمة ورقائق البطاطس.",
                benefitAr = "يساعد على الشبع ويوفر البروتين."
            ),
            HealthyAlternative(
                id = "alt_4",
                nameAr = "شاي الماتشا الأخضر الياباني",
                nameEn = "Japanese Matcha Tea",
                category = "مشروب",
                calories = 5,
                protein = 1f,
                carbs = 0.5f,
                fat = 0f,
                sugar = 0f,
                reasonAr = "بديل للمشروبات المحلاة.",
                benefitAr = "يحتوي على مضادات الأكسدة والكافيين."
            )
        )
    )

    val alternativesList: StateFlow<List<HealthyAlternative>> =
        _alternativesList.asStateFlow()

    init {
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
                    healthScoreReason = "تركيز بروتيني ممتاز ودهون غير مشبعة وألياف.",
                    explanation = "وجبة متوازنة تحتوي على البروتين والدهون الصحية والخضروات.",
                    ingredients = "دجاج مشوي، خس، أفوكادو، طماطم، خيار، زيت زيتون",
                    positiveAspects = "بروتين وألياف ودهون مفيدة.",
                    cautions = "راقب كمية الزيت والملح المضاف.",
                    recommendations = "أضف الخضروات الطازجة وعصير الليمون.",
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
                val filename = "scan_${System.currentTimeMillis()}.jpg"
                val file = File(
                    getApplication<Application>().cacheDir,
                    filename
                )

                FileOutputStream(file).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                }

                val result = foodRecognitionService.recognizeFood(bitmap)

                result.onSuccess { analysis ->
                    val completeAnalysis = analysis.copy(
                        localImagePath = file.absolutePath
                    )

                    val id = dao.insertScan(completeAnalysis)
                    val saved = completeAnalysis.copy(id = id)

                    _selectedAnalysis.value = saved
                    _scanState.value = ScanUiState.Success(saved)

                    if (_isVoiceAutoPlay.value) {
                        speakAnalysis(saved)
                    }
                }.onFailure { error ->
                    _scanState.value = ScanUiState.Error(
                        error.message ?: "تعذر إكمال التحليل الذكي"
                    )
                }
            } catch (error: Exception) {
                _scanState.value = ScanUiState.Error(
                    "حدث خطأ أثناء فحص الصورة: ${error.localizedMessage}"
                )
            }
        }
    }

    fun triggerSampleFoodScan() {
        val context = getApplication<Application>()

        try {
            val bitmap = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.sample_food_scan
            )

            if (bitmap != null) {
                processCapturedImage(bitmap)
            } else {
                _scanState.value = ScanUiState.Error(
                    "تعذر تحميل صورة العينة"
                )
            }
        } catch (error: Exception) {
            _scanState.value = ScanUiState.Error(
                "تعذر تحميل العينة"
            )
        }
    }

    fun speakAnalysis(analysis: FoodAnalysis) {
        val speechText = """
            مرحباً بك في NutriAI.
            تم تحليل العينة: ${analysis.detectedName}.
            التقييم الصحي التقديري ${analysis.healthScore} من مئة.
            تحتوي على حوالي ${analysis.calories} سعرة حرارية.
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

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = userText
        )

        _chatMessages.value = _chatMessages.value + userMessage
        _isChatLoading.value = true

        viewModelScope.launch {
            try {
                val result = foodRecognitionService.consultNutritionAdvisor(
                    prompt = userText,
                    contextScan = _selectedAnalysis.value
                )

                val reply = result.getOrElse {
                    "عذراً، تعذر الحصول على إجابة. يرجى المحاولة مرة أخرى."
                }

                val botMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = reply
                )

                _chatMessages.value = _chatMessages.value + botMessage

                if (_isVoiceAutoPlay.value) {
                    voiceAssistant.speak(reply)
                }
            } catch (error: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "حدث خطأ أثناء معالجة رسالتك."
                )
            } finally {
                _isChatLoading.value = false
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
