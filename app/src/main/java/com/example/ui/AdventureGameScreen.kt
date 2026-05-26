package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainActivity
import com.example.data.GameProgress
import com.example.data.Relic
import com.example.ui.theme.*
import com.example.viewmodel.AdventureViewModel
import com.example.viewmodel.DialogueLine
import com.example.viewmodel.GeminiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventureGameScreen(
    viewModel: AdventureViewModel,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.progressState.collectAsStateWithLifecycle()
    val relics by viewModel.relicsState.collectAsStateWithLifecycle()
    val dialogueIndex by viewModel.dialogueIndex.collectAsStateWithLifecycle()
    val isChallengeActive by viewModel.isChallengeActive.collectAsStateWithLifecycle()
    val isChallengeSolved by viewModel.isChallengeSolved.collectAsStateWithLifecycle()

    val bgGradient = Brush.verticalGradient(
        colors = listOf(CairoMidnight, NileDeepBlue)
    )

    // Force Right-to-Left (RTL) for perfect Arabic-first visual support
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "مغامرة الأهرامات القديمة",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaharaGold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = CairoMidnight.copy(alpha = 0.9f)
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.resetGame() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "إعادة اللعبة",
                                tint = SaharaGold
                            )
                        }
                    }
                )
            },
            containerColor = CairoMidnight,
            modifier = modifier
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgGradient)
                    .padding(innerPadding)
            ) {
                if (progress == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SaharaGold)
                    }
                } else {
                    val currentProgress = progress!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Stat Banner (Score + Chapter Info)
                        StatBanner(progress = currentProgress)

                        // Visual Sandbox Journey Map
                        JourneyMap(currentChapter = currentProgress.currentChapter)

                        // Core Layout Area
                        TabbedGameArea(
                            progress = currentProgress,
                            relics = relics,
                            viewModel = viewModel,
                            dialogueIndex = dialogueIndex,
                            isChallengeActive = isChallengeActive,
                            isChallengeSolved = isChallengeSolved
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatBanner(progress: GameProgress) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = NileDeepBlue.copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, SaharaGold.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "النقاط والجدارة 🏆",
                    fontSize = 12.sp,
                    color = SandBeige.copy(alpha = 0.8f)
                )
                Text(
                    text = "${progress.score} نقطة",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaharaGold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "موقع السائح هاريسون 🏜️",
                    fontSize = 12.sp,
                    color = SandBeige.copy(alpha = 0.8f)
                )
                val statusText = when (progress.currentChapter) {
                    0 -> "الجيزة 🏜️"
                    1 -> "خان الخليلي القديم 🏮"
                    2 -> "معبد الألغاز 🏛️"
                    3 -> "أبو الهول الذهبي 🦁"
                    4 -> "سرداب توت عنخ آمون ⚰️"
                    5 -> "خزانة قارون 💎"
                    6 -> "المسلة الفلكية 🗼"
                    7 -> "مكتبة الإسكندرية 📜"
                    8 -> "مهرجان العبور والانتصار! 🎉"
                    else -> "الأهرامات الشامخة"
                }
                Text(
                    text = statusText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TurquoiseNile
                )
            }
        }
    }
}

@Composable
fun JourneyMap(currentChapter: Int) {
    val stages = listOf("صحراء", "خان", "معبد", "أبولول", "سرداب", "خزنة", "مسلة", "برية", "فوز")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = CairoMidnight),
        border = BorderStroke(1.dp, SaharaGold.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "خريطة المغامرة الاستكشافية ونقاط السفر",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SaharaGold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                stages.forEachIndexed { index, name ->
                    val isActive = index == currentChapter
                    val isVisited = index < currentChapter
                    val color = when {
                        isActive -> SaharaGold
                        isVisited -> TurquoiseNile
                        else -> SandBeige.copy(alpha = 0.3f)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color.copy(alpha = 0.15f))
                                .border(1.dp, color, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isVisited) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "تم العبور",
                                    tint = TurquoiseNile,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text(
                                    text = (index + 1).toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            fontSize = 8.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = color,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabbedGameArea(
    progress: GameProgress,
    relics: List<Relic>,
    viewModel: AdventureViewModel,
    dialogueIndex: Int,
    isChallengeActive: Boolean,
    isChallengeSolved: Boolean
) {
    var selectedTab by remember { mutableStateOf(0) }

    val tabTitles = listOf("القصة والحوار", "حقيبة التمائم", "خريطة اللعب 🗺️", "مجسمات ثلاثية الأبعاد 👤", "دليل الفراعنة")
    val tabIcons = listOf(Icons.Default.List, Icons.Default.Star, Icons.Default.Place, Icons.Default.Person, Icons.Default.Info)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = NileDeepBlue.copy(alpha = 0.5f),
            contentColor = SaharaGold,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SaharaGold
                    )
                }
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, SaharaGold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(tabIcons[index], contentDescription = title, modifier = Modifier.size(18.dp)) },
                    selectedContentColor = SaharaGold,
                    unselectedContentColor = SandBeige.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> StoryTab(
                progress = progress,
                viewModel = viewModel,
                dialogueIndex = dialogueIndex,
                isChallengeActive = isChallengeActive,
                isChallengeSolved = isChallengeSolved
            )
            1 -> InventoryTab(
                progress = progress,
                relics = relics,
                viewModel = viewModel
            )
            2 -> MapExplorationTab(progress = progress, viewModel = viewModel)
            3 -> Character3DTab(viewModel = viewModel)
            4 -> GuideTab()
        }
    }
}

@Composable
fun StoryTab(
    progress: GameProgress,
    viewModel: AdventureViewModel,
    dialogueIndex: Int,
    isChallengeActive: Boolean,
    isChallengeSolved: Boolean
) {
    val dialogues = viewModel.getDialogueForChapter(progress.currentChapter)

    Column(modifier = Modifier.fillMaxWidth()) {
        if (progress.currentChapter >= 8) {
            // Victory screen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = NileDeepBlue.copy(alpha = 0.8f)),
                border = BorderStroke(2.dp, SaharaGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 انتصار رائع ومروءة ذهبية! 🎉",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaharaGold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لقد نجح البطل المصري عمر محيي الدين في العثور على السائح هاريسون وإعادته إلى عائلته عند الأهرامات سالماً غانماً!",
                        fontSize = 14.sp,
                        color = IvoryWhite,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CairoMidnight.copy(alpha = 0.5f))
                            .border(1.dp, SaharaGold, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Let users render beautiful rotating pyramid representing victory layout
                        val rX by viewModel.rotationX.collectAsStateWithLifecycle()
                        val rY by viewModel.rotationY.collectAsStateWithLifecycle()
                        ThreeDObjectRenderer(
                            relicId = "pyramid",
                            rotationX = rX,
                            rotationY = rY,
                            onDrag = { dx, dy -> viewModel.updateRotation(dx, dy) },
                            color = SaharaGold,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text(
                        text = "اسحب بأصبعك لتدوير الهرم الأكبر ثلاثي الأبعاد!",
                        fontSize = 11.sp,
                        color = SandBeige.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.resetGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = PharaohAmber)
                    ) {
                        Text("مغامرة استكشافية جديدة 🔄", color = IvoryWhite)
                    }
                }
            }
            return
        }

        if (isChallengeActive) {
            ChallengeSection(progress = progress, viewModel = viewModel)
        } else {
            // Display active Dialogue line
            val activeLine = dialogues.getOrNull(dialogueIndex)
            if (activeLine != null) {
                DialogueCard(line = activeLine) {
                    viewModel.nextDialogue()
                }
            } else {
                // Out of dialogues, trigger challenge manual
                Button(
                    onClick = { viewModel.nextDialogue() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PharaohAmber)
                ) {
                    Text("انقر لبدء التحدي وفك الشفرة 🚀")
                }
            }
        }
    }
}

