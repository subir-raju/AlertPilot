package com.dey.alertpilot.data.classifier

import com.dey.alertpilot.data.model.ImportanceLevel

class ImportanceClassifier {

    private val highImportanceKeywords = listOf(
        "check-in", "check in", "gate change", "flight", "boarding",
        "payment due", "overdue", "fine", "penalty", "last chance",
        "security alert", "suspicious", "password reset"
    )

    private val mediumImportanceKeywords = listOf(
        "reminder", "appointment", "schedule", "booking",
        "delivery", "shipment"
    )

    fun classify(text: String?, title: String?, packageName: String): ImportanceLevel {
        val content = (title.orEmpty() + " " + text.orEmpty()).lowercase()

        // Airline example – prioritize certain senders
        if (packageName.contains("air", ignoreCase = true) &&
            highImportanceKeywords.any { content.contains(it) }
        ) {
            return ImportanceLevel.HIGH
        }

        if (highImportanceKeywords.any { content.contains(it) }) {
            return ImportanceLevel.HIGH
        }

        if (mediumImportanceKeywords.any { content.contains(it) }) {
            return ImportanceLevel.MEDIUM
        }

        return ImportanceLevel.LOW
    }
}