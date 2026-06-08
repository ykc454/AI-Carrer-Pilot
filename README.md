AI Career Pilot – Android Application

Name: Yash Chaudhari

Project Description

AI Career Pilot is an Android application built using Kotlin, Jetpack Compose, Clean Architecture, MVVM, Hilt, Room Database, Firebase Authentication, and Gemini AI.

The app helps users analyze their resumes against a Job Description (JD) using Google's Gemini AI. Users can upload or paste resume and job description content, receive detailed ATS-style analysis, identify skill gaps, improve resume quality, and track previous analyses.

In addition to resume analysis, the application provides insights into current software industry trends by integrating with the Stack Overflow API and News APIs. This enables users to stay updated with in-demand technologies, programming languages, frameworks, and industry developments.

The application follows a scalable Clean Architecture approach, ensuring maintainability, testability, and separation of concerns.

Screenshots:
<img width="720" height="1600" alt="home screen 2" src="https://github.com/user-attachments/assets/7c9f8ec6-01db-4992-83d8-7af6833ab113" />
<img width="720" height="1600" alt="historylist screen" src="https://github.com/user-attachments/assets/fe9db0ee-59d5-499f-9edb-403ca63474f1" />
<img width="1024" height="1536" alt="ChatGPT Image Jun 1, 2026, 03_37_47 AM" src="https://github.com/user-attachments/assets/b3ccaa3f-e8be-4010-bdbe-490015eb23f1" />

Architecture

The project follows Clean Architecture + MVVM principles.

Presentation Layer
        │
        ▼
    ViewModels
        │
        ▼
     UseCases
        │
        ▼
Repository Interfaces
        │
        ▼
Repository Implementations
        │
 ┌──────┴────────┐
 ▼               ▼
Local Data     Remote Data
(Room DB)      APIs/Firebase/Gemini

Directory Structure
com.example.aicareerpilot
│
├── data
│   │
│   ├── local
│   │   ├── AnalysisDao.kt
│   │   └── AppDatabase.kt
│   │
│   ├── model
│   │   ├── gemini_response/
│   │   └── news/
│   │
│   ├── remote
│   │   └── NewsApi.kt
│   │
│   └── repository
│       ├── FirebaseAuthRepository.kt
│       ├── ResumeRepositoryImpl.kt
│       ├── StackOverflowRepository.kt
│       └── UsageRepositoryImpl.kt
│
├── di
│   ├── AppModule.kt
│   └── StackOverflowModule.kt
│
├── domain
│   │
│   ├── repository
│   │   ├── AuthRepository.kt
│   │   ├── ResumeRepository.kt
│   │   └── UsageRepository.kt
│   │
│   └── usecases
│       ├── AnalyzeResumeUseCase.kt
│       ├── CanAnalyzeUseCase.kt
│       ├── DeleteRecordUseCase.kt
│       ├── GetHistoryUseCase.kt
│       ├── GetRemainingAttemptsUseCase.kt
│       └── IncrementUsageUseCase.kt
│
├── presentation
│   │
│   ├── screens
│   │   ├── HomeScreen.kt
│   │   ├── HistoryScreen.kt
│   │   ├── HistoryDetailScreen.kt
│   │   ├── JobMarketScreen.kt
│   │   ├── ProfileScreen.kt
│   │   ├── SignInScreen.kt
│   │   └── MainScreen.kt
│   │
│   ├── theme
│   │
│   └── viewmodel
│       ├── AuthViewModel.kt
│       ├── NewsViewModel.kt
│       └── ResumeViewModel.kt
│
├── util
│
├── AICareerPilotApp.kt
└── MainActivity.kt

Breakdown:

    data/: Contains local database, API services, data models, and repository implementations responsible for managing application data.

    di/: Contains Hilt dependency injection modules used to provide dependencies across the application.

    domain/: Contains repository interfaces and use cases that define the core business logic of the application.

    presentation/: Contains Jetpack Compose UI screens, ViewModels, and theme configurations responsible for user interaction and state management.

    util/: Contains helper classes, utility functions, constants, and common extensions used throughout the project.

    AICareerPilotApp.kt: Application class responsible for initializing app-wide configurations and dependencies.

    MainActivity.kt: The main entry point of the application that hosts the Compose UI and navigation.
