package com.dey.alertpilot.data.classifier

import com.dey.alertpilot.data.model.ImportanceLevel
import java.util.Locale
import java.util.regex.Pattern

class ImportanceClassifier {

    fun classify(
        text: String?,
        title: String?,
        packageName: String
    ): ImportanceLevel {
        val content = buildString {
            if (!title.isNullOrBlank()) append(title).append(" ")
            if (!text.isNullOrBlank()) append(text)
        }.lowercase(Locale.getDefault())

        // 1) Hard HIGH rules: deadlines, penalties, overdue, critical account
        if (isDeadline(content) || isPenalty(content) || isAccountRisk(content)) {
            return ImportanceLevel.HIGH
        }

        // 2) Medium rules: tasks / reminders without hard deadline
        if (isActionRequired(content) || isReminder(content)) {
            return ImportanceLevel.MEDIUM
        }

        // 3) Promotion / marketing
        if (isPromotion(content)) {
            return ImportanceLevel.LOW
        }

        // 4) Default
        return ImportanceLevel.LOW
    }

    private fun isDeadline(text: String): Boolean {
        val deadlineKeywords = listOf(
            "deadline", "due date", "due on", "due by", "submit by", "submission deadline",
            "last date", "final date", "must submit", "must be submitted"
        )

        val timeKeywords = listOf(
            "today", "tonight", "tomorrow",
            "by end of day", "eod", "within 24 hours"
        )

        val hasDeadlineWord = deadlineKeywords.any { it in text }
        val hasTimeWord = timeKeywords.any { it in text } || containsDate(text)

        return hasDeadlineWord && hasTimeWord
    }

    private fun containsDate(text: String): Boolean {
        // Very simple date pattern: 2026-05-21, 21/05/2026, 21.05.2026, 21 May, May 21
        val datePatterns = listOf(
            "\\b\\d{4}-\\d{1,2}-\\d{1,2}\\b",       // 2026-05-21
            "\\b\\d{1,2}/\\d{1,2}/\\d{2,4}\\b",     // 21/05/2026
            "\\b\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}\\b", // 21.05.2026
            "\\b\\d{1,2}\\s+(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b",
            "\\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\s+\\d{1,2}\\b"
        )
        return datePatterns.any { pattern ->
            Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text).find()
        }
    }

    private fun isPenalty(text: String): Boolean {
        val penaltyKeywords = listOf(
            "fine", "penalty", "charged", "fee will be applied",
            "late fee", "late payment fee", "overdue", "past due",
            "will be cancelled", "will be canceled"
        )
        return penaltyKeywords.any { it in text }
    }

    private fun isAccountRisk(text: String): Boolean {
        val accountKeywords = listOf(
            "suspicious activity", "unusual activity", "account locked",
            "account suspended", "reset your password", "security alert"
        )
        return accountKeywords.any { it in text }
    }

    private fun isActionRequired(text: String): Boolean {
        val words = listOf(
            "action required", "please confirm", "please review",
            "approve", "approval needed", "respond", "reply", "update your info"
        )
        return words.any { it in text }
    }

    private fun isReminder(text: String): Boolean {
        val words = listOf(
            "reminder", "don't forget", "this is a reminder",
            "follow up", "follow-up"
        )
        return words.any { it in text }
    }

    private fun isPromotion(text: String): Boolean {
        val promoWords = listOf(
            "sale", "discount", "offer", "promotion", "subscribe",
            "newsletter", "coupon", "deal", "flash sale", "limited time offer"
        )
        return promoWords.any { it in text }
    }
}