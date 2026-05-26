package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DialogueLine(
    val speaker: String, // "عمر" or "هاريسون" or "الراوي"
    val textAr: String,
    val textEn: String
)

data class ChatMessage(
    val sender: String, // "Sphinx" or "Player"
    val message: String
)

sealed interface GeminiState {
    object Idle : GeminiState
    object Loading : GeminiState
    data class Success(val response: String) : GeminiState
    data class Error(val error: String) : GeminiState
}

class AdventureViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GameDatabase.getDatabase(application)
    private val repository = GameRepository(database.adventureDao())

    val progressState: StateFlow<GameProgress?> = repository.progressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val relicsState: StateFlow<List<Relic>> = repository.relicsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Local States
    private val _dialogueIndex = MutableStateFlow(0)
    val dialogueIndex = _dialogueIndex.asStateFlow()

    private val _isChallengeActive = MutableStateFlow(false)
    val isChallengeActive = _isChallengeActive.asStateFlow()

    private val _isChallengeSolved = MutableStateFlow(false)
    val isChallengeSolved = _isChallengeSolved.asStateFlow()

    // 3D rotation states for the active artifact
    private val _rotationX = MutableStateFlow(0f)
    val rotationX = _rotationX.asStateFlow()

    private val _rotationY = MutableStateFlow(0f)
    val rotationY = _rotationY.asStateFlow()

    // Gemini Chat State
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory = _chatHistory.asStateFlow()

    private val _geminiState = MutableStateFlow<GeminiState>(GeminiState.Idle)
    val geminiState = _geminiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.initializeDefaultRelicsIfEmpty()
            // Make sure we have progress
            repository.getProgressDirect()
        }
    }

    fun updateRotation(x: Float, y: Float) {
        _rotationX.value = (_rotationX.value + x) % 360f
        _rotationY.value = (_rotationY.value + y) % 360f
    }

    fun resetRotation() {
        _rotationX.value = 0f
        _rotationY.value = 0f
    }

    // Story Dialogues Data
    fun getDialogueForChapter(chapter: Int): List<DialogueLine> {
        return when (chapter) {
            0 -> listOf(
                DialogueLine("الراوي", "في صحراء الجيزة الشاسعة والمليئة بالرمال الذهبية المتلألئة، يقف سائح تائه ينظر حوله بقلق وخوف...", "In the vast Golden sands in Giza, a tourist stands lost looking around with fear..."),
                DialogueLine("هاريسون", "أوه لا... أين أنا؟ بطارية هاتفي فارغة تماماً، والرمال في كل مكان! كيف سأعود لعائلتي عند الأهرامات؟! Help!", "Oh no! Empty phone battery, sand everywhere! How will I get back to my family at Pyramids?!"),
                DialogueLine("عمر", "يا هلا يا باشا! منور مصر! مالك واقف في الشمس كده ومحتار؟ أنا عمر محيي الدين، تحت أمرك يا صحبي!", "Welcome my friend! Why standing in the sun and confused? I am Omar Mohie El-Din, at your service!"),
                DialogueLine("هاريسون", "أوه! مرحباً عمر! أنا هاريسون.. أنا ضائع تماماً! لا أعرف كيف أصل للأهرامات أو أتواصل مع أي أحد!", "Oh! Hello Omar! I am Harrison.. I am totally lost! I don't know how to reach Pyramids or talk to anyone!"),
                DialogueLine("عمر", "ولا تشيل هم يا هاريسون! أنت مع المصريين يعني في أمان. خطوة بخطوة هرجعك لأهلك سالماً غانماً. بس الأول لازم تتعلم شوية كلام مصري عشان تسلك في الشارع!", "No worries Harrison! You are with Egyptians, you are safe! Move by move I'll return you to family. First, learn some Egyptian local slang!")
            )
            1 -> listOf(
                DialogueLine("الراوي", "يأخذ عمر صديقه الجديد هاريسون في جولة إلى خان الخليلي العريق لشراء خريطة بريدية وبوصلة لتحديد الاتجاهات...", "Omar takes Harrison to the ancient Khan El-Khalili Bazaar to buy a map and map directions..."),
                DialogueLine("هاريسون", "هذا المكان مذهل جداً وعريق! الروائح والأنوار هنا ساحرة كأنها من قصص ألف ليلة وليلة!", "This place is incredible and old! The scents and lamps are magical like 1001 Nights!"),
                DialogueLine("عمر", "حبيبي يا هاريسون، دا خان الخليلي أصل عبق التاريخ! بس عشان صاحب دكان الخرائط يدينا الخريطة الذهبية، سألنا لغز حسابي معقد من أيام الفاطميين!", "My friend Harrison, this is Khan El-Khalili, the breath of history! But the merchant challenges us to solve a Fatimid math riddle!"),
                DialogueLine("هاريسون", "لغز حسابي؟ أوه، أنا ممتاز في الرياضيات! أخبرني باللغز يا عمر لنحله فوراً ونحصل على الخريطة!", "A math riddle? Oh, I am perfect in math! Tell me the riddle Omar so we can solve it and get the map!")
            )
            2 -> listOf(
                DialogueLine("الراوي", "بعد الخان، يتجه عمر وهاريسون عبر ممشى الصحراء السري نحو الأهرامات، فيمرون بمعبد أثري قديم مدفون تحت الرمال...", "After the Bazaar, they head via the desert path, finding an ancient temple buried under the dunes..."),
                DialogueLine("عمر", "شايف المعبد دا يا هاريسون؟ دا مدخل الممر الملكي القديم للأهرامات.. بس البوابة مقفولة بتمائم سحرية ثلاثية الأبعاد!", "See this temple Harrison? This is the royal path to Pyramids.. but the gate is locked with 3D magic symbols!"),
                DialogueLine("هاريسون", "يا إلهي، إنه رائع! انظر هناك، هناك رموز هيروغليفية محفورة على تمثال مفتاح الحياة الذهبي والجعان المقدس!", "My god, it is lovely! Look, hieroglyphic writing carved on the golden Key of Life and Sacred Scarab!"),
                DialogueLine("عمر", "بالظبط! لازم ندوّر التمائم في الفضاء ثلاثي الأبعاد ونبحث عن الرموز المتطابقة عشان نفك الشفرة ونفتح الممر. يلا همتك معايا!", "Exactly! We need to rotate these symbols in 3D, inspect them, and find matching glyphs to decode the gate. Let's do it!")
            )
            3 -> listOf(
                DialogueLine("الراوي", "يقترب الصديقان من هضبة الجيزة، وفجأة يظهر أمامهما تمثال أبو الهول العظيم يلمع بنور ذهبي ساحر!", "The two friends approach the Giza plateau, and suddenly the majestic Sphinx glows in golden light!"),
                DialogueLine("عمر", "يا الله! حارس التاريخ بنفسه! أبو الهول صاحي عشان يختبرنا.. لازم يا هاريسون نكلمه بلغة حكيمة ونقنع بذكائنا عشان يفتح الممر الأخير!", "Oh! The Guardian of History himself! The Sphinx is awake to test us.. we must talk to him wisely to unlock the final gate!"),
                DialogueLine("هاريسون", "مدهش! يتحدث؟ سأسأله عن اللغز ومكان عائلتي باستخدام هذا اللوح الأثري الذكي.. ساعدني يا عمر في صياغة الكلمات!", "Amazing! It speaks? Let's use this smart golden tablet.. help me choose words so we satisfy the Great Sphinx!")
            )
            4 -> listOf(
                DialogueLine("الراوي", "يدخل عمر وهاريسون إلى سرداب فرعوني مفقود تحت هضبة الأهرام. هنا يرقد تابوت غامض يحمل قناع توت عنخ آمون الذهبي الساحر!", "Entering a lost pharaonic crypt under Giza, they see a mysterious sarcophagus carrying Tutankhamun's Golden Mask!"),
                DialogueLine("هاريسون", "يا إلهي! إنه بريق القناع الذهبي الشهير! لكن التابوت مقفل بقفل دوار ثلاثي الأبعاد لعنخ الرمزية!", "My God! It's the shine of the Golden Mask! But the sarcophagus is locked with a rotating 3D lock!"),
                DialogueLine("عمر", "صحيح يا صديقي! دا قفل زاوية الدوران المقدس. لازم نضبط زاوية دوران المجسم على زاوية اللغز لفتح السرداب ويطلع لنا القناع الذهبي!", "Yes my friend! This is the sacred angle rotation lock. We need to rotate the 3D model to the secret target angle to pop the gold mask!")
            )
            5 -> listOf(
                DialogueLine("الراوي", "يمر الصديقان بغرفة مخفية تسمى خزانة قارون الكبرى، وبها خرطوشة فرعونية عملاقة منقوش عليها رموز ملكية وأعداد غامضة...", "They pass through a hidden vault called Qarun's Treasury, holding a grand pharaonic cartouche carved with magic symbols and numbers..."),
                DialogueLine("عمر", "شوف الخرطوشة دي يا هاريسون! دا لغز تتابع الأعداد من كتاب الحساب الفرعوني القديم. لو كملنا السلسلة الحسابية، الخزينة هتتفتح!", "See this Cartouche Harrison! This is a number sequence riddle from the ancient Egyptian math registry. If we complete it, the vault opens!"),
                DialogueLine("هاريسون", "أنا جاهز يا عمر! أخبرني بتسلسل الأرقام وسأستخدم علم الجبر لحله والحصول على التميمة الملكية والخرطوشة!", "I'm ready Omar! Give me the sequence and I'll use algebraic equations to crack it and retrieve the Royal Cartouche!")
            )
            6 -> listOf(
                DialogueLine("الراوي", "يصل عمر وهاريسون إلى مسلة فلكية قديمة ترتفع عالياً نحو السماء، وتصوب حزمتها الضوئية البراقة نحو النجوم الشامخة في الفلك...", "They reach an ancient astronomical obelisk rising high, focusing its light beams towards the majestic stars in the sky..."),
                DialogueLine("عمر", "المسلة دي هي البوصلة الكونية للقدماء! وعشان نعدي للبوابة التالية، لازم نضبط الزوايا مع حركة النجوم وحزام أوريون الفلكي!", "This obelisk is the cosmic map of our ancestors! To pass through the next solar gate, we must align the obelisk elements with the stars!")
            )
            7 -> listOf(
                DialogueLine("الراوي", "أخيراً، بقرب الأهرامات، تقع أعينهم على مخطوطة بردية ذهبية تسمى 'بردية الحكمة المفقودة' التي صانت تاريخ الفراعنة لآلاف السنين...", "Finally near the pyramids, they spot a golden scroll called 'The Lost Scroll of Wisdom' preserving pharaonic history..."),
                DialogueLine("عمر", "دي آخر عقبة يا هاريسون وبنوصل لأهلك! لغز حارس المكتبة الفرعوني الأخير، وهو عبارة عن أسئلة ثقافية في تاريخ مصر الشامخ لفتح البوابة الأخيرة!", "This is the final hurdle Harrison! The library guardian's trivia quiz about Egypt's history to unlock the final gate!"),
                DialogueLine("هاريسون", "رائع جداً! لقد قرأت عشرات الكتب عن مصر والملوك الفراعنة. أنا مستعد للاختبار لكي ألقى عائلتي الشوقة!", "Splendid! I read dozens of books about Egypt's pharaoh kings. I'm ready to pass this quiz and join my beloved family!")
            )
            8 -> listOf(
                DialogueLine("الراوي", "وأخيراً! تنفتح البوابة الكبرى ويسقط النور الساطع ليكشف عن الأهرامات الثلاثة الشامخة وعائلة هاريسون وهي تلوح بسعادة وغبطة مفرطة!", "Finally! The grand gate opens to reveal the majestic Pyramids, and Harrison's family waving happily!"),
                DialogueLine("هاريسون", "أبي! أمي! أنا هنا! شكراً لك يا رب! لقد نجحت بفضلك ومساعدة أخي عمر محيي الدين!", "Father! Mother! I am here! Thank God! I succeeded with help of my brother Omar Mohie El-Din!"),
                DialogueLine("عمر", "الحمد لله على السلامة يا صاحبي! مصر بلدك التاني، وأي ضيف بييجي عندنا بنشيله في عيوننا وفي قلوبنا!", "Welcome back safely my friend! Egypt is your second home, we keep our guests in our eyes and hearts!"),
                DialogueLine("هاريسون", "لن أنسى كرمك ومروءتك أبداً يا عمر. أنت البطل الحقيقي العظيم! وسأخبر العالم كله عن جمال مصر وطيبة أهلها!", "I will never forget your generosity Omar! You are the true hero! I will tell the whole world about Egypt's beauty and kindness!")
            )
            else -> emptyList()
        }
    }

    fun nextDialogue() {
        val currentChapter = progressState.value?.currentChapter ?: 0
        val dialogues = getDialogueForChapter(currentChapter)
        if (_dialogueIndex.value < dialogues.size - 1) {
            _dialogueIndex.value++
        } else {
            // Reached end of dialogue, trigger challenge
            _isChallengeActive.value = true
            _isChallengeSolved.value = false
        }
    }

    fun solveChallengeAndUnlockRelic(relicId: String, points: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addScore(points)
            repository.unlockRelic(relicId)
            _isChallengeSolved.value = true
            _isChallengeActive.value = false
            _dialogueIndex.value = 0
            repository.advanceChapter()
        }
    }

    fun resetGame() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetGame()
            _dialogueIndex.value = 0
            _isChallengeActive.value = false
            _isChallengeSolved.value = false
            _chatHistory.value = emptyList()
            _geminiState.value = GeminiState.Idle
            resetRotation()
        }
    }

    // --- Gemini API Sphinx Oracle Integration ---
    fun askSphinx(userPrompt: String) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val chatHistoryList = _chatHistory.value.toMutableList()
        chatHistoryList.add(ChatMessage("Player", userPrompt))
        _chatHistory.value = chatHistoryList

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Emulate Golden Sphinx mystical offline response
            viewModelScope.launch {
                _geminiState.value = GeminiState.Loading
                kotlinx.coroutines.delay(1200)
                val mysticalAnswers = listOf(
                    "أنا حارس الرمال الشاهد على العصور. سؤالك ذكي، لكن قل لي يا هاريسون: ما هو الشيء الذي يمشى على أربعة في الصباح، وعلى اثنين في الظهيرة، وعلى ثلاثة في المساء؟ (اكتب الإجابة: الإنسان)",
                    "مرحباً بك يا سليل النور. لكشف ممر خوفو الذهبي، كم عدد السنين التي تطلبها بناء هرم الجيزة الأكبر؟ هل هي عشرين عاماً أم خمسون؟ (اكتب الإجابة: عشرين عاماً)",
                    "أ عمر محيي الدين يرافقك؟ نِعم الصديق الوفي. قل لي يا غريب: ما هو الطائر المقدس عند الفراعنة الذي يرمز للإله حورس وهو حامي الملوك؟ هل هو الصقر أم العقاب؟ (اكتب الإجابة: الصقر)"
                )
                val response = mysticalAnswers.random()
                _chatHistory.value = _chatHistory.value + ChatMessage("Sphinx", response)
                _geminiState.value = GeminiState.Success(response)
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _geminiState.value = GeminiState.Loading
            try {
                // Compile conversation history
                val systemPrompt = "أنت حارس الأهرامات التاريخي العظيم، أبو الهول الذهبي الحكيم. تتحدث بلغة عربية فصحى ساحرة وغامضة وشاعرية للغاية وبأسلوب مصري فيه فخر ووقار شديد. السائح هاريسون ومرافقه المصري البطل عمر محيي الدين محمد عبد الهادي والي واقفان أمامك يطلبون العبور للأهرامات. اطرح عليهم لغزاً تاريخياً ممتعاً وموجزاً عن الفراعنة والآثار المصرية، وإذا أجابوا بذكاء أو خفة دم، بارك عبورهم واجعل ردك شيقًا ولا يتعدى 3 أسطر."
                
                val messagesHistory = _chatHistory.value.map { msg ->
                    GeminiContent(parts = listOf(GeminiPart(text = "${if (msg.sender == "Player") "هاريسون وعمر" else "أبو الهول"}: ${msg.message}")))
                }

                // Append general instructions as system instruction
                val systemContent = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))

                val request = GeminiRequest(
                    contents = messagesHistory,
                    generationConfig = GeminiConfig(temperature = 0.7f, maxOutputTokens = 350)
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                    ?: "لقد تهادت الحكمة مع الرياح، اسألني ثانية يا رفيق الدرب."

                _chatHistory.value = _chatHistory.value + ChatMessage("Sphinx", textResponse)
                _geminiState.value = GeminiState.Success(textResponse)
            } catch (e: Exception) {
                val errorMsg = "سعل حراس المعبد مع عاصفة رملية.. جرب ثانية يا رفيق. (الخطأ: ${e.message})"
                _chatHistory.value = _chatHistory.value + ChatMessage("Sphinx", errorMsg)
                _geminiState.value = GeminiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}
