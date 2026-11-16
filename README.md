# AI Career Pilot

**Developer:** Yash Chaudhari

AI Career Pilot is an Android app that compares your resume with a job description using **Google Gemini AI**. It gives you an ATS-style match score, the skills you match and miss, and suggestions to improve your resume. Your analysis history works **offline first**: it is stored on the device in Room and backed up in the background to **Cloud Firestore**, so it follows your account to any device. The app also shows trending developer topics from the Stack Overflow API, and those work offline too.

Built with **Kotlin, Jetpack Compose, Clean Architecture + MVVM, Hilt, Room, WorkManager, Firebase Auth, Cloud Firestore, Retrofit, Gemini AI, CameraX and ML Kit**.

You can also **scan a paper resume with the camera**: text is read on the device with ML Kit, so the photos never leave the phone.

---

## Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/7c9f8ec6-01db-4992-83d8-7af6833ab113" width="240" alt="Home Screen"/>
  &nbsp;&nbsp;
  <img src="https://github.com/user-attachments/assets/fe9db0ee-59d5-499f-9edb-403ca63474f1" width="240" alt="History Screen"/>
  &nbsp;&nbsp;
  <img src="https://github.com/user-attachments/assets/64245233-7f81-4137-a8c3-7a1ffede52d8" width="240" alt="Detail Screen"/>
</p>

---

## Table of Contents

1. [Features at a glance](#features-at-a-glance)
2. [Architecture](#architecture)
3. [Feature deep dive: how each feature is implemented](#feature-deep-dive-how-each-feature-is-implemented)
   - [Authentication](#1-authentication-firebase-auth)
   - [Resume upload and text extraction](#2-resume-upload-and-text-extraction-pdf--docx)
   - [Share-to-app](#3-share-to-app-receive-resumes-from-other-apps)
   - [AI resume analysis](#4-ai-resume-analysis-gemini--keyword-engine)
   - [Daily usage limit](#5-daily-usage-limit-firestore)
   - [Analysis history](#6-analysis-history-room)
   - [Offline-first cloud sync](#7-offline-first-cloud-sync-room--firestore--workmanager)
   - [Developer trends (offline cache)](#8-developer-trends-with-offline-cache-retrofit--room)
   - [Adaptive UI](#9-adaptive-ui-phones--tablets)
   - [Splash screen, theme and animations](#10-splash-screen-theme-and-animations)
   - [Release build optimisation](#11-release-build-optimisation-r8)
   - [Testing](#12-testing)
   - [Scan a paper resume](#13-scan-a-paper-resume-camerax--ml-kit-ocr)
   - [Structured report UI](#14-structured-report-ui)
   - [Light and dark mode](#15-light-and-dark-mode)
4. [Database schema](#database-schema)
5. [Firestore data model and security rules](#firestore-data-model-and-security-rules)
6. [Project structure](#project-structure)
7. [Tech stack](#tech-stack)
8. [Getting started](#getting-started)

---

## Features at a glance

| Feature | What the user gets | Key tech |
|---|---|---|
| Email login / sign-up | Secure, per-user account | Firebase Auth |
| Resume upload | Pick a **PDF or DOCX** from storage | Storage Access Framework, PDFBox, ZIP/XML parsing |
| Share-to-app | Share a resume from Gmail, Drive, WhatsApp and similar apps straight into the app | `ACTION_SEND` intent filter |
| Scan paper resume | Photograph one or more pages of a printed resume and analyse it | CameraX, ML Kit Text Recognition (on-device) |
| AI analysis | Match score, matched skills, missing keywords, improvements, ATS tips, verdict | Gemini 2.5 Flash |
| Hybrid scoring | Final score = 80% AI score + 20% keyword-density score | Custom keyword engine (~250 skills) |
| Offline fallback | Still returns a keyword-based report when AI or the network fails | Error mapping in the repository |
| Daily limit | 3 analyses per user per day, enforced on the server side | Cloud Firestore |
| History | All past analyses with score, date and a full Markdown report | Room + Flow |
| **Cloud backup and multi-device restore** | History syncs to the user's account and is restored on login on any device | **WorkManager + Firestore** |
| **Sync status** | "Backed up / Syncing… / N waiting / Failed · tap to retry" | WorkManager `WorkInfo` + Room `Flow` |
| Developer trends | Hot Stack Overflow questions with views, score, answers and tags | Retrofit + Gson |
| **Offline trends** | Trends open instantly from cache and show an offline banner when there's no network | Room cache + periodic worker |
| Adaptive layout | List on phones, 2-column grid on tablets | Window size detection |
| Polished UX | Splash screen, Lottie animations, dark theme, Material 3 | SplashScreen API, Lottie |

---

## Architecture

The app follows **Clean Architecture + MVVM** with a strict one-way dependency flow:
`presentation → domain ← data`.

```text
┌──────────────────────────── PRESENTATION ────────────────────────────┐
│  Compose Screens  ──observe StateFlow──▶  ViewModels (Hilt)          │
└──────────────────────────────────┬───────────────────────────────────┘
                                   │ calls
┌──────────────────────────────── DOMAIN ──────────────────────────────┐
│  Use Cases  ──────────▶  Repository Interfaces (pure Kotlin)         │
└──────────────────────────────────┬───────────────────────────────────┘
                                   │ implemented by
┌───────────────────────────────── DATA ───────────────────────────────┐
│  Repository Impls ──▶ Room (source of truth)                         │
│        │          ──▶ Gemini AI       ──▶ Retrofit (Stack Overflow)  │
│        │          ──▶ Firebase Auth   ──▶ Cloud Firestore            │
│        └──▶ WorkManager workers (background sync / refresh)          │
└──────────────────────────────────────────────────────────────────────┘
```

**Principles used**
- **Single source of truth:** the UI only reads from Room. The network and the cloud only *update* Room.
- **Unidirectional data flow:** ViewModels expose `StateFlow` / sealed `UiState`, and screens send events back up.
- **Dependency injection:** Hilt provides every dependency, including injected `@HiltWorker` background workers.
- **Testability:** business rules (use cases, sync conflict resolution) are plain Kotlin and covered by unit tests.

---

## Feature deep dive: how each feature is implemented

### 1. Authentication (Firebase Auth)

**Files:** `domain/repository/AuthRepository.kt`, `data/repository/FirebaseAuthRepository.kt`, `presentation/viewmodel/AuthViewModel.kt`, `SignInScreen.kt`, `SignUpScreen.kt`

- `AuthRepository` (domain interface) defines `loginWithEmail`, `registerWithEmail`, `logout` and `isUserLoggedIn`.
- `FirebaseAuthRepository` implements it with `FirebaseAuth`, turning the Firebase `Task` API into coroutines with `await()`, and returns `Result<String>` (the user's UID) so errors never crash the UI.
- `AuthViewModel` exposes a sealed `AuthUiState` (`Idle / Loading / Success / Error`) and an `isLoggedIn` `StateFlow` that decides the start destination in navigation.
- It guards against double taps: a new request is ignored while `Loading`.
- **After login or sign-up** it triggers a cloud sync, so the user's history is restored onto the device.
- **On logout** it cancels pending sync work and clears the local database, so the next account on the same phone never sees the previous user's data.

### 2. Resume upload and text extraction (PDF / DOCX)

**Files:** `util/PdfHelper.kt` (`DocumentHelper`), `util/FileUtils.kt`, `HomeScreen.kt`

- The user picks a file with `ActivityResultContracts.OpenDocument()` (Storage Access Framework), so **no storage permission** is needed.
- `getFileName()` reads the display name through `ContentResolver` and `OpenableColumns`.
- `DocumentHelper.extractTextFromUri()` runs on `Dispatchers.IO` and chooses a parser by extension:
  - **PDF:** `PDFBox-Android` (`PDDocument` + `PDFTextStripper`).
  - **DOCX:** a lightweight custom parser. A `.docx` is a ZIP archive, so the app streams it with `ZipInputStream`, finds `word/document.xml` and pulls the text out of the `<w:t>` nodes. This replaced Apache POI, cutting several MB from the APK.
- Unsupported files and files with no extractable text produce a clear error state.

### 3. Share-to-app (receive resumes from other apps)

**Files:** `AndroidManifest.xml`, `MainActivity.kt`, `ResumeViewModel.kt`

- `MainActivity` declares an `ACTION_SEND` intent filter for `application/pdf` and the DOCX MIME type, so the app appears in the system **Share sheet**.
- `handleShareIntent()` runs in both `onCreate` and `onNewIntent` (the activity uses `launchMode="singleTop"`). It reads `EXTRA_STREAM` with `IntentCompat.getParcelableExtra`, so it works on old and new APIs.
- The shared file is passed to `ResumeViewModel.onResumeShared()`, and the Home screen asks the user to analyse it or dismiss it.

### 4. AI resume analysis (Gemini + keyword engine)

**Files:** `data/repository/ResumeRepositoryImpl.kt`, `domain/usecases/AnalyzeResumeUseCase.kt`, `ResumeViewModel.kt`, `di/AppModule.kt`

**Flow** (`ResumeViewModel.processResume`):
1. Validate the file type (PDF/DOCX).
2. Check the daily limit (`CanAnalyzeUseCase`).
3. Extract the text (`DocumentHelper`).
4. Analyse it (`AnalyzeResumeUseCase` → `ResumeRepository.analyzeWithJD`).
5. Increment usage and refresh the remaining-attempts count.
6. Emit `ResumeUiState.Success` or `Error`.

**Inside the repository:**
- **Input safety:** the resume is capped at 12,000 characters and the JD at 4,000, to control token cost and latency.
- **Keyword engine:** `extractKeywords()` scans the JD against a dictionary of about 250 skills across Android, iOS, web, backend, databases, cloud/DevOps, AI/ML, security, QA, design, PM, marketing, finance, HR, sales, healthcare and education. Keyword match density = matched ÷ required × 100.
- **Structured prompt engineering:** Gemini (`gemini-2.5-flash`, provided as a Hilt singleton) is told to act as a senior ATS recruiter and return a strict plain-text format: `MATCH_SCORE`, `MATCH_SUMMARY`, `MATCHED_SKILLS`, `MISSING_KEYWORDS`, `THINGS_TO_IMPROVE`, `ATS_OPTIMIZATION_TIPS`, `FINAL_VERDICT`.
- **Robust parsing:** a case-insensitive regex (`MATCH_SCORE:\s*(\d+)`) pulls out the AI score and clamps it to 0–100.
- **Hybrid score:** `final = 0.8 × AI score + 0.2 × keyword score`. If the AI score is missing, the keyword score is used.
- **Graceful degradation:** network, timeout, 503, 429 and API-key errors are mapped to friendly messages, and a **keyword-only offline report** is still produced and saved, so the user always gets a result.
- Every result is saved to Room as an `AnalysisRecord` and queued for cloud sync.

### 5. Daily usage limit (Firestore)

**Files:** `data/repository/UsageRepositoryImpl.kt`, `CanAnalyzeUseCase`, `IncrementUsageUseCase`, `GetRemainingAttemptsUseCase`

- Each user has a document `users/{uid}` with `usageCount` and `lastResetDate`.
- **Daily reset without a server job:** if `lastResetDate` is not today, the counter is treated as 0 and reset with `set(..., SetOptions.merge())`. Otherwise it is incremented atomically with `FieldValue.increment(1)`.
- The limit (3 per day) lives in the cloud, so reinstalling the app or clearing its data does not reset it.
- The Home screen shows "Remaining today: X / 3".

### 6. Analysis history (Room)

**Files:** `data/local/AnalysisDao.kt`, `data/local/AppDatabase.kt`, `HistoryScreen.kt`, `HistoryDetailScreen.kt`, `GetHistoryUseCase`, `DeleteRecordUseCase`

- `AnalysisDao.getAllRecords()` returns a `Flow<List<AnalysisRecord>>` (newest first, tombstoned rows hidden). The ViewModel turns it into a `StateFlow` with `stateIn(WhileSubscribed(5000))`, so it survives configuration changes and stops collecting when not in use.
- The **History** list shows file name, colour-coded score and date. Deleting asks for confirmation in a dialog first.
- **History Detail** (`history_detail/{recordId}` route) shows the score, a match-score progress bar and the full report rendered as **Markdown** (`multiplatform-markdown-renderer-m3`).
- **Delete is a soft delete:** the row is marked `isDeleted` and disappears instantly, and the sync worker then removes it from the cloud.

### 7. Offline-first cloud sync (Room + Firestore + WorkManager)

This is the main architecture feature: history works fully offline, is backed up automatically, and is **restored on any device** where the user logs in.

**Files:**

| Layer | File | Responsibility |
|---|---|---|
| domain | `domain/repository/SyncRepository.kt` | `requestSync()`, `schedulePeriodicSync()`, `observeSyncState()`, `clearLocalData()`, plus a sealed `SyncState` |
| domain | `domain/usecases/SyncHistoryUseCase.kt`, `ObserveSyncStateUseCase.kt` | Use cases the UI depends on |
| data | `data/repository/SyncRepositoryImpl.kt` | Schedules WorkManager jobs and maps `WorkInfo` + pending count to `SyncState` |
| data | `data/sync/HistorySyncEngine.kt` | The actual two-way sync algorithm |
| data | `data/sync/SyncConflictResolver.kt` | Pure, unit-tested last-write-wins merge rules |
| data | `data/sync/Workers.kt` | `HistorySyncWorker`, `QuestionsRefreshWorker` (`@HiltWorker`) |
| app | `AICareerPilotApp.kt` | Implements `Configuration.Provider` with `HiltWorkerFactory` and schedules the periodic sync |

**Sync metadata on every record** (`AnalysisRecord`):
- `remoteId`: a UUID shared across devices, used as the Firestore document id
- `updatedAt`: when the record last changed; drives conflict resolution
- `syncStatus`: `PENDING` / `SYNCED`
- `isDeleted`: tombstone for deletions that still need to reach the cloud

**Write path (works offline):**
```text
User analyses / deletes
      │
      ▼
Room write (syncStatus = PENDING) ──▶ UI updates instantly via Flow
      │
      ▼
SyncRepository.requestSync() ──▶ WorkManager unique work (needs network)
```

**Sync algorithm** (`HistorySyncEngine.sync()`):
1. **Push:** every `PENDING` row is written to `users/{uid}/analyses/{remoteId}`. Tombstones are uploaded with `isDeleted = true` (so other devices learn about the deletion) and then hard-deleted locally.
2. **Safe acknowledgement:** a row is marked `SYNCED` with `UPDATE … WHERE remoteId = ? AND updatedAt = ?`. If the user edited the row while the upload was in progress, it stays `PENDING`, so no edit is ever lost.
3. **Incremental pull:** it fetches only documents where `updatedAt > lastPull`. The cursor is stored per user in SharedPreferences, and a fresh login uses 0, which downloads the full history.
4. **Conflict resolution:** `SyncConflictResolver` applies **last-write-wins**:
   - remote is new → insert it
   - remote is newer → overwrite the local row, keeping the local primary key so navigation routes stay valid
   - remote tombstone is newer → delete locally
   - local is newer or equal → keep local, which will be pushed
5. **Bounded network calls:** Firestore's `await()` waits forever while offline, so every call is wrapped in `withTimeout(30s)` and the worker can fail and retry.

**WorkManager setup:**
- One-time sync: `NetworkType.CONNECTED` constraint, **exponential backoff** (starting at 30 s), up to 5 attempts, and the `APPEND_OR_REPLACE` policy, so a sync requested during a running sync is queued instead of lost.
- Periodic sync: every **6 hours** (`ExistingPeriodicWorkPolicy.KEEP`).
- Triggers: after each analysis or deletion, on app start (flushes anything queued offline), and after login (restores history).
- Hilt integration: `@HiltWorker` + `@AssistedInject`. The default WorkManager initializer is removed in the manifest (`tools:node="remove"`) so the app's `HiltWorkerFactory` is used.

**Sync status in the UI:** `observeSyncState()` combines `getWorkInfosByTagFlow()` with Room's `observePendingCount()` into:
- `Synced`: "Backed up to cloud"
- `Syncing`: spinner
- `Pending(n)`: "n waiting to sync" (tap to sync now)
- `Failed(n)`: "Sync failed · tap to retry"

The History screen shows this as a status bar, and shows "Restoring your history…" while the first download after login is running.

**Privacy:** data is stored under the user's UID, Firestore security rules only allow `request.auth.uid == userId`, and logout wipes local data.

### 8. Developer trends with offline cache (Retrofit + Room)

**Files:** `data/remote/NewsApi.kt` (`StackOverflowApi`), `di/StackOverflowModule.kt`, `data/repository/StackOverflowRepository.kt`, `data/local/QuestionDao.kt`, `presentation/viewmodel/NewsViewModel.kt` (`DiscussionViewModel`), `JobMarketScreen.kt`

- Retrofit + Gson call `GET /2.3/questions?order=desc&sort=hot&site=stackoverflow&pagesize=50`.
- **Offline-first:** the repository exposes `observeQuestions()` from Room (`cached_questions` table). `refresh()` downloads fresh data and swaps the cache **atomically** inside a Room `@Transaction` (`replaceAll`), so the list never flashes empty.
- `DiscussionViewModel` **combines** the cached `Flow` with the refresh status:
  - cache + network OK → list
  - cache + network failed → list + **"Offline – showing saved trends. Tap to retry"** banner
  - no cache + network failed → error screen with a **Retry** button
- `QuestionsRefreshWorker` refreshes the cache every **12 hours** in the background (needs network and a battery that isn't low), so the screen opens instantly with recent data.
- Each card shows title, views, score, answer count and up to 4 tags, and opens the discussion in the browser.

### 9. Adaptive UI (phones and tablets)

**Files:** `util/DeviceUtils.kt`, `HistoryScreen.kt`, `MainScreen.kt`

- `getDeviceType()` classifies the device as `PHONE` or `TABLET` from the screen width.
- History uses a `LazyColumn` on phones and a 2-column `LazyVerticalGrid` on tablets.
- `MainScreen` uses a Material 3 `NavigationBar` with animated (scaled) selected items, and Navigation Compose routes: Home, History, Developer Trends, Profile, Sign In, Sign Up and History Detail.
- The app supports edge-to-edge display and every screen orientation (`fullSensor`).

### 10. Splash screen, theme and animations

**Animated splash (`presentation/screens/AnimatedSplashScreen.kt`).** The system splash is made invisible: a transparent icon (`drawable/splash_empty.xml`) on a background colour set per theme (`values/` and `values-night/splash_colors.xml`). So the first thing users see is a Compose animation of about 2.8 seconds that tells the app's story:

1. The app tile springs in.
2. The resume outline draws itself (`PathMeasure.getSegment`).
3. The avatar and text lines appear one by one.
4. An AI scan line sweeps the page, and a magnifier pops in with a spring.
5. The "AI Career Pilot" letters rise in one after another, the tagline fades in, and a thin progress bar fills.
6. The screen zooms slightly and fades out into `MainScreen`.

It follows the light or dark theme and plays only on a fresh launch (`rememberSaveable`).

- **Android 12+ SplashScreen API** (`core-splashscreen`, `installSplashScreen()`) with a custom splash theme.
- A **Material 3** dark theme with custom colour and typography (`presentation/theme`).
- **Lottie** animations for a livelier loading and empty experience.
- Gradient and radial-glow backgrounds drawn with `Modifier.drawBehind`.

### 11. Release build optimisation (R8)

**File:** `app/proguard-rules.pro`

- `isMinifyEnabled = true` and `isShrinkResources = true` for release builds.
- Avoids blanket `-keep class lib.** { *; }` rules. It relies on the libraries' own consumer rules and keeps only what reflection needs (Gson `@SerializedName` fields and Retrofit service interfaces), which maximises shrinking and obfuscation.
- Keeps line numbers so crash stack traces can be de-obfuscated.
- Firestore mapping uses explicit `Map`s rather than reflection, so the sync code needs no extra keep rules.
- API keys come from `local.properties` through `BuildConfig` fields and are never committed.

### 12. Testing

**Location:** `app/src/test/`

| Test | What it verifies |
|---|---|
| `AnalyzeResumeUseCaseTest` | Use case delegates to the repository and returns its result |
| `CanAnalyzeUseCaseTest` | Daily-limit allow/deny behaviour |
| `GetRemainingAttemptsUseCaseTest` | Remaining-attempts calculation |
| `SyncConflictResolverTest` | Last-write-wins rules: new remote inserted, unknown tombstone ignored, newer remote overwrites and keeps the PK, newer local wins, newer tombstone deletes |

Tools: **JUnit4, MockK, kotlinx-coroutines-test**.

```bash
./gradlew testDebugUnitTest
```

### 13. Scan a paper resume (CameraX + ML Kit OCR)

Many people still carry a printed resume. The **Scan paper resume** button on Home opens a full-screen camera, lets the user capture one or more pages, reads the text **on the device**, and then runs the same Gemini analysis as a PDF upload.

**Flow through the layers**

```text
HomeScreen ──▶ ResumeScannerScreen (CameraX)        presentation / UI
                 │ page photos saved to cache/scans/
                 ▼
ResumeViewModel.processScannedResume(paths)         presentation / state
                 │
                 ▼
ScanResumeUseCase                                   domain
                 │
                 ▼
TextRecognitionRepository (interface)               domain
                 ▲ @Binds in AppModule
MlKitTextRecognitionRepository (ML Kit)             data
                 │ text
                 ▼
same pipeline as uploads: daily limit ▶ Gemini ▶ Room history ▶ cloud sync
```

**How it is implemented**

| Piece | File | What it does |
|---|---|---|
| Camera UI | `presentation/screens/ResumeScannerScreen.kt` | Runtime `CAMERA` permission with a rationale screen; `PreviewView` inside Compose via `AndroidView`; `Preview` + `ImageCapture` (`CAPTURE_MODE_MAXIMIZE_QUALITY` for sharper text) bound to the lifecycle with `ProcessCameraProvider` in a `DisposableEffect` (unbound on dispose); an A4-ratio guide frame; multi-page capture into `cacheDir/scans/`. Shown as a full-screen `Dialog`, so the navigation graph is unchanged. |
| Domain contract | `domain/repository/TextRecognitionRepository.kt` | `suspend fun recognizeText(imagePaths: List<String>): String`. It uses only paths and strings, so the domain layer has no CameraX or ML Kit imports. |
| Use case | `domain/usecases/ScanResumeUseCase.kt` | Calls the repository and trims the result. |
| OCR implementation | `data/ocr/MlKitTextRecognitionRepository.kt` | ML Kit Latin text recognizer (bundled model, works **offline**). `InputImage.fromFilePath` respects EXIF rotation. The ML Kit `Task` is wrapped in `suspendCancellableCoroutine` and runs on `Dispatchers.Default`. Pages are joined in order, keeping ML Kit's top-to-bottom block order. |
| DI | `di/AppModule.kt` | `@Binds` from `MlKitTextRecognitionRepository` to `TextRecognitionRepository`. |
| ViewModel | `presentation/viewmodel/ResumeViewModel.kt` | `processScannedResume(paths)` and `processResume(uri, name)` both use one private `runAnalysis(...)` pipeline (limit check → extract text → Gemini → usage count). The temporary photos are deleted in a `finally` block once OCR finishes, and they are also deleted if the user closes the scanner. |

**Privacy:** images stay on the device and are deleted after OCR. Only the recognised text is sent to Gemini, the same as with a PDF.

**Permissions:** `android.permission.CAMERA`, plus `<uses-feature android:name="android.hardware.camera.any" android:required="false"/>`, so devices without a camera can still install the app.

**Dependencies** (version catalog): `androidx.camera:camera-core / camera-camera2 / camera-lifecycle / camera-view` and `com.google.mlkit:text-recognition`.

**Tips for good results:** use even lighting, keep the page flat, and fill the guide frame with one page per shot.

### 14. Structured report UI

Gemini answers in a fixed plain-text format (`MATCH_SCORE:`, `MATCH_SUMMARY:`, `MATCHED_SKILLS:`, `MISSING_KEYWORDS:`, `THINGS_TO_IMPROVE:`, `ATS_OPTIMIZATION_TIPS:`, `FINAL_VERDICT:`). Showing that text directly produced one hard-to-read block, so the report screen now parses it into real sections.

- **`domain/model/AnalysisReport.kt`** holds the `AnalysisReport` data class and `AnalysisReportParser`, which is pure Kotlin with no Android code. The parser strips markdown (`#`, `**`), detects section headers, handles different bullet styles (`-`, `*`, `•`, `1.`), splits inline comma lists, and reads the keyword header and the offline `SYSTEM_STATUS` report. Covered by `AnalysisReportParserTest`.
- **`HistoryDetailScreen.kt`** shows:
  - a hero card with an animated ATS score ring, a "Strong / Fair / Needs work" pill, and AI-match and keyword stat tiles;
  - a highlighted **final verdict** card;
  - a **summary** as a bullet list;
  - **skills** as chip groups: matched skills in sage, missing ones in coral;
  - **improvements** as a numbered list;
  - **ATS tips** as icon rows;
  - an offline banner when only the keyword report was available.
- If a record has no recognisable sections (very old data), the screen falls back to rendering the text as Markdown, so nothing is lost.
- **Palette** (`theme/Color.kt` → `ReportColors`): graphite surfaces, one muted lavender accent, and desaturated status colours (sage, sand, coral). There are no neon colours, and spacing uses large radii (24–28 dp) with hairline borders.

### 15. Light and dark mode

Profile → **Appearance** lets the user pick **System / Light / Dark**. The choice is saved on the device and applied immediately.

- **Palettes** (`theme/Color.kt`) use one `AppPalette` data class with two instances.
  - `DarkPalette`: the original look, with a pure black background, `#111111` / `#1A1A1A` cards, white accent and grey secondary text.
  - `LightPalette`: lavender-tinted off-white background, white cards, deep plum text and accent.
  - Light mode uses desaturated status colours (sage / sand / coral) and no neon colours.
- **Access:** the palette is provided through `LocalAppPalette` (a `CompositionLocal`), and screens read it with `AppColors.X`. No screen uses hard-coded black or white any more. `AICareerPilotTheme` also builds the matching Material 3 colour scheme, so dialogs, text fields and buttons follow the theme, and it switches the status and navigation bar icons between light and dark.
- **Architecture:** `domain/model/ThemeMode`, `domain/repository/SettingsRepository`, `data/repository/SettingsRepositoryImpl` (SharedPreferences exposed as a `StateFlow`), and `presentation/viewmodel/ThemeViewModel`. `MainActivity` collects the mode and passes it to the theme.

---

## Database schema

Room database `ai_career_pilot_db`, **version 3**.

**`analysis_records`**

| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER PK (auto) | Local id used in navigation routes |
| `fileName` | TEXT | Uploaded resume name |
| `timestamp` | INTEGER | Creation time |
| `rawText` | TEXT | Extracted resume text |
| `aiFeedback` | TEXT | Full Markdown report |
| `resumeScore` | INTEGER | Final hybrid score (0–100) |
| `aiMatchScore` | INTEGER | Raw Gemini score |
| `remoteId` | TEXT, unique index | Cross-device id = Firestore doc id |
| `updatedAt` | INTEGER | Last modification time (LWW) |
| `syncStatus` | TEXT | `PENDING` / `SYNCED` |
| `isDeleted` | INTEGER (bool) | Tombstone |

**`cached_questions`:** `link` (PK), `title`, `tags`, `viewCount`, `score`, `answerCount`, `position`, `cachedAt`.

**Migration `MIGRATION_2_3`:** adds the sync columns with defaults, gives every existing row a unique `remoteId` (`lower(hex(randomblob(16)))`), copies `timestamp` into `updatedAt`, creates the unique index and the `cached_questions` table. Existing users **keep their history**, and their old records are uploaded on the first sync.

---

## Firestore data model and security rules

```text
users/{uid}
 ├── usageCount: number
 ├── lastResetDate: "YYYY-MM-DD"
 └── analyses/{remoteId}
       ├── fileName, timestamp, rawText (≤200k chars), aiFeedback
       ├── resumeScore, aiMatchScore
       ├── updatedAt: number
       └── isDeleted: boolean
```

```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;

      // Resume analysis history: users/{uid}/analyses/{docId}
      match /analyses/{docId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

Each user can read and write **only their own** documents. This is enforced on the server, so even a modified app cannot read another user's history.

---

## Project structure

```text
com.nextgendevs.aicareerpilot
│
├── data
│   ├── local
│   │   ├── AnalysisDao.kt            # history queries + sync queries
│   │   ├── AppDatabase.kt            # Room DB v3 + MIGRATION_2_3
│   │   └── QuestionDao.kt            # trends cache entity, DAO, mappers
│   ├── ocr
│   │   └── MlKitTextRecognitionRepository.kt # on-device OCR (ML Kit)
│   ├── model
│   │   ├── gemini_response/          # AnalysisRecord entity + SyncStatus
│   │   └── news/                     # Question, QuestionsResponse (Gson)
│   ├── remote
│   │   └── NewsApi.kt                # StackOverflowApi (Retrofit)
│   ├── repository
│   │   ├── FirebaseAuthRepository.kt
│   │   ├── ResumeRepositoryImpl.kt   # Gemini + keyword engine + offline fallback
│   │   ├── StackOverflowRepository.kt# offline-first trends
│   │   ├── SyncRepositoryImpl.kt     # WorkManager scheduling + SyncState
│   │   └── UsageRepositoryImpl.kt    # daily limit in Firestore
│   └── sync
│       ├── HistorySyncEngine.kt      # push / pull / merge with Firestore
│       ├── SyncConflictResolver.kt   # last-write-wins rules (unit-tested)
│       └── Workers.kt                # HistorySyncWorker, QuestionsRefreshWorker
│
├── di
│   ├── AppModule.kt                  # Gemini, Room, Firebase, WorkManager, bindings
│   └── StackOverflowModule.kt        # Retrofit
│
├── domain
│   ├── model
│   │   └── AnalysisReport.kt         # AnalysisReport + AnalysisReportParser (unit-tested)
│   ├── repository
│   │   ├── AuthRepository.kt
│   │   ├── ResumeRepository.kt
│   │   ├── SyncRepository.kt         # + SyncState
│   │   ├── TextRecognitionRepository.kt # OCR contract
│   │   └── UsageRepository.kt
│   └── usecases
│       ├── AnalyzeResumeUseCase.kt
│       ├── CanAnalyzeUseCase.kt
│       ├── DeleteRecordUseCase.kt
│       ├── GetHistoryUseCase.kt
│       ├── GetRemainingAttemptsUseCase.kt
│       ├── IncrementUsageUseCase.kt
│       ├── ObserveSyncStateUseCase.kt
│       ├── ScanResumeUseCase.kt
│       └── SyncHistoryUseCase.kt
│
├── presentation
│   ├── screens
│   │   ├── MainScreen.kt             # NavHost + bottom navigation
│   │   ├── HomeScreen.kt             # JD input, upload, results, limits
│   │   ├── ResumeScannerScreen.kt    # CameraX multi-page scanner
│   │   ├── HistoryScreen.kt          # list/grid + sync status bar
│   │   ├── HistoryDetailScreen.kt    # structured premium report
│   │   ├── JobMarketScreen.kt        # developer trends + offline banner
│   │   ├── ProfileScreen.kt          # account + logout
│   │   ├── SignInScreen.kt
│   │   └── SignUpScreen.kt
│   ├── theme/                        # Material 3 colours, typography
│   └── viewmodel
│       ├── AuthViewModel.kt
│       ├── NewsViewModel.kt          # DiscussionViewModel
│       └── ResumeViewModel.kt
│
├── util
│   ├── DeviceUtils.kt                # phone / tablet detection
│   ├── FileUtils.kt                  # file name from Uri
│   └── PdfHelper.kt                  # DocumentHelper: PDF + DOCX extraction
│
├── AICareerPilotApp.kt               # @HiltAndroidApp + WorkManager config
└── MainActivity.kt                   # splash, edge-to-edge, share intents
```

---

## Tech stack

| Category | Libraries |
|---|---|
| Language | Kotlin, Coroutines, Flow |
| UI | Jetpack Compose, Material 3, Navigation Compose, Material Icons Extended, Lottie, Markdown Renderer M3, SplashScreen API |
| Architecture | Clean Architecture, MVVM, Use Cases, Repository pattern |
| DI | Hilt (incl. `hilt-navigation-compose`, `hilt-work`) |
| Local storage | Room (Flow, migrations, transactions), SharedPreferences |
| Background work | WorkManager (constraints, backoff, unique/periodic work) |
| Cloud | Firebase Authentication, Cloud Firestore |
| AI | Google Gemini (`generativeai` SDK, `gemini-2.5-flash`), ML Kit Text Recognition (on-device OCR) |
| Camera | CameraX (`Preview`, `ImageCapture`, `PreviewView`, lifecycle binding) |
| Networking | Retrofit, Gson, OkHttp |
| Documents | PDFBox-Android, custom DOCX (ZIP + XML) parser |
| Build | Gradle KTS, version catalog, KSP, R8 minify + resource shrinking |
| Testing | JUnit4, MockK, kotlinx-coroutines-test |

**SDK:** minSdk 26 · targetSdk 36 · compileSdk 36 · Java 17

---

## Getting started

1. **Clone** the repository and open it in Android Studio.
2. **Firebase:**
   - Create a Firebase project and add an Android app with package `com.nextgendevs.aicareerpilot`.
   - Download `google-services.json` into `app/`.
   - Enable **Authentication → Email/Password**.
   - Create a **Cloud Firestore** database and publish the security rules above.
3. **API keys:** add them to `local.properties` (never commit this file):
   ```properties
   GEMINI_API_KEY=your_gemini_api_key
   NEWS_API_KEY=your_news_api_key
   ```
4. **Run** the `app` configuration on a device or emulator (API 26+).
5. **Test:**
   ```bash
   ./gradlew testDebugUnitTest
   ```
