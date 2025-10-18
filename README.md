# 📚 EduApp - AI-Powered Educational Platform

> An intelligent Android application that provides personalized learning experiences through AI tutoring, interactive concept mapping, and multi-language support.

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 🌟 Overview

EduApp is a modern, AI-powered educational platform built with Jetpack Compose for Android. It leverages advanced language models (Groq LLM) to provide personalized tutoring experiences, complete with visual concept mapping, speech-to-text input, and text-to-speech output with lip-sync animations.

### 🎯 Key Highlights

- **AI-Powered Tutoring** - Conversational learning with Groq's LLaMA 4 Scout model
- **Visual Learning** - Dynamic, interactive concept maps generated from AI responses
- **Multi-Language Support** - English, Hindi, Kannada, Telugu with automatic detection
- **Voice Integration** - Speech-to-text input and text-to-speech with animated avatars
- **Personalized Learning Paths** - Adaptive content based on student pace and goals
- **NCERT Curriculum** - Complete coverage of NCERT content (Classes 1-10)

---

## ✨ Features

### 🎓 For Students

#### 📖 Personalized Learning Experience
- **Smart Onboarding** - Language selection, Google Sign-In, and academic profile setup
- **Level Assessment** - Automatically adapts to student's class and learning pace
- **Learning Intent Selection** - Choose between concept-based or exam-oriented learning
- **Study Session Setup** - Select subjects, syllabi (CBSE/NCERT), and specific chapters

#### 💬 AI Chatbot Tutor
- **Natural Conversations** - Chat naturally with AI tutor "Sarah"
- **Context-Aware Responses** - AI understands your learning context and pace
- **Multi-Modal Input** - Type or speak your questions
- **Visual Explanations** - Automatic concept map generation for every answer

#### 🗣️ Voice Features
- **Speech-to-Text** - Ask questions using voice input (supports multiple languages)
- **Text-to-Speech** - Listen to AI responses with natural voice synthesis
- **Lip Sync Animation** - Animated avatar that syncs with speech
- **Multiple Voices** - Choose between boy/girl avatar characters

#### 🎨 Interactive Concept Maps
- **Auto-Generated Visualizations** - AI creates concept maps from explanations
- **Interactive Navigation** - Zoom, pan, and drag nodes
- **Hierarchical Layout** - Clear visual representation of concept relationships
- **Color-Coded Categories** - Different colors for different concept types

### 🔧 Technical Features

#### 🏗️ Architecture
- **MVVM Pattern** - Clean separation of concerns
- **Jetpack Compose** - Modern, declarative UI
- **StateFlow** - Reactive state management
- **Navigation Component** - Type-safe navigation
- **ViewModel** - Lifecycle-aware data handling

