package com.dey.alertpilot.di

import com.dey.alertpilot.data.repository.NotificationRepository
import com.dey.alertpilot.data.classifier.ImportanceClassifier

object AppModule {

    // Singletons for simple manual DI
    val importanceClassifier: ImportanceClassifier by lazy {
        ImportanceClassifier()
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepository(importanceClassifier)
    }
}