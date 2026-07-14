package com.dey.alertpilot.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface EmailApiService {
    @POST("send-email")
    suspend fun sendEmail(@Body request: EmailRequest): EmailResponse

    @GET("important-emails")
    suspend fun getImportantEmails(@Query("limit") limit: Int): List<EmailSummaryDto>
}

data class EmailRequest(
    val recipient: String,
    val subject: String,
    val body: String
)

data class EmailResponse(
    val success: Boolean,
    val message: String
)

data class EmailSummaryDto(
    val id: String,
    val subject: String,
    val sender: String,
    val timestamp: Long,
    val snippet: String
)
