package com.ragnar.eduapp.ui.screens.home

import android.Manifest
import android.util.Log
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ragnar.eduapp.R
import com.ragnar.eduapp.core.chatBot.ChatViewModel
import com.ragnar.eduapp.core.chatBot.ChatViewModelFactory
import com.ragnar.eduapp.ui.components.ChatMessageBubbleModel
import com.ragnar.eduapp.ui.components.ConceptMapModel
import com.ragnar.eduapp.ui.components.ExpandableTextOutput
import com.ragnar.eduapp.ui.theme.AccentBlue
import com.ragnar.eduapp.ui.theme.BackgroundPrimary
import com.ragnar.eduapp.ui.theme.BackgroundSecondary
import com.ragnar.eduapp.ui.theme.BrandPrimary
import com.ragnar.eduapp.ui.theme.ColorHint
import com.ragnar.eduapp.ui.theme.ColorSuccess
import com.ragnar.eduapp.ui.theme.SendButtonColor
import com.ragnar.eduapp.ui.theme.TextPrimary
import com.ragnar.eduapp.ui.theme.TextSecondary
import com.ragnar.eduapp.ui.theme.White
import com.ragnar.eduapp.utils.DebugLogger
import com.ragnar.eduapp.utils.SharedPreferenceUtils
import com.ragnar.eduapp.viewModels.speechModels.SpeechToText
import com.ragnar.eduapp.viewModels.speechModels.TextToSpeech
import kotlinx.coroutines.delay

@Composable
fun ChatBotScreen(
    //    navController: NavController,
    ttsController: TextToSpeech = viewModel(), // TextToSpeech core Util
    sttController: SpeechToText = viewModel(), // SpeechToText core Util
) {

    val context = LocalContext.current
    val userClass: String = SharedPreferenceUtils.getUserInfo(context, SharedPreferenceUtils.KEY_CLASS).toString()
    val nodeNumber = "10";
    val maxWord = "100"

    val chatBotController: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(
            apiKey = stringResource(R.string.gemini_api_key),
            userClass = userClass,
            nodeNumber = nodeNumber,
            maxWord = maxWord
        )
    )

    val ttsState by ttsController.state.collectAsState() // TTS states
    val sttState by sttController.state.collectAsState() // STT states

    // track current audio playback time
    var currentAudioTime by remember { mutableStateOf(0f) }

    // Collects chat state from ChatViewModel
    val chatMessages by chatBotController.messages.collectAsState()
    val isChatLoading by chatBotController.isLoading.collectAsState()

    // Add after existing state collectors
    val typingText by chatBotController.typingText.collectAsState()
    val isTyping by chatBotController.isTyping.collectAsState()
    val shouldStartTTS by chatBotController.shouldStartTTS.collectAsState()
    val fullTextForTTS by chatBotController.fullTextForTTS.collectAsState()


    // ConceptMap json output from AI
    val conceptMapResult = chatBotController.conceptMapJSON.collectAsState()

    val conceptMapJSON = conceptMapResult.value


    // Auto-scroll state for chat messages
    val chatListState = rememberLazyListState()

    var messageInput by remember { mutableStateOf("") }
    //    messageInput = sttState.resultText

    // gets the latest AI message from chat history
    val aiMessageOutput = when {
        isTyping -> typingText
        else -> chatMessages.lastOrNull { it.sender == "ai" }?.content
            ?: "Hi! I'm ready to help you learn. What would you like to work on today?"
    }



    // state for customAlertBox
    //    val showDialog by chatBotController.showPopup.collectAsState()



//    // show popUp simulation screen
//    if (showDialog){
//        WebViewAlertDialog(
//            url = "file:///android_asset/RaceStory.html",
//            showDialog = showDialog,
//            onDismiss = { chatBotController.dismissPopUp() }
//        )
//    }

//    Update audio time periodically while speaking
LaunchedEffect(ttsState.isSpeaking) {
    if (ttsState.isSpeaking) {
        while (true) {
            // Get current position from TTS controller
            currentAudioTime = ttsController.getCurrentPosition() / 1000f // ms to seconds
            delay(50) // Update every 50ms
        }
    } else {
        // Don't reset currentAudioTime to 0 when TTS finishes
        // This allows the concept map to maintain the final state
        // The ConceptMapModel will handle showing all nodes when isAudioPlaying = false
    }
}

    // Auto-start TTS when shouldStartTTS becomes true
    LaunchedEffect(shouldStartTTS) {
        if (shouldStartTTS && fullTextForTTS.isNotBlank()) {
            if (ttsState.isInitialized) {
                // Reset audio time when starting new TTS
                currentAudioTime = 0f
                ttsController.speak(fullTextForTTS)
            }
        }
    }

    // updates messageInput from STT when speech recognition completes
    LaunchedEffect(sttState.resultText) {
        if (sttState.resultText.isNotBlank()) {
            messageInput = sttState.resultText
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        sttController.handlePermissionResult(
            SpeechToText.RECORD_AUDIO_PERMISSION_REQUEST,
            if (isGranted) intArrayOf(android.content.pm.PackageManager.PERMISSION_GRANTED)
            else intArrayOf(android.content.pm.PackageManager.PERMISSION_DENIED)
        )
    }

    // Launch Activity
    LaunchedEffect(Unit) {
        sttController.initialize(context)
        ttsController.initialize(context)
        if (!sttState.hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Disposal Activity
    DisposableEffect(Unit) {
        onDispose {
            sttController.destroy()
            ttsController.cleanup()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(White)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AI Tutor Sarah",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Your personal mathematics learning companion",
                color = TextSecondary,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = when {
                    isChatLoading -> "• Thinking..."
                    isTyping -> "• Speaking..."
                    else -> "• Available to help"
                },
                color = when {
                    isChatLoading -> ColorHint
                    isTyping -> AccentBlue
                    else -> ColorSuccess
                },
                modifier = Modifier.padding(8.dp)
            )

            /*
            Card to display current result from AI
             */
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp) ,
                modifier = Modifier
                    .align(Alignment.Start)
                    .wrapContentHeight()
                    .padding(0.dp, 15.dp)

            ){
//                Spacer(modifier = Modifier.padding(15.dp))
                /**
                 * Column for send message part
                 * TextField
                 * Send Icon
                 * Mic Icon
                 */
                Column(
                    modifier = Modifier
                        .background(BackgroundPrimary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundPrimary)
                            .padding(10.dp)
                    ) {
                        // Avatar / Lip sync WebView
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                            modifier = Modifier
                                .width(120.dp)
                                .height(160.dp)
                                .padding(end = 8.dp)
                        ) {
                            AndroidView(factory = {
                                WebView(context).apply {
                                    ttsController.setupWebView(this)
                                }
                            })
                        }

                        // AI speech and message column
                        Column(
                            modifier = Modifier
                                .weight(1f) // take remaining space
                                .wrapContentHeight()
                        ) {
                            // Header row ("Sarah is saying" + button)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Outlined.SmartToy,
                                    contentDescription = "AI Icon",
                                    tint = AccentBlue
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Sarah is saying:", color = AccentBlue)
                                Spacer(Modifier.weight(1f))
                                IconButton(
                                    onClick = {
                                        if (ttsState.isInitialized) {
                                            if (!ttsState.isSpeaking) {
                                                ttsController.speak(aiMessageOutput)
                                            } else {
                                                ttsController.stop()
                                            }
                                        } else {
                                            ttsController.initialize(context)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = BrandPrimary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        if (ttsState.isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "Play Audio",
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            // Message text
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            ) {
                                ExpandableTextOutput(text = aiMessageOutput)

                                // Show typing cursor when typing
                                if (isTyping) {
                                    Text(
                                        text = "▋",
                                        color = AccentBlue,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    /**
                     * A text field send icon and mic button in a row
                     */
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // standard TextField
                        TextField(
                            value = messageInput,
                            onValueChange = { messageInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ask Sarah a question...") },
                            shape = RoundedCornerShape(20.dp),
                            singleLine = false,
                            colors = TextFieldDefaults.colors(
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedContainerColor = BackgroundSecondary,
                                focusedContainerColor = BackgroundSecondary,
                                cursorColor = ColorHint,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                unfocusedPlaceholderColor = TextSecondary,
                                focusedPlaceholderColor = TextSecondary
                            )
                        )
                        // Button to send message to chat
                        IconButton(
                            onClick = {
                                if (messageInput.isNotBlank() && !isChatLoading) {
                                    // Send message to chatbot
                                    chatBotController.sendMessage(messageInput)
                                    // Clear input
                                    messageInput = ""
                                }
                            },
                            enabled = messageInput.isNotBlank() && !isChatLoading,
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = SendButtonColor,
                                contentColor = Color.White,
                                disabledContainerColor = ColorHint,
                                disabledContentColor = Color.White
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Message"
                            )
                        }

                        // mic button
                        IconButton(
                            onClick = {
                                DebugLogger.debugLog("ChatScreen", "Mic Button Clicked")
                                if (!sttState.isSpeaking) {
                                    if (sttState.isInitialized && sttState.hasPermission) {
                                        sttController.startListening()
                                    } else if (!sttState.hasPermission){
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                } else {
                                    sttController.stopListening()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .border(1.dp, SendButtonColor, CircleShape)
                        ) {
                            Icon(
                                if (sttState.isSpeaking) Icons.Outlined.Stop else Icons.Outlined.Mic,
                                contentDescription = "Record Audio",
                                tint = SendButtonColor
                            )
                        }
                    }

                    // "Tap to send" text below the Row
                    Text(
                        text = if (isChatLoading) "Sending..." else "Tap to send",
                        style = MaterialTheme.typography.labelMedium,
                        color = ColorHint,
                        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                    )
                }
            }

            /**
             * Card to display concept map
             * this is a dynamic compose model
             */
            Card(
                modifier = Modifier
                    .height(500.dp)
//                    .padding(10.dp)
                    .background(BackgroundSecondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {

                /**
                 * Concept map model
                 * parameters:
                 * @param json: input json that contain all the node and edge relations
                 * @param currentAudioTime: input from TTS of currently playing audio
                 * @param isAudioPlaying: input from TTS if audio is playing or not
                 */
                ConceptMapModel(
                    json = conceptMapJSON,
                    currentAudioTime = currentAudioTime,
                    isAudioPlaying = ttsState.isSpeaking
                )
            }
            Spacer(modifier = Modifier.padding(10.dp))
            /*
            Card to display previous messages
             */
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier.align(Alignment.Start)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)
                ) {
                    // Header row
                    Row(
                        modifier = Modifier
                            .background(BackgroundSecondary)
                            .fillMaxWidth()
                            .padding(5.dp, 10.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Previous Conversation Icon",
                            tint = TextPrimary
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(
                            text = "Previous Conversation: ",
                            color = TextPrimary
                        )
                    }

                    // ChatMessage with auto-scroll
                    LazyColumn(
                        state = chatListState,
                        modifier = Modifier
                            .height(300.dp) // fixed height
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (chatMessages.isEmpty()) {
                            item {
                                Text(
                                    text = "No conversation yet...",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            items(chatMessages) { message ->
                                ChatMessageBubbleModel(
                                    message = message,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}