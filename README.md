# AI Career Pilot

**Name:** Yash Chaudhari

## Project Description

AI Career Pilot is an Android application built using **Kotlin, Jetpack Compose, Clean Architecture, MVVM, Hilt, Room Database, Firebase Authentication, and Gemini AI**.

The app helps users analyze their resumes against a Job Description (JD) using Google's Gemini AI. Users can upload or paste resume and job description content, receive detailed ATS-style analysis, identify skill gaps, improve resume quality, and track previous analyses.

In addition to resume analysis, the application provides insights into current software industry trends by integrating with the Stack Overflow API and News APIs. This enables users to stay updated with in-demand technologies, programming languages, frameworks, and industry developments.

The application follows a scalable Clean Architecture approach, ensuring maintainability, testability, and separation of concerns.

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

## Architecture

The project follows **Clean Architecture + MVVM** principles.

```text
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
```

---

## Directory Structure

```text
com.example.aicareerpilot
│
├── data
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
```

---

## Breakdown

* **data/**: Contains local database, API services, data models, and repository implementations responsible for managing application data.

* **di/**: Contains Hilt dependency injection modules used to provide dependencies across the application.

* **domain/**: Contains repository interfaces and use cases that define the core business logic of the application.

* **presentation/**: Contains Jetpack Compose UI screens, ViewModels, and theme configurations responsible for user interaction and state management.

* **util/**: Contains helper classes, utility functions, constants, and common extensions used throughout the project.

* **AICareerPilotApp.kt**: Application class responsible for initializing app-wide configurations and dependencies.

* **MainActivity.kt**: The main entry point of the application that hosts the Compose UI and navigation.

---

## Tech Stack

* Kotlin
* Jetpack Compose
* MVVM Architecture
* Clean Architecture
* Hilt
* Room Database
* Firebase Authentication
* Gemini AI
* Stack Overflow API
* News API

---
