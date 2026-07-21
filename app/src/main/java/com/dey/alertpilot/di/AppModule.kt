package com.dey.alertpilot.di

import com.dey.alertpilot.data.repository.NotificationRepository
import com.dey.alertpilot.data.classifier.ImportanceClassifier
import com.dey.alertpilot.data.api.EmailApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object AppModule {

    // Singletons for simple manual DI
    val importanceClassifier: ImportanceClassifier by lazy {
        ImportanceClassifier()
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepository(importanceClassifier)
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.0.100:8000/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val emailApi: EmailApiService by lazy {
        retrofit.create(EmailApiService::class.java)
    }
}