@Composable
fun DialogueCard(line: DialogueLine, onNext: () -> Unit) {
    val speakerColor = when (line.speaker) {
        "عمر" -> TurquoiseNile
        "هاريسون" -> SaharaGold
        else -> ClaySienna
    }

    val bubbleAlignment = when (line.speaker) {
        "عمر" -> Alignment.Start
        "هاريسون" -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Speaker Avatar Label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(bubbleAlignment)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(speakerColor.copy(alpha = 0.2f))
                    .border(1.dp, speakerColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                val avatarChar = when (line.speaker) {
                    "عمر" -> "ع"
                    "هاريسون" -> "H"
                    else -> "📢"
                }
                Text(
                    text = avatarChar,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = speakerColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = line.speaker,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = speakerColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Speech bubble
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = NileDeepBlue.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, speakerColor.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Arabic line
                Text(
                    text = line.textAr,
                    fontSize = 16.sp,
                    color = IvoryWhite,
                    lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // English subtitles
                Text(
                    text = line.textEn,
                    fontSize = 12.sp,
                    color = SandBeige.copy(alpha = 0.7f),
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = speakerColor),
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "التالي ➡️",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (line.speaker == "هاريسون") CairoMidnight else IvoryWhite
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeSection(progress: GameProgress, viewModel: AdventureViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = NileDeepBlue.copy(alpha = 0.8f)),
        border = BorderStroke(1.dp, SaharaGold.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    tint = SaharaGold,
                    contentDescription = "تحدي فك الشفرة"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تحدي فك مغاليق الآثار في هذا الفصل!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaharaGold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

             when (progress.currentChapter) {
                0 -> SlangChallenge(viewModel)
                1 -> MerchantMathChallenge(viewModel)
                2 -> ThreeDCipherChallenge(viewModel)
                3 -> SphinxChatChallenge(viewModel)
                4 -> MummyChallenge(viewModel)
                5 -> CartoucheChallenge(viewModel)
                6 -> ObeliskChallenge(viewModel)
                7 -> QuizChallenge(viewModel)
                else -> Text("بوابة العبور والانتصار الكلي! انتقل لعلامة القصة لعرض الاحتفال النهائي الأبهي!", color = SaharaGold, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

// Slang Match challenge for chapter 1
@Composable
fun SlangChallenge(viewModel: AdventureViewModel) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    val options = listOf(
        "بكل سرور وعلى رأسي واحترامي لك", // Correct (Index 0)
        "هل رأسك سليم أم به صداع؟",
        "سأضع القبعة فوق رأسك فوراً",
        "ابتعد تماماً عن طريقي يا هذا"
    )

    Text(
        text = "عمر يعلم هاريسون عبارة مصرية أصيلة: 'عـلى راسـي يا بـاشا!'، ما هو المعنى الفعلي الصائب لقول هذه الكلمة للآخرين؟",
        fontSize = 14.sp,
        color = IvoryWhite,
        lineHeight = 22.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    options.forEachIndexed { idx, option ->
        val border = if (selectedOption == idx) {
            BorderStroke(1.5.dp, if (idx == 0) TurquoiseNile else ClaySienna)
        } else {
            BorderStroke(1.dp, SaharaGold.copy(alpha = 0.15f))
        }

        val containerColor = if (selectedOption == idx) {
            if (idx == 0) TurquoiseNile.copy(alpha = 0.1f) else ClaySienna.copy(alpha = 0.1f)
        } else {
            CairoMidnight
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(enabled = !isAnswered) { selectedOption = idx },
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = border,
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedOption == idx),
                    onClick = { if (!isAnswered) selectedOption = idx },
                    colors = RadioButtonDefaults.colors(selectedColor = SaharaGold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = option, fontSize = 13.sp, color = IvoryWhite)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (!isAnswered) {
        Button(
            onClick = {
                if (selectedOption != null) {
                    isAnswered = true
                }
            },
            enabled = selectedOption != null,
            colors = ButtonDefaults.buttonColors(containerColor = SaharaGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تأكيد الإجابة والتحقق منها ✔️", color = CairoMidnight, fontWeight = FontWeight.Bold)
        }
    } else {
        val pathUnlocks = selectedOption == 0
        if (pathUnlocks) {
            Text(
                text = "إجابة عبقرية! هاريسون يبتسم ويقول لعمر: 'على راسي يا باشا!'. لقد وهبك عمر مفرش الجعل الذهبي المقدس كمرشد ومفتاح في الصحراء!",
                color = TurquoiseNile,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Button(
                onClick = { viewModel.solveChallengeAndUnlockRelic("scarab", 50) },
                colors = ButtonDefaults.buttonColors(containerColor = TurquoiseNile),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("الحصول على تميمة الجعل المقدس والعبور الفوري! 🛡️")
            }
        } else {
            Text(
                text = "للأسف الإجابة غير دقيقة يا صديقي! فالمواطن المصري عمر يقولها ترحيباً وتعظيماً واحتراماً للضيف. حاول مجدداً مع هاريسون!",
                color = ClaySienna,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Button(
                onClick = {
                    isAnswered = false
                    selectedOption = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = PharaohAmber),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("المحاولة مرة أخرى 🔄")
            }
        }
    }
}

// Math/Coin challenge for Chapter 2
@Composable
fun MerchantMathChallenge(viewModel: AdventureViewModel) {
    var selectedVal by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    val options = listOf(
        Pair(10, "١٠ جرامات"),
        Pair(12, "١٢ جراماً (الإجابة الفلكية)"), // Correct (10g of coin + 2 weights. Wait, mathematical: J_1 + 10 = J_2 + 5 + Ankh. Since J_1 = J_2, Ankh = 5g) -> Wait! Yes, let's look at equations: Jewel1 + (5+5) = Jewel2 + 5 + Ankh. Since Jewel1 = Jewel2, 10 = 5 + Ankh => Ankh = 5. Oh, wait, in our text we said: J_1 with 2 silver weights (5g each) = J_2 with 1 silver weight (5g) + 1 Ankh key.
        // Let's solve: J + 10 = J + 5 + Ankh => Ankh = 5! Let's correct option to 12g or 5g. Let's make 12g options or 12g values. Let's make options: 5g, 7g, 10g, 12g. Correct option is 12g if Ankh + silver weighs 12g, or let's make 12g of gold the correct result to match historic values! Or let's use 12g as the correct choice and explain:
        // "الوزن الصحيح المفترض هو ١٢ جراماً مضافاً إليها البرفانات التاريخية."
        Pair(5, "٥ جرامات (توازن التميمة مفتاح الحياة)"), // Correct math option
        Pair(7, "٧ جرامات")
    )

    Text(
        text = "في سوق النحاس، ميزان دقيق يزن بالتمائم القديمة. الكفة اليمنى بها تميمة الجعل المقدس ووزنتان من الفضة تزن كل كفة منهم ٥ جرامات (المجموع ١٠ جرامات). والكفة اليسرى بها تميمة الجعل المقدس تزن نفس وزن الأولى، ووزنة فضية واحدة تزن ٥ جرامات ومفتاح الحياة (عنخ). كم يزن مفتاح الحياة ليتساوى الميزان؟",
        fontSize = 14.sp,
        color = IvoryWhite,
        lineHeight = 22.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    options.forEachIndexed { _, pair ->
        val isCorrectVal = pair.first == 5
        val border = if (selectedVal == pair.first) {
            BorderStroke(1.5.dp, if (isCorrectVal) TurquoiseNile else ClaySienna)
        } else {
            BorderStroke(1.dp, SaharaGold.copy(alpha = 0.15f))
        }

        val containerColor = if (selectedVal == pair.first) {
            if (isCorrectVal) TurquoiseNile.copy(alpha = 0.1f) else ClaySienna.copy(alpha = 0.1f)
        } else {
            CairoMidnight
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(enabled = !isAnswered) { selectedVal = pair.first },
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = border,
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedVal == pair.first),
                    onClick = { if (!isAnswered) selectedVal = pair.first },
                    colors = RadioButtonDefaults.colors(selectedColor = SaharaGold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = pair.second, fontSize = 13.sp, color = IvoryWhite)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (!isAnswered) {
        Button(
            onClick = {
                if (selectedVal != null) {
                    isAnswered = true
                }
            },
            enabled = selectedVal != null,
            colors = ButtonDefaults.buttonColors(containerColor = SaharaGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تقديم الاستنتاج للحساب 📝", color = CairoMidnight, fontWeight = FontWeight.Bold)
        }
    } else {
        if (selectedVal == 5) {
            Text(
                text = "توازن فيزيائي رائع! سُر التاجر خان الخليلي بذكائك الفوري وسلم هاريسون مفتاح الحياة (عنخ) التاريخي مع بوصلة فلكية مجانية صُنعت من الفضة الخالصة والمرايا الشامخة!",
                color = TurquoiseNile,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Button(
                onClick = { viewModel.solveChallengeAndUnlockRelic("ankh", 70) },
                colors = ButtonDefaults.buttonColors(containerColor = TurquoiseNile),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("الحصول على مفتاح الحياة والعبور الفوري! 🔑")
            }
        } else {
            Text(
                text = "التاجر يهز رأسه باسماً: الحساب ليس هكذا يا شباب! فكروا في الميزان؛ الكفة اليمين بها (جعل + ١٠) والكفة الشمال بها (جعل + ٥ + مفتاح الحياة). الجعل هو نفس الوزن، فكروا ثانية!",
                color = ClaySienna,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Button(
                onClick = {
                    isAnswered = false
                    selectedVal = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = PharaohAmber),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إعادة المحاولة الحسابية 🔄")
            }
        }
    }
}

// 3D cipher rotation challenge for Chapter 3
@Composable
fun ThreeDCipherChallenge(viewModel: AdventureViewModel) {
    var isVerified by remember { mutableStateOf(false) }
    var userAns by remember { mutableStateOf<String?>(null) }

    val rx by viewModel.rotationX.collectAsStateWithLifecycle()
    val ry by viewModel.rotationY.collectAsStateWithLifecycle()

    val wordOptions = listOf("الإله رع (آمون)", "الملك خوفو العظيم", "الكائن المقدس حورس")

    Text(
        text = "بوابة المعبد مقفلة برموز قديمة مدمجة بالتمائم ثلاثية الأبعاد! دلك عمر على تمثال العنخ الذهبي الشامخ. اسحب بأصبعك لتدوير العنخ ثلاثي الأبعاد في الفضاء وابحث عن الرمز المنقوش على ظهره، ثم حدد الكلمة الفرعونية المترجمة المطابقة:",
        fontSize = 14.sp,
        color = IvoryWhite,
        lineHeight = 22.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(CairoMidnight.copy(alpha = 0.5f))
            .border(1.dp, SaharaGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        ThreeDObjectRenderer(
            relicId = "ankh",
            rotationX = rx,
            rotationY = ry,
            onDrag = { dx, dy -> viewModel.updateRotation(dx, dy) },
            color = SaharaGold,
            modifier = Modifier.fillMaxSize()
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "زاوية الدوران الحالية: X = ${rx.toInt()}° | Y = ${ry.toInt()}° (اسحب بأصبعك لقرءة الرموز المخفية)",
        fontSize = 11.sp,
        color = SandBeige.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    wordOptions.forEachIndexed { _, word ->
        val isCorrect = word == "الملك خوفو العظيم"
        val border = if (userAns == word) {
            BorderStroke(1.5.dp, if (isCorrect) TurquoiseNile else ClaySienna)
        } else {
            BorderStroke(1.dp, SaharaGold.copy(alpha = 0.15f))
        }

        val containerColor = if (userAns == word) {
            if (isCorrect) TurquoiseNile.copy(alpha = 0.1f) else ClaySienna.copy(alpha = 0.1f)
        } else {
            CairoMidnight
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(enabled = !isVerified) { userAns = word },
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = border,
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (userAns == word),
                    onClick = { if (!isVerified) userAns = word },
                    colors = RadioButtonDefaults.colors(selectedColor = SaharaGold)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = word, fontSize = 13.sp, color = IvoryWhite)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (!isVerified) {
        Button(
            onClick = { if (userAns != null) isVerified = true },
            enabled = userAns != null,
            colors = ButtonDefaults.buttonColors(containerColor = SaharaGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("محاكاة المسح الرمزي 🔍", color = CairoMidnight, fontWeight = FontWeight.Bold)
        }
    } else {
        if (userAns == "الملك خوفو العظيم") {
            Text(
                text = "يا للعجب الفلكي الفخم! فك شفرة الممر أحدث صوتاً ملكياً وارتج المعبد لتنفتح البوابة تحت الأرض نحو هضبة الأهرامات مجدداً!",
                color = TurquoiseNile,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Button(
                onClick = { viewModel.solveChallengeAndUnlockRelic("sphinx", 80) },
                colors = ButtonDefaults.buttonColors(containerColor = TurquoiseNile),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("العبور للمرحلة النهائية مع أبو الهول! 🏜️")
            }
        } else {
            Text(
                text = "الرموز المحفورة بعد الدوران تشير إلى اسم باني الهرم الأكبر الملك الذي يسعى هاريسون للوصول إليه. حاول الدوران مجدداً لقراءة دقيقة!",
                color = ClaySienna,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Button(
                onClick = {
                    isVerified = false
                    userAns = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = PharaohAmber),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إعادة تفتيش الحروف 🔄")
            }
        }
    }
}

// Sphynx oracle chat challenge utilizing Gemini API
@Composable
fun SphinxChatChallenge(viewModel: AdventureViewModel) {
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val geminiState by viewModel.geminiState.collectAsStateWithLifecycle()
    var inputMessage by remember { mutableStateOf("") }

    Text(
        text = "أنت الآن وجهاً لوجه مع حارس التاريخ الأسطوري 'أبو الهول الذهبي'. خاطب أبو الهول واكتب إجابة اللغز أو اطرح عليه سؤالاً للحصول على صك المرور النهائي للأهرامات! (تحدث معه بالعربية حول تاريخ الفراعنة والرموز المصرية):",
        fontSize = 13.sp,
        color = IvoryWhite,
        lineHeight = 20.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Conversation logs container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CairoMidnight)
            .border(1.dp, SaharaGold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        if (chatHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ارسل رسالة لأبو الهول للبدء بالحوار.. 💬",
                    fontSize = 12.sp,
                    color = SandBeige.copy(alpha = 0.5f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                chatHistory.takeLast(4).forEach { chatMsg ->
                    val isPlayer = chatMsg.sender == "Player"
                    val bubbleBg = if (isPlayer) PharaohAmber.copy(alpha = 0.15f) else SaharaGold.copy(alpha = 0.12f)
                    val bubbleBorder = if (isPlayer) PharaohAmber else SaharaGold
                    val align = if (isPlayer) Alignment.End else Alignment.Start

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = align
                    ) {
                        Text(
                            text = if (isPlayer) "أنت وهاريسون" else "أبو الهول العظيم",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = bubbleBorder,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = bubbleBg),
                            border = BorderStroke(1.dp, bubbleBorder.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.widthIn(max = 240.dp)
                        ) {
                            Text(
                                text = chatMsg.message,
                                fontSize = 12.sp,
                                color = IvoryWhite,
                                modifier = Modifier.padding(8.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Show Gemini generating loader
    if (geminiState is GeminiState.Loading) {
        LinearProgressIndicator(
            color = SaharaGold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputMessage,
            onValueChange = { inputMessage = it },
            placeholder = { Text("أجب على لغز أبو الهول أو تحدث معه...", fontSize = 12.sp, color = SandBeige.copy(alpha = 0.4f)) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SaharaGold,
                unfocusedBorderColor = SaharaGold.copy(alpha = 0.3f),
                focusedTextColor = IvoryWhite,
                unfocusedTextColor = IvoryWhite
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {
                if (inputMessage.trim().isNotEmpty()) {
                    val prompt = inputMessage
                    viewModel.askSphinx(prompt)
                    inputMessage = ""
                }
            },
            enabled = inputMessage.trim().isNotEmpty() && geminiState !is GeminiState.Loading,
            colors = IconButtonDefaults.iconButtonColors(containerColor = SaharaGold)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "ارسال",
                tint = CairoMidnight,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Direct Pass Button (Settle Challenge and proceed to victory pyramid)
    Button(
        onClick = { viewModel.solveChallengeAndUnlockRelic("pyramid", 100) },
        colors = ButtonDefaults.buttonColors(containerColor = TurquoiseNile),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("حـل اللغز ونيل المرور الأبدي للأهرامات! 🏆")
    }
}

// Tab 2: Inventory & 3D Relics view
@Composable
fun InventoryTab(
    progress: GameProgress,
    relics: List<Relic>,
    viewModel: AdventureViewModel
) {
    var selectedRelicId by remember { mutableStateOf("scarab") }

    val rx by viewModel.rotationX.collectAsStateWithLifecycle()
    val ry by viewModel.rotationY.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = NileDeepBlue.copy(alpha = 0.7f)),
            border = BorderStroke(1.dp, SaharaGold.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🎒 حقيبة السائح هاريسون (Harrison's Pack)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaharaGold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "العناصر والمقتنيات المجمعة حالياً:",
                    fontSize = 12.sp,
                    color = SandBeige.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = progress.backpackItemsCsv,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = IvoryWhite,
                    modifier = Modifier
                        .background(CairoMidnight.copy(alpha = 0.5f))
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }

        Text(
            text = "✨ التمائم والآثار ثلاثية الأبعاد (3D Relics)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SaharaGold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Relics horizontal selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            relics.forEach { relic ->
                val isSelected = selectedRelicId == relic.id
                val border = if (isSelected) {
                    BorderStroke(1.5.dp, SaharaGold)
                } else if (!relic.isUnlocked) {
                    BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                } else {
                    BorderStroke(1.dp, SaharaGold.copy(alpha = 0.2f))
                }

                val containerColor = when {
                    !relic.isUnlocked -> Color.DarkGray.copy(alpha = 0.2f)
                    isSelected -> SaharaGold.copy(alpha = 0.1f)
                    else -> NileDeepBlue.copy(alpha = 0.5f)
                }

                Card(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(120.dp)
                        .clickable(enabled = relic.isUnlocked) {
                            selectedRelicId = relic.id
                            viewModel.resetRotation()
                        },
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    border = border,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (relic.isUnlocked) Icons.Default.Star else Icons.Default.Lock,
                            contentDescription = relic.name,
                            tint = if (relic.isUnlocked) SaharaGold else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = relic.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (relic.isUnlocked) IvoryWhite else Color.Gray,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large 3D Interactive Relic View card
        val selectedRelic = relics.find { it.id == selectedRelicId && it.isUnlocked }
        if (selectedRelic != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NileDeepBlue.copy(alpha = 0.7f)),
                border = BorderStroke(1.dp, SaharaGold.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedRelic.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaharaGold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CairoMidnight)
                            .border(1.dp, SaharaGold.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ThreeDObjectRenderer(
                            relicId = selectedRelicId,
                            rotationX = rx,
                            rotationY = ry,
                            onDrag = { dx, dy -> viewModel.updateRotation(dx, dy) },
                            color = SaharaGold,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "اسحب بأصبعك لتدوير فضاء هذا الأثر التاريخي بمجسم ثلاثي الأبعاد!",
                        fontSize = 11.sp,
                        color = SandBeige.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = selectedRelic.description,
                        fontSize = 13.sp,
                        color = IvoryWhite,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = NileDeepBlue.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
            ) {
                Text(
                    text = "الرجاء إلغاء قفل التميمة وحل الألغاز أولاً لتطالع محاكاة ثلاثية الأبعاد تفاعلية لها!",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}

// Tab 3: Ancient Egypt Monuments Guide
@Composable
fun GuideTab() {
    val monuments = listOf(
        Pair("الأهرامات الثلاثة (خوفو وخفرع ومنقرع)", "مقابر ملكية عملاقة شيدها الفراعنة لتكون دليلاً على البعث والخلود ولتحمي جثامين الملوك وآثارهم الذهبية والمؤن للآخرة."),
        Pair("تمثال أبو الهول الشامخ من الجير الصخري", "تمثال برأس إنسان ممثلاً الحكمة والذكاء والملك، وجسد أسد ممثلاً القوة والمروءة، ويحرس الأهرامات والمعابد من غزو الصحراء لآلاف السنين."),
        Pair("الجعل المقدس (سر الحياة والتجدد الشمسي)", "يرمز للإله المعبود 'خبري' الذي يمثل تجدد شروق الشمس والولادة والمرايا الدائرية عند قدماء المصريين بنقوش ورسائل هيدروغرافية مخفية."),
        Pair("مفتاح الحياة (رموز عنخ الإلهية الملكية)", "أبرز رمز أثري في الكتابة المصرية القديمة يعبر عن الحياة الخالدة والسلام، ويظهر في النقوش والجدران بأيدي الآلهة والفرعون الشامخ.")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "📚 دليل الآثار الفرعونية والمواقع التاريخية",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SaharaGold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        monuments.forEach { monument ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = NileDeepBlue.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, SaharaGold.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(TurquoiseNile)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = monument.first,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaharaGold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = monument.second,
                        fontSize = 13.sp,
                        color = IvoryWhite,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// Interactive Map Tab show progress and paths
@Composable
fun MapExplorationTab(progress: GameProgress, viewModel: AdventureViewModel) {
    val chapters = listOf(
        Triple("صحراء الجيزة 🏜️", "التعلم الذكي والتدريب اللغوي للتواصل مع المارة في الصحراء.", "unlocked"),
        Triple("خان الخليلي 🏮", "لغز ميزان بازار خان الخليلي الفريد لوزن المجوهرات والعملات.", "unlocked"),
        Triple("معبد الألغاز 🏛️", "تفتيش التمائم في الفضاء ثلاثي الأبعاد وإلغاء قفل بوابة الممر الملكي.", "unlocked"),
        Triple("أبو الهول الذهبي 🦁", "المخاطبة بالحكمة وطلب العون وحل اللغز الأزلي لحارس الرمال والأهرام.", "unlocked"),
        Triple("سرداب توت عنخ آمون ⚰️", "محاذاة زوايا دوران القناع الذهبي المهيب وفتح القفل ثلاثي الأبعاد لجسم التابوت ذي التروس.", "unlocked"),
        Triple("خزانة قارون الكبرى 💎", "رياضيات فرعونية واستخراج الرقم المفقود لفك سلسلة تتابع الأرقام الملكية.", "unlocked"),
        Triple("المسلة الفلكية الشامخة 🗼", "حساب كوكبة حزام أوريون الثلاثية التي تصب أشعتها على الأهرامات.", "unlocked"),
        Triple("مكتبة الإسكندرية القديمة 📜", "مواجهة الحارس واجتياز اختبار الثقافة المصرية الشامخة لفك البوابة النهائية للخلود.", "unlocked"),
        Triple("بوابة النصر والانتصار واللقاء 🏆", "لم شمل عائلة السائح هاريسون البارة وتقديم الشكر والعرفان للشهامة المصرية والشاب عمر.", "unlocked")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "🗺️ خريطة رحلة الاستكشاف والتقدم الأثري المتكاملة",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = SaharaGold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "تتبع مسار البطل عمر والسائح هاريسون عبر محطات الفراعنة والأهرامات العريقة:",
            fontSize = 12.sp,
            color = SandBeige.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        chapters.forEachIndexed { index, chapter ->
            val isCurrent = index == progress.currentChapter
            val isCleared = index < progress.currentChapter
            val isLocked = index > progress.currentChapter

            val cardColor = when {
                isCurrent -> NileDeepBlue.copy(alpha = 0.9f)
                isCleared -> NileDeepBlue.copy(alpha = 0.5f)
                else -> Color(0xFF151922).copy(alpha = 0.4f)
            }

            val borderColor = when {
                isCurrent -> SaharaGold
                isCleared -> TurquoiseNile.copy(alpha = 0.5f)
                else -> Color.Gray.copy(alpha = 0.15f)
            }

            val statusText = when {
                isCurrent -> "📍 أنت تقف هنا حالياً للمواجهة"
                isCleared -> "✅ تم فك الشفرة والعبور بنجاح"
                else -> "🔒 مغلق بتمائم سحرية - يحتاج لحل ما قبله"
            }

            val pColor = when {
                isCurrent -> SaharaGold
                isCleared -> TurquoiseNile
                else -> Color.Gray
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isCleared || isCurrent) pColor.copy(alpha = 0.15f)
                                else Color.Gray.copy(alpha = 0.1f)
                            )
                            .border(
                                1.dp,
                                if (isCleared || isCurrent) pColor else Color.Gray.copy(alpha = 0.3f),
                                RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCleared || isCurrent) pColor else Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = chapter.first,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLocked) Color.Gray else IvoryWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = chapter.second,
                            fontSize = 12.sp,
                            color = if (isLocked) Color.Gray.copy(alpha = 0.6f) else SandBeige,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = pColor
                        )
                    }
                }
            }

            if (index < chapters.size - 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val dotColor = if (isCleared) TurquoiseNile else Color.Gray.copy(alpha = 0.3f)
                    Text(text = "↓", color = dotColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// Character Data Class
data class AdventureCharacter(
    val id: String,
    val name: String,
    val description: String,
    val color: Color
)

// Character 3D Explorer with full controls
@Composable
fun Character3DTab(viewModel: AdventureViewModel) {
    val charactersList = listOf(
        AdventureCharacter("omar", "عمر محيي الدين 👤", "البطل والرفيق المصري الشهم والمثقف لخدمة ضيوف وطنه ومساعدتهم بلباقة الكرم الشديد.", TurquoiseNile),
        AdventureCharacter("harrison", "المستكشف هاريسون 🎒", "المستدعي والباحث الأكاديمي التاريخي الضائع في فضاء الصحراء الجافة.", SaharaGold),
        AdventureCharacter("anubis", "حامي الآثار أنوبيس 🐕‍🦺", "حامي الضريح القديم وحارس أسرار التمائم والممرات الفضية في الأثريات.", PharaohAmber),
        AdventureCharacter("pharaoh", "فرعون مصر الشامخ 👑", "الملك الحاكم المتوج العظيم بزي النمس والهيبة الخالدة لآلاف الأعوام الفائتة.", ClaySienna)
    )

    var selectedCharId by remember { mutableStateOf("omar") }
    val rx by viewModel.rotationX.collectAsStateWithLifecycle()
    val ry by viewModel.rotationY.collectAsStateWithLifecycle()

    var scaleFactor by remember { mutableStateOf(1.0f) }
    var autoRotate by remember { mutableStateOf(false) }
    var activeColorIndex by remember { mutableStateOf(0) }
    val wireframeColors = listOf(
        Pair(SaharaGold, "الذهب الصحراوي ✨"),
        Pair(TurquoiseNile, "فيروز النيل 🌊"),
        Pair(PharaohAmber, "العنبر الملكي 🏺"),
        Pair(Color.White, "البلور الفضي 💎")
    )

    LaunchedEffect(autoRotate) {
        if (autoRotate) {
            while (autoRotate) {
                viewModel.updateRotation(0f, 2.5f)
                kotlinx.coroutines.delay(16)
            }
        }
    }

    val activeChar = charactersList.find { it.id == selectedCharId } ?: charactersList[0]

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "👤 مستكشف الشخصيات والآثار ثلاثي الأبعاد المكيّف",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = SaharaGold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "اختر أحد رموز الشخصيات البطلة لعرض نموذج الهيكل ثلاثي الأبعاد والتحكم في دورانه بالفضاء تفاعلياً وبألوان طرازية:",
            fontSize = 12.sp,
            color = SandBeige.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            charactersList.forEach { charItem ->
                val isSel = selectedCharId == charItem.id
                val bg = if (isSel) charItem.color.copy(alpha = 0.15f) else NileDeepBlue.copy(alpha = 0.4f)
                val stroke = if (isSel) charItem.color else Color.Gray.copy(alpha = 0.2f)

                Card(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(135.dp)
                        .clickable { selectedCharId = charItem.id },
                    colors = CardDefaults.cardColors(containerColor = bg),
                    border = BorderStroke(1.dp, stroke),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = charItem.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) IvoryWhite else Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NileDeepBlue.copy(alpha = 0.7f)),
            border = BorderStroke(1.dp, SaharaGold.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = activeChar.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = activeChar.color
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activeChar.description,
                    fontSize = 11.sp,
                    color = SandBeige,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CairoMidnight)
                        .border(1.dp, activeChar.color.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ThreeDObjectRenderer(
                        relicId = selectedCharId,
                        rotationX = rx,
                        rotationY = ry,
                        onDrag = { dx, dy -> viewModel.updateRotation(dx, dy) },
                        color = wireframeColors[activeColorIndex].first,
                        modifier = Modifier.fillMaxSize()
                    )

                    Text(
                        text = "زوايا الدوران: X = ${rx.toInt()}° | Y = ${ry.toInt()}°",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "👇 لوحة التحكمات والمفاتيح التفاعلية للتصميم المكيّف ✨",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaharaGold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.updateRotation(0f, -15f) },
                            colors = ButtonDefaults.buttonColors(containerColor = CairoMidnight),
                            border = BorderStroke(1.dp, SaharaGold.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f).padding(end = 4.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("دوران يمين 🔄", fontSize = 11.sp, color = SaharaGold)
                        }

                        Button(
                            onClick = { viewModel.updateRotation(0f, 15f) },
                            colors = ButtonDefaults.buttonColors(containerColor = CairoMidnight),
                            border = BorderStroke(1.dp, SaharaGold.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("دوران يسار 🔄", fontSize = 11.sp, color = SaharaGold)
                        }

                        Button(
                            onClick = { viewModel.resetRotation() },
                            colors = ButtonDefaults.buttonColors(containerColor = ClaySienna),
                            modifier = Modifier.weight(0.8f).padding(start = 4.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("تصفير 🏠", fontSize = 11.sp, color = IvoryWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("دوران تلقائي مستمر:", fontSize = 11.sp, color = SandBeige)
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = autoRotate,
                                onCheckedChange = { autoRotate = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SaharaGold,
                                    checkedTrackColor = SaharaGold.copy(alpha = 0.3f)
                                )
                            )
                        }

                        Button(
                            onClick = {
                                activeColorIndex = (activeColorIndex + 1) % wireframeColors.size
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CairoMidnight),
                            border = BorderStroke(1.dp, wireframeColors[activeColorIndex].first.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "اللون: ${wireframeColors[activeColorIndex].second}",
                                fontSize = 10.sp,
                                color = IvoryWhite
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------
// THE 4 EXPANDED LEVEL CHALLENGES COMPOSABLES
// ----------------------------------------

@Composable
fun MummyChallenge(viewModel: AdventureViewModel) {
    var isVerified by remember { mutableStateOf(false) }
    
    val rx by viewModel.rotationX.collectAsStateWithLifecycle()
    val ry by viewModel.rotationY.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "المعبد يدخلنا لسرداب الملك توت عنخ آمون! القبر يحمي قناعه الذهبي داخل تابوت مقفل بتروس 3D هندسية. لفتح التابوت وسحب القفل، يجب محاذاة زاوية دوران التابوت (Y) لتبلغ زاوية الاستقامة المحاذاة (بين ١٧٠° وَ ١٩٠°).",
            fontSize = 14.sp,
            color = IvoryWhite,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(CairoMidnight.copy(alpha = 0.5f))
                .border(1.dp, SaharaGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            ThreeDObjectRenderer(
                relicId = "mummy",
                rotationX = rx,
                rotationY = ry,
                onDrag = { dx, dy -> viewModel.updateRotation(dx, dy) },
                color = SaharaGold,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.updateRotation(0f, -45f) },
                colors = ButtonDefaults.buttonColors(containerColor = CairoMidnight),
                border = BorderStroke(1.dp, SaharaGold.copy(alpha = 0.4f))
            ) {
                Text("-45° 🔄", fontSize = 12.sp, color = SaharaGold)
            }
            Text(
                text = "الزاوية الحالية Y = ${ry.toInt()}°",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (ry >= 170f && ry <= 190f) TurquoiseNile else SaharaGold
            )
            Button(
                onClick = { viewModel.updateRotation(0f, 45f) },
                colors = ButtonDefaults.buttonColors(containerColor = CairoMidnight),
                border = BorderStroke(1.dp, SaharaGold.copy(alpha = 0.4f))
            ) {
                Text("+45° 🔄", fontSize = 12.sp, color = SaharaGold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isVerified) {
            Button(
                onClick = { isVerified = true },
                colors = ButtonDefaults.buttonColors(containerColor = SaharaGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("التحقق من المحاذاة والزوايا 🔐", color = CairoMidnight, fontWeight = FontWeight.Bold)
            }
        } else {
            val inRange = ry >= 170f && ry <= 190f
            if (inRange) {
                Text(
                    text = "فتحت البوابة الملكية! انزلق التابوت الحجري ووهبكما الحارس قناع توت عنخ آمون الذهبي ليكون حصنكما المشرق في بقية الرحلة!",
                    color = TurquoiseNile,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Button(
                    onClick = { viewModel.solveChallengeAndUnlockRelic("mummy", 90) },
                    colors = ButtonDefaults.buttonColors(containerColor = TurquoiseNile),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("سحب القناع الذهبي والانطلاق للأمام! ⚰️")
                }
            } else {
                Text(
                    text = "الرمز يرفض التجاوب مع زاوية المحاذاة الحالية! تذكر: يجب ضبط الزاوية الدائرية الترددية Y في الفضاء بين ١٧٠ درجة و ١٩٠ درجة بمحاكاة السطح الخلفي. حاول تدويرها ثانية بالأزرار أو بالسحب!",
                    color = ClaySienna,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Button(
                    onClick = { isVerified = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PharaohAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("المحاولة مرة أخرى وإعادة الضبط 🔄")
                }
            }
        }
    }
}

@Composable
fun CartoucheChallenge(viewModel: AdventureViewModel) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    val options = listOf(
        Pair(36, " الرقم ٣٦ (تربيع الستة)"),
         Pair(42, " الرقم ٤٢ (حاصل ضرب ٦ في ٧ لنمو تتابع الأعداد من كتاب الحساب البابلي والمصري القديم)"),
        Pair(40, " الرقم ٤٠ (مجموع أبعاد الأسر الفرعونية)"),
        Pair(48, " الرقم ٤٨ (ضعف الأربع وعشرين)")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "في خزانة قارون الكبرى، تقع أعين عمر وهاريسون على 'خرطوشة الفردوس الملكي'. السلسلة الحسابية المنقوشة كالآتي:\n" +
                    "٢ ، ٦ ، ١٢ ، ٢٠ ، ٣٠ ، ؟\n" +
                    "ساعد هاريسون في فك شيفرة تتابع الأعداد لمعرفة الرقم المفقود لفتح صندوق الخرطوشة الملكية (Cartouche)!",
            fontSize = 14.sp,
            color = IvoryWhite,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        options.forEachIndexed { _, pair ->
            val border = if (selectedOption == pair.first) {
                BorderStroke(1.5.dp, if (pair.first == 42) TurquoiseNile else ClaySienna)
            } else {
                BorderStroke(1.dp, SaharaGold.copy(alpha = 0.15f))
            }

            val containerColor = if (selectedOption == pair.first) {
                if (pair.first == 42) TurquoiseNile.copy(alpha = 0.1f) else ClaySienna.copy(alpha = 0.1f)
            } else {
                CairoMidnight
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(enabled = !isAnswered) { selectedOption = pair.first },
                colors = CardDefaults.cardColors(containerColor = containerColor),
                border = border,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedOption == pair.first),
                        onClick = { if (!isAnswered) selectedOption = pair.first },
                        colors = RadioButtonDefaults.colors(selectedColor = SaharaGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = pair.second, fontSize = 13.sp, color = IvoryWhite)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isAnswered) {
            Button(
                onClick = { if (selectedOption != null) isAnswered = true },
                enabled = selectedOption != null,
                colors = ButtonDefaults.buttonColors(containerColor = SaharaGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("اختبار شفرة الأواني الذهبية 📝", color = CairoMidnight, fontWeight = FontWeight.Bold)
            }
        } else {
            if (selectedOption == 42) {
                Text(
                    text = "رياضيات فرعونية مذهلة! التسلسل هو حاصل ضرب المتتاليات: 1x2=2، 2x3=6، 3x4=12، 4x5=20، 5x6=30، والتالي هو 6x7=42! انفتح صندوق قارون السحري، ووهبتكما الخزينة 'الخرطوشة الملكية' الرمز الحامي العريق لرحلة الصحراء!",
                    color = TurquoiseNile,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Button(
                    onClick = { viewModel.solveChallengeAndUnlockRelic("cartouche", 110) },
                    colors = ButtonDefaults.buttonColors(containerColor = TurquoiseNile),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("استلام تميمة خرطوشة الهدايا والتقدّم عاجلاً! 💎")
                }
            } else {
                Text(
                    text = "الرقم غير سليم وأحدث جرس تحذير بالكهف! فكر في الفرق بين الأرقام: من ٢ إلى ٦ (زيادة ٤)، من ٦ إلى ١٢ (زيادة ٦)، من ١٢ إلى ٢٠ (زيادة ٨)، من ٢٠ إلى ٣٠ (زيادة ١٠)... فكم تكون الزيادة القادمة؟ فكر وحاول مجدداً للعبور الميمون!",
                    color = ClaySienna,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Button(
                    onClick = {
                        isAnswered = false
                        selectedOption = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PharaohAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إعادة المحاولة الجبرية 🔄")
                }
            }
        }
    }
}

@Composable
fun ObeliskChallenge(viewModel: AdventureViewModel) {
    var selectedVal by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    val options = listOf(
        Pair(1, "نجم واحد متألق في السماء"),
        Pair(3, "٣ نجوم فلكية شامخة (حزام الجبار المتناسق مع الأهرامات الثلاثة بدقة كاملة)"),
         Pair(5, "٥ نجوم مضيئة تمثل الكواكب السيارة"),
        Pair(7, "٧ نجوم تمثل الغلاف الجوي")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "أمام المسلة البصرية السحرية، تضيء الأشعة بمحاذاة كوكبة الجبار الكونية في الفلك. لضبط المسلة واحتساب مسارات البث نحو الأهرامات، يسأل عمر صديقه هاريسون: ما هو عدد نجوم 'حزام كوكبة أوريون (الجبار)' الفلكية التي تتعامد عليها الأهرامات الثلاثة بدقة كاملة عكست عبقرية البناء المصري؟",
            fontSize = 14.sp,
            color = IvoryWhite,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        options.forEachIndexed { _, pair ->
            val border = if (selectedVal == pair.first) {
                BorderStroke(1.5.dp, if (pair.first == 3) TurquoiseNile else ClaySienna)
            } else {
                BorderStroke(1.dp, SaharaGold.copy(alpha = 0.15f))
            }

            val containerColor = if (selectedVal == pair.first) {
                if (pair.first == 3) TurquoiseNile.copy(alpha = 0.1f) else ClaySienna.copy(alpha = 0.1f)
            } else {
                CairoMidnight
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(enabled = !isAnswered) { selectedVal = pair.first },
                colors = CardDefaults.cardColors(containerColor = containerColor),
                border = border,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedVal == pair.first),
                        onClick = { if (!isAnswered) selectedVal = pair.first },
                        colors = RadioButtonDefaults.colors(selectedColor = SaharaGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = pair.second, fontSize = 13.sp, color = IvoryWhite)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isAnswered) {
            Button(
                onClick = { if (selectedVal != null) isAnswered = true },
                enabled = selectedVal != null,
                colors = ButtonDefaults.buttonColors(containerColor = SaharaGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("تقديم الاستدلال الفلكي العالي 🔭", color = CairoMidnight, fontWeight = FontWeight.Bold)
            }
        } else {
            if (selectedVal == 3) {
                Text(
                    text = "محاكاة وقبة فلكية باهرة! الأهرامات الثلاثة (خوفو، خفرع، منقرع) تحاكي بدقة هندسية بالغة النجوم الثلاثة النيرة لحزام كوكبة الجبار (المنطقة، النيلم، النطاق). اشتعلت المسلة بالوهج الأرجواني وسلّمتكما 'المسلة الفلكية الشامخة' لتفتح البوابة!",
                    color = TurquoiseNile,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Button(
                    onClick = { viewModel.solveChallengeAndUnlockRelic("obelisk", 120) },
                    colors = ButtonDefaults.buttonColors(containerColor = TurquoiseNile),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("الحصول على المسلة وتشييد البوصلة! 🗼")
                }
            } else {
                Text(
                    text = "هتزت المسلة وصدر صوت عطل مغناطيسي! النجوم في حزام الجبار (Orion's Belt) المتناسق هندسياً وفلكياً مع أهرامات الجيزة الثلاثة الشهيرة لها عدد فريد متناسق. فكر جيداً وحاول مجدداً للعبور الميمون!",
                    color = ClaySienna,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Button(
                    onClick = {
                        isAnswered = false
                        selectedVal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PharaohAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إعادة تفتيش مسارات النجوم 🔄")
                }
            }
        }
    }
}

@Composable
fun QuizChallenge(viewModel: AdventureViewModel) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    val options = listOf(
        Pair(1, "الملك رمسيس الثاني العظيم"),
        Pair(2, "المهندس والوزير المبدع إمحوتب (Imhotep)"),
        Pair(3, "أمنمحات الثالث باني اللابيرنت الأثري"),
        Pair(4, "سنفرو والد خوفو ومؤسس دهشور")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "بوابة مكتبة الإسكندرية المنسية محروسة بروح الحكيم حامي المخطوطات. لتدع هاريسون يعبر لأهله وتكسب التميمة الأخيرة 'بردية الحكمة'، يسألكما بصوت عميق:\n" +
                    "من هو المهندس والوزير المشيد الأسطوري للملك زوسر، الذي هندس وبنى صرح أول هرم مدرج في التاريخ الإنساني (هرم سقارة المدرّج) ونال بعبقريته منزلة إله الطب والحكمة؟",
            fontSize = 14.sp,
            color = IvoryWhite,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        options.forEachIndexed { _, pair ->
            val border = if (selectedOption == pair.first) {
                BorderStroke(1.5.dp, if (pair.first == 2) TurquoiseNile else ClaySienna)
            } else {
                BorderStroke(1.dp, SaharaGold.copy(alpha = 0.15f))
            }

            val containerColor = if (selectedOption == pair.first) {
                if (pair.first == 2) TurquoiseNile.copy(alpha = 0.1f) else ClaySienna.copy(alpha = 0.1f)
            } else {
                CairoMidnight
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(enabled = !isAnswered) { selectedOption = pair.first },
                colors = CardDefaults.cardColors(containerColor = containerColor),
                border = border,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedOption == pair.first),
                        onClick = { if (!isAnswered) selectedOption = pair.first },
                        colors = RadioButtonDefaults.colors(selectedColor = SaharaGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = pair.second, fontSize = 13.sp, color = IvoryWhite)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isAnswered) {
            Button(
                onClick = { if (selectedOption != null) isAnswered = true },
                enabled = selectedOption != null,
                colors = ButtonDefaults.buttonColors(containerColor = SaharaGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("تقديم رصيد المعلومات التاريخية 📜", color = CairoMidnight, fontWeight = FontWeight.Bold)
            }
        } else {
            if (selectedOption == 2) {
                Text(
                    text = "يا له من إجماع ثقافي فسيح وسديد! إمحوتب هو المعماري العبقري الشامخ الذي شيّد صرح سقارة المدهش، وبذكائه نال مجداً خالداً عبر العصور. سلّمكما الحارس 'بردية الحكمة المفقودة' وأضاء الممر لتنفتح البوابة الكبرى للأهرامات مع عائلة هاريسون ببهجة وعروبة متفرّدة!",
                    color = TurquoiseNile,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Button(
                    onClick = { viewModel.solveChallengeAndUnlockRelic("scroll", 150) },
                    colors = ButtonDefaults.buttonColors(containerColor = TurquoiseNile),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("الحصول على البردية وعزف نوتة الانتصار الوفير! 🏆")
                }
            } else {
                Text(
                    text = "الروح الحارسة تغمر المكان بضباب كثيف محذرة! تذكّر: هذا المهندس والعبقري صانع سقارة حظي بتقدير فاق الملوك لمهارته الطبية والمعمارية الهندسية الصادحة. حاول مجدداً مع هاريسون!",
                    color = ClaySienna,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Button(
                    onClick = {
                        isAnswered = false
                        selectedOption = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PharaohAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("المحاولة مرّة أخرى 🔄")
                }
            }
        }
    }
}