#### 🔐 Authentication & Data
- **Google Sign-In** - Secure authentication with Credential Manager API
- **SharedPreferences** - Local user data persistence
- **Session Management** - Automatic navigation based on user state

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** - Hedgehog (2023.1.1) or later
- **Minimum SDK** - API 24 (Android 7.0)
- **Target SDK** - API 34 (Android 14)
- **Kotlin** - 1.9.0 or later
- **Groq API Key** - Required for AI chatbot functionality

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/The-RagNaR11/EduApp.git
   cd EduApp
   ```

2. **Open in Android Studio**
    - Launch Android Studio
    - Select "Open an Existing Project"
    - Navigate to the cloned directory

3. **Configure API Keys**

   Create or update `app/src/main/res/values/strings.xml`:
   ```xml
   <resources>
       <string name="app_name">EduApp</string>
       <string name="web_client_id">YOUR_GOOGLE_OAUTH_CLIENT_ID</string>
       <string name="chat_bot_api_key">YOUR_GROQ_API_KEY</string>
   </resources>
   ```

4. **Sync Gradle**
    - Click "Sync Now" in the banner that appears
    - Wait for dependencies to download

5. **Run the App**
    - Connect an Android device or start an emulator
    - Click the "Run" button (▶️) or press Shift+F10

### Getting API Keys

#### Google OAuth Client ID
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable "Google Sign-In API"
4. Create OAuth 2.0 credentials
5. Add your app's SHA-1 fingerprint
6. Copy the Web Client ID

#### Groq API Key
1. Visit [Groq Console](https://console.groq.com/)
2. Sign up for an account
3. Navigate to API Keys section
4. Generate a new API key
5. Copy the key to your `strings.xml`

---

## 📁 Project Structure

```
app/
├── src/main/
│   ├── java/com/ragnar/eduapp/
│   │   ├── MainActivity.kt                     # App entry point
│   │   │
│   │   ├── core/                              # Core functionality
│   │   │   ├── GoogleSignIn.kt                # Google authentication
│   │   │   └── chatBot/                       # AI chatbot core
│   │   │       ├── ChatViewModel.kt           # Chat state management
│   │   │       ├── ChatViewModelFactory.kt    # ViewModel factory
│   │   │       └── GroqLLMClient.kt          # Groq API client
│   │   │
│   │   ├── data/                              # Data layer
│   │   │   ├── model/                         # Data models
│   │   │   │   ├── ChatMessageModel.kt        # Chat message entity
│   │   │   │   ├── ChatMessageBubbleModel.kt  # Chat UI component
│   │   │   │   └── GoogleUserInfo.kt          # User profile data
│   │   │   └── dataClass/
│   │   │       └── GraphData.kt               # Concept map data structure
│   │   │
│   │   ├── ui/                                # UI layer
│   │   │   ├── screens/                       # App screens
│   │   │   │   ├── sign_up/                   # Onboarding flow
│   │   │   │   │   ├── LanguageSelectionScreen.kt
│   │   │   │   │   ├── GoogleSignInScreen.kt
│   │   │   │   │   ├── UserDetailEntryScreen.kt
│   │   │   │   │   └── StudentLevelAssessmentScreen.kt
│   │   │   │   └── home/                      # Main app screens
│   │   │   │       ├── StudySessionSetupScreen.kt
│   │   │   │       ├── LearningIntentScreen.kt
│   │   │   │       └── ChatBotScreen.kt       # Main chat interface
│   │   │   │
│   │   │   ├── components/                    # Reusable UI components
│   │   │   │   ├── ConceptMapModel.kt         # Interactive graph visualization
│   │   │   │   ├── ChapterSelectionModel.kt   # Multi-select chapters
│   │   │   │   ├── DropDownMenuModel.kt       # Custom dropdown
│   │   │   │   ├── IntentListViewModel.kt     # Learning intent selector
│   │   │   │   ├── LanguageOptionModel.kt     # Language radio buttons
│   │   │   │   ├── ExpandableTextOutput.kt    # Expandable text view
│   │   │   │   ├── LearnerTitleModel.kt       # Section titles
│   │   │   │   └── SignUpPageFooterModel.kt   # Footer component
│   │   │   │
│   │   │   └── theme/                         # App theming
│   │   │       ├── Color.kt                   # Color palette
│   │   │       └── Theme.kt                   # Material theme config
│   │   │
│   │   ├── viewModels/                        # View models
│   │   │   └── speechModels/                  # Speech functionality
│   │   │       ├── SpeechToText.kt           # STT controller
│   │   │       └── TextToSpeech.kt           # TTS with lip sync
│   │   │
│   │   └── utils/                             # Utility classes
│   │       ├── SharedPreferenceUtils.kt       # Local storage
│   │       └── GoogleUserInfoExtractor.kt     # User info parser
│   │
│   ├── res/
│   │   ├── values/
│   │   │   ├── strings.xml                    # String resources
│   │   │   └── colors.xml                     # Color resources
│   │   └── drawable/                          # App icons & images
│   │
│   └── assets/                                # Static assets
│       ├── LipSync.html                       # Lip sync animation
│       └── images/                            # Avatar images
│           ├── boy.png
│           └── girl.png
│
└── build.gradle                               # App dependencies
```

---

## 🏗️ Architecture & Design Patterns

### MVVM Architecture

```
┌─────────────────┐
│   View (UI)     │ ← Jetpack Compose
│  @Composable    │
└────────┬────────┘
         │ observes
         │ StateFlow
┌────────▼────────┐
│   ViewModel     │ ← Business Logic
│  (StateFlow)    │
└────────┬────────┘
         │ uses
         │
┌────────▼────────┐
│   Repository    │ ← Data Layer
│  (API/Local)    │
└─────────────────┘
```

### Key Design Patterns

1. **State Management** - Unidirectional data flow with StateFlow
2. **Factory Pattern** - ViewModelFactory for dependency injection
3. **Observer Pattern** - State observation in Composables
4. **Repository Pattern** - Data abstraction (API + SharedPreferences)
5. **Singleton Pattern** - SharedPreferenceUtils object

---

## 🔑 Core Components

### 1. Authentication Flow

```kotlin
LanguageSelection → GoogleSignIn → UserDetails → StudentAssessment
                                       ↓
                              StudySessionSetup → LearningIntent → ChatBot
```

**SharedPreferences Keys:**
- `selected_language` - User's preferred language
- `id`, `email`, `display_name`, `profile_picture_uri` - Google account info
- `phone_number`, `school_name`, `ambition` - User details
- `student_class`, `student_pace` - Learning level
- `selected_syllabus`, `selected_subject`, `chapter_list` - Study session
- `learning_intent` - Learning approach

### 2. Chat System

**ChatViewModel** manages:
- Message history (StateFlow<List<ChatMessageModel>>)
- Loading states
- Concept map JSON generation
- Error handling with retry

**Message Flow:**
```
User Input → ChatViewModel.sendMessage()
    ↓
GroqLLMClient.queryLLM()
    ↓
Extract Answer + Concept Map JSON
    ↓
Update UI States
```

### 3. Concept Map Visualization

**Features:**
- Dynamic graph layout with hierarchical positioning
- Interactive node dragging
- Zoom and pan gestures
- Color-coded categories
- Bezier curve edges with labels
- Multi-line text wrapping in nodes

**Data Structure:**
```kotlin
data class GraphData(
    val visualization_type: String,
    val main_concept: String,
    val nodes: List<Node>,
    val edges: List<Edge>
)
```

### 4. Speech Integration

**SpeechToText:**
- Android SpeechRecognizer API
- Multi-language support
- Real-time partial results
- Permission handling

**TextToSpeech:**
- Android TTS engine
- Language auto-detection
- WebView lip sync animation
- Character selection (boy/girl avatars)

---

## 🎨 UI/UX Design

### Color Palette

```kotlin
// Brand Colors
BrandPrimary = #6366F1 (Indigo)
AccentGreen = #22C55E (Emerald)
AccentBlue = #3B82F6 (Blue)

// Backgrounds
BackgroundPrimary = #F8F9FF
BackgroundSecondary = #E2E8F0
AiMessageBackground = #D7D9FF

// Text
TextPrimary = #111827
TextSecondary = #6B7280
TextOnPrimary = #FFFFFF
```

### Design Principles

1. **Material Design 3** - Modern, consistent UI
2. **Card-based Layout** - Clear content separation
3. **Color-Coded States** - Visual feedback for user actions
4. **Responsive Typography** - Scalable text sizes
5. **Accessible Contrast** - WCAG AA compliant

---

## 🔌 API Integration

### Groq LLM API

**Endpoint:** `https://api.groq.com/openai/v1/chat/completions`

**Model:** `meta-llama/llama-4-scout-17b-16e-instruct`

**Request Format:**
```json
{
  "model": "meta-llama/llama-4-scout-17b-16e-instruct",
  "messages": [
    {
      "role": "system",
      "content": "System prompt with concept map instructions..."
    },
    {
      "role": "user",
      "content": "User's question"
    }
  ],
  "temperature": 0.7,
  "max_tokens": 1024
}
```

**Response Parsing:**
1. Extract answer text from `[ANSWER]` section
2. Extract concept map JSON from `[CONCEPT_MAP_JSON]` section
3. Parse JSON into GraphData structure
4. Handle errors with fallback default data

### Google Sign-In API

**Implementation:** Credential Manager API (Modern approach)

