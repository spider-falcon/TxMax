package org.weresomeware.tx.txmax

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import assistant.ActionExecutor
import assistant.models.Actions
import assistant.QuickResult
import assistant.RuleEngine
import assistant.SearchResult
import assistant.SpeechActions
import assistant.ResponseManager
import assistant.utils.AppRegistryManager
import coil.compose.AsyncImage
import database.HistoryDatabase
import kotlinx.coroutines.launch
import org.weresomeware.tx.txmax.ui.theme.TxMaxTheme

class AssistantActivity : ComponentActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechActions: SpeechActions
    private val ruleEngine = RuleEngine()
    private lateinit var actionExecutor: ActionExecutor
    private lateinit var historyDb: HistoryDatabase
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            AppRegistryManager.syncInstalledApps(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechActions = SpeechActions(this)
        actionExecutor = ActionExecutor(this)
        historyDb = HistoryDatabase(this)
        sharedPreferences = getSharedPreferences("tx_max_settings", Context.MODE_PRIVATE)

        setContent {
            TxMaxTheme {
                val context = LocalContext.current
                val activity = LocalActivity.current
                val keyboardController = LocalSoftwareKeyboardController.current
                val focusManager = LocalFocusManager.current
                val coroutineScope = rememberCoroutineScope()

                var inputText by remember { mutableStateOf("") }
                var isListening by remember { mutableStateOf(false) }
                var assistantReply by remember { mutableStateOf<SearchResult?>(null) }
                val scrollState = rememberScrollState()

                var dynamicDarkMode by remember {
                    mutableStateOf(sharedPreferences.getBoolean("dark_mode", false))
                }

                DisposableEffect(sharedPreferences) {
                    val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                        if (key == "dark_mode") {
                            dynamicDarkMode = prefs.getBoolean("dark_mode", false)
                        }
                    }
                    sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
                    onDispose {
                        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
                    }
                }

                val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
                val micScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isListening) 1.25f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(700, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "micScaleAnimation"
                )

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
                    if (!audioGranted) {
                        speechActions.speak("Microphone permission denied")
                    }
                }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (dynamicDarkMode) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.35f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            activity?.finish()
                        }
                ) {
                    SmallFloatingActionButton(
                        onClick = { context.startActivity(Intent(context, ChatHistoryActivity::class.java)) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(top = 16.dp, end = 20.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {},
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        AnimatedVisibility(
                            visible = assistantReply != null,
                            enter = slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(400, easing = EaseOutExpo)) + fadeIn(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(28.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                tonalElevation = 8.dp,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp)
                                    .animateContentSize(animationSpec = tween(300))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .verticalScroll(scrollState)
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Assistant",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "TX Max",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    if (assistantReply?.text == "Searching the web...") {
                                        LinearProgressIndicator(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    }

                                    if (assistantReply?.imageUrl != null) {
                                        AsyncImage(
                                            model = assistantReply?.imageUrl,
                                            contentDescription = "Search Image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                        )
                                    }

                                    Text(
                                        text = assistantReply?.text ?: "",
                                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    assistantReply?.sourceUrl?.let { url ->
                                        OutlinedButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Read More")
                                        }
                                    }
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(32.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                            tonalElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 8.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = {
                                        Text(
                                            if (isListening) "Listening..." else "Ask me anything...",
                                            color = if (isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            fontWeight = if (isListening) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    singleLine = false,
                                    maxLines = 4,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                inputText.isNotBlank() -> MaterialTheme.colorScheme.primary
                                                isListening -> MaterialTheme.colorScheme.errorContainer
                                                else -> MaterialTheme.colorScheme.secondaryContainer
                                            }
                                        )
                                        .clickable {
                                            if (inputText.isNotBlank()) {
                                                keyboardController?.hide()
                                                focusManager.clearFocus()

                                                processCommand(inputText) { reply ->
                                                    assistantReply = reply
                                                    coroutineScope.launch { scrollState.animateScrollTo(0) }
                                                }
                                                inputText = ""
                                            } else if (!isListening) {
                                                isListening = true
                                                assistantReply = null
                                                startListening(
                                                    onResult = { result ->
                                                        isListening = false
                                                        processCommand(result) { reply ->
                                                            assistantReply = reply
                                                            coroutineScope.launch { scrollState.animateScrollTo(0) }
                                                        }
                                                    },
                                                    onError = { errorMsg ->
                                                        isListening = false
                                                        assistantReply = SearchResult(text = errorMsg)
                                                        speechActions.speak(errorMsg)
                                                    }
                                                )
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (inputText.isNotBlank()) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Send",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "🎤",
                                            color = if (isListening) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier
                                                .padding(bottom = 2.dp)
                                                .scale(micScale)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun processCommand(command: String, onReplyGenerated: (SearchResult) -> Unit) {
        val profilePrefs = getSharedPreferences("user_profile_db", Context.MODE_PRIVATE)
        val dynamicUserName = profilePrefs.getString("name", "MEVIN M.") ?: "MEVIN M."

        historyDb.insertMessage(
            sender = dynamicUserName,
            message = command,
            eventTag = "User Command"
        )

        val assistantIntent = ruleEngine.process(command)

        lifecycleScope.launch {
            // Check if it's a web search action first to prevent skipping early callbacks
            val resultObject = if (assistantIntent.action == Actions.SEARCH_WEB) {
                onReplyGenerated(SearchResult("Searching the web..."))
                val query = assistantIntent.value ?: ""
                val answer = QuickResult.fetchAnswer(query)

                if (answer.text.contains("I couldn't find a short answer")) {
                    actionExecutor.execute(assistantIntent, ruleEngine.sessionState)
                }
                answer
            } else {
                // Delegate all other responses to the dynamic multi-variant manager file
                ResponseManager.generateResponse(
                    action = assistantIntent.action,
                    value = assistantIntent.value,
                    isMediaPlaying = ruleEngine.sessionState.isMediaPlaying,
                    isFlashlightOn = ruleEngine.sessionState.isFlashlightOn
                )
            }

            speechActions.speak(resultObject.text)
            onReplyGenerated(resultObject)

            historyDb.insertMessage(
                sender = "TX Max",
                message = resultObject.text,
                eventTag = assistantIntent.action ?: "General"
            )

            if (assistantIntent.action != Actions.SEARCH_WEB || resultObject.text.contains("open the web")) {
                actionExecutor.execute(assistantIntent, ruleEngine.sessionState)
            }
        }
    }

    private fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

            val forceOffline = sharedPreferences.getBoolean("offline_voice_first", false)
            if (forceOffline) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!text.isNullOrBlank()) {
                    onResult(text)
                } else {
                    onError("No speech detected")
                }
            }
            override fun onError(error: Int) {
                val errorMsg = when(error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error"
                }
                onError(errorMsg)
            }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        speechRecognizer.startListening(intent)
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        speechActions.shutdown()
        super.onDestroy()
    }
}