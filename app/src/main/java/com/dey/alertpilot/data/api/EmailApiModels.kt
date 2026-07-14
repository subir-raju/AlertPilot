package com.dey.alertpilot.data.api

import com.dey.alertpilot.data.model.ImportanceLevel
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EmailSummaryDto(
    val id: String,
    val subject: String,
    val from_addr: String,
    val importance: ImportanceLevel,
    val received_at: String
)

@JsonClass(generateAdapter = true)
data class EmailDetailDto(
    val id: String,
    val subject: String,
    val from_addr: String,
    val to_addr: String,
    val body: String,
    val importance: ImportanceLevel,
    val received_at: String
)

@JsonClass(generateAdapter = true)
data class EmailRequest(
    val recipient: String,
    val subject: String,
    val body: String
)

@JsonClass(generateAdapter = true)
data class EmailResponse(
    val success: Boolean,
    val message: String
)