**Flow:**
```kotlin
1. Create GetGoogleIdOption with web client ID
2. Build GetCredentialRequest
3. Call CredentialManager.getCredential()
4. Extract GoogleIdTokenCredential
5. Parse user information
6. Store in SharedPreferences
```

---

## 📱 Screen Flow

### Onboarding Journey

```
┌──────────────────┐
│ Language         │ → User selects preferred language
│ Selection        │
└────────┬─────────┘
         │
┌────────▼─────────┐
│ Google Sign-In   │ → Authenticate with Google
└────────┬─────────┘
         │
┌────────▼─────────┐
│ User Details     │ → Phone, School, Ambition
│ Entry            │
└────────┬─────────┘
         │
┌────────▼─────────┐
│ Student Level    │ → Class & Learning Pace
│ Assessment       │
└────────┬─────────┘
         │
┌────────▼─────────┐
│ Study Session    │ → Syllabus, Subject, Chapters
│ Setup            │
└────────┬─────────┘
         │
┌────────▼─────────┐
│ Learning Intent  │ → Concept/Exam-Oriented
└────────┬─────────┘
         │
┌────────▼─────────┐
│ Chat Bot         │ → AI Tutoring Session
│ Screen           │
└──────────────────┘
```

### Navigation Logic

**Automatic Route Selection:**
```kotlin
val startDestination = when {
    // Fully onboarded → Go to chat
    isUserLoggedIn && hasUserDetails && hasLearningPrefs → "chatBot"
    
    // Has account but incomplete profile → Resume setup
    isUserLoggedIn && hasUserDetails → "studentLevelAssessment"
    
    // Has account only → Collect details
    isUserLoggedIn → "userDetailEntry"
    
    // New user → Start onboarding
    else → "languageSelection"
}
```

---

## 🧪 Testing

### Manual Testing Checklist

#### Authentication
- [ ] Language selection persists
- [ ] Google Sign-In successful
- [ ] User details saved correctly
- [ ] Profile picture displayed

#### Chat Functionality
- [ ] Messages send successfully
- [ ] AI responses appear correctly
- [ ] Concept maps render properly
- [ ] Chat history maintained

#### Voice Features
- [ ] Microphone permission granted
- [ ] Speech-to-text recognizes speech
- [ ] Text-to-speech plays audio
- [ ] Lip sync animation works

#### Navigation
- [ ] Back navigation works correctly
- [ ] State persists across sessions
- [ ] Deep linking (if implemented)

### Unit Testing (Recommended)

```kotlin
// Example ViewModel test
@Test
fun `sendMessage updates state correctly`() = runTest {
    val viewModel = ChatViewModel(apiKey = "test_key")
    viewModel.sendMessage("Test question")
    
    assertTrue(viewModel.messages.value.isNotEmpty())
    assertEquals("user", viewModel.messages.value.first().sender)
}
```

---

## 🔧 Configuration

### Build Configuration

**Minimum Requirements:**
```gradle
minSdk = 24  // Android 7.0
targetSdk = 34  // Android 14
compileSdk = 34
```

**Key Dependencies:**
```gradle
// Jetpack Compose
implementation("androidx.compose.ui:ui:1.5.0")
implementation("androidx.compose.material3:material3:1.1.0")

// Credential Manager (Google Sign-In)
implementation("androidx.credentials:credentials:1.2.0")
implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")

// Networking
implementation("com.squareup.okhttp3:okhttp:4.11.0")

// Serialization
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

// Media (TTS Avatar)
implementation("androidx.media3:media3-common:1.2.0")
```

### Permissions

**AndroidManifest.xml:**
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

---

## 🚀 Deployment

### Release Build

1. **Generate Signed APK/Bundle**
   ```bash
   ./gradlew bundleRelease
   ```

2. **ProGuard Configuration**
   ```proguard
   # Keep Groq API models
   -keep class com.ragnar.eduapp.data.** { *; }
   
   # Keep Compose runtime
   -keep class androidx.compose.** { *; }
   ```

3. **Version Management**
   ```gradle
   versionCode = 1
   versionName = "1.0.0"
   ```

### Play Store Checklist

- [ ] App signing configured
- [ ] Privacy policy URL
- [ ] App description & screenshots
- [ ] Content rating completed
- [ ] Target API level 33+
- [ ] 64-bit library support

---

## 🐛 Troubleshooting

### Common Issues

**Issue: Google Sign-In fails**
- ✅ Check web_client_id in strings.xml
- ✅ Verify SHA-1 fingerprint in Google Console
- ✅ Enable Google Sign-In API

**Issue: AI responses not appearing**
- ✅ Verify Groq API key is correct
- ✅ Check internet connection
- ✅ Review Logcat for API errors

**Issue: Concept map not rendering**
- ✅ Ensure JSON is valid (check logs)
- ✅ Verify GraphData deserialization
- ✅ Check if nodes list is empty

**Issue: Speech recognition not working**
- ✅ Grant microphone permission
- ✅ Check device has speech recognition
- ✅ Verify language pack installed

**Issue: Lip sync not syncing**
- ✅ Check WebView JavaScript enabled
- ✅ Verify assets/LipSync.html exists
- ✅ Review WebView console logs

---

## 🔐 Security Best Practices

### API Key Management
- ❌ **Never** commit API keys to version control
- ✅ Use `local.properties` for sensitive data
- ✅ Implement server-side API proxy for production
- ✅ Rotate keys regularly

### Data Protection
- ✅ Encrypt sensitive data in SharedPreferences
- ✅ Use HTTPS for all network calls
- ✅ Implement certificate pinning
- ✅ Clear session data on logout

### User Privacy
- ✅ Request minimal permissions
- ✅ Provide clear privacy policy
- ✅ Allow users to delete their data
- ✅ Comply with COPPA (if targeting children)

---

## 🎯 Roadmap

### Version 1.1 (Planned)
- [ ] Offline mode with cached content
- [ ] Progress tracking and analytics
- [ ] Gamification (badges, streaks)
- [ ] Parent dashboard

### Version 1.2 (Planned)
- [ ] Video lessons integration
- [ ] Practice quizzes and tests
- [ ] Peer learning groups
- [ ] Teacher connect feature

### Version 2.0 (Future)
- [ ] AR/VR learning experiences
- [ ] Advanced AI tutor with memory
- [ ] Multi-subject support
- [ ] Certification programs

---

## 🤝 Contributing

We welcome contributions! Here's how to get started:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Make your changes**
4. **Follow coding standards**
    - Use Kotlin conventions
    - Add KDoc comments
    - Write unit tests
5. **Commit with clear messages**
   ```bash
   git commit -m "Add: Amazing feature description"
   ```
6. **Push and create Pull Request**

### Coding Standards
- Follow [Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Keep functions small and focused
- Write self-documenting code
- Add comments for complex logic

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2025 The-RagNaR11

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

## 👨‍💻 Author

**Shubhash Singh**
- GitHub: [@shubhash-singh](https://github.com/shubhash-singh)
- Project Link: [https://github.com/The-RagNaR11/EduApp](https://github.com/The-RagNaR11/EduApp)

---

## 🙏 Acknowledgments

- **Groq** - For providing fast LLM inference
- **Google** - For Authentication APIs
- **Jetpack Compose** - For modern Android UI
- **Android Open Source Project** - For TTS/STT APIs
- **Material Design** - For design guidelines

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/The-RagNaR11/EduApp/issues)
- **Discussions**: [GitHub Discussions](https://github.com/The-RagNaR11/EduApp/discussions)
- **Email**: your.email@example.com

---

## 📊 Project Stats

![GitHub repo size](https://img.shields.io/github/repo-size/The-RagNaR11/EduApp)
![GitHub language count](https://img.shields.io/github/languages/count/The-RagNaR11/EduApp)
![GitHub top language](https://img.shields.io/github/languages/top/The-RagNaR11/EduApp)
![GitHub last commit](https://img.shields.io/github/last-commit/The-RagNaR11/EduApp)

---

<div align="center">

**Made with ❤️ for Students**

[⬆ Back to Top](#-eduapp---ai-powered-educational-platform)

</div>