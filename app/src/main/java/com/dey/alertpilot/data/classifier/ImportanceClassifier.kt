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

        // 1) Hard HIGH rules: deadlines, penalties, overdue, critical account, travel urgency
        if (isDeadline(content) || isPenalty(content) || isAccountRisk(content) || isTravelUrgency(content)) {
            return ImportanceLevel.HIGH
        }

        // 2) Medium rules: tasks / reminders without hard deadline
        if (isActionRequired(content) || isReminder(content)) {
            return ImportanceLevel.MEDIUM
        }

        // 3) Promotion / marketing (Check this last, as some promos might trigger "action required" but we want to ignore them unless they have deadlines)
        if (isPromotion(content) && !isDeadline(content) && !isTravelUrgency(content)) {
            return ImportanceLevel.LOW
        }

        // 4) Default
        return ImportanceLevel.LOW
    }

    private fun isTravelUrgency(text: String): Boolean {
        // Broaden keywords for travel
        val travelKeywords = listOf("flight", "boarding", "check-in", "check in", "airline", "gate change", "delayed", "ryanair", "boarding pass")
        
        // Indicators that the travel info is urgent/mandatory
        val urgencyIndicators = listOf(
            "fee", "€", "$", "avoid", "mandatory", "required", "immediately", 
            "before you fly", "hours before", "days before", "action required", 
            "must download", "only way to access"
        )
        
        val hasTravelWord = travelKeywords.any { it in text }
        val hasUrgency = urgencyIndicators.any { it in text }
        
        return hasTravelWord && hasUrgency
    }

    private fun isDeadline(text: String): Boolean {
        val deadlineKeywords = listOf(
            "deadline", "due date", "due on", "due by", "submit by", "submission deadline",
            "last date", "final date", "must submit", "must be submitted", "expiration", "expires",
            "valid until", "cut-off", "cutoff", "action needed by", "before you"
        )

        val timeKeywords = listOf(
            "today", "tonight", "tomorrow", "asap", "immediately", "urgent",
            "hours", "minutes", "days", "eod"
        )

        val hasDeadlineWord = deadlineKeywords.any { it in text }
        val hasTimeWord = timeKeywords.any { it in text } || containsDate(text)

        // Combinations like "2 hours before" or "within 24 hours"
        val hasRelativeTime = text.contains(Regex("\\d+\\s*(hour|minute|day)s?\\s*before"))

        return (hasDeadlineWord && hasTimeWord) || (text.contains("urgent") && hasDeadlineWord) || hasRelativeTime
    }

    private fun containsDate(text: String): Boolean {
        val datePatterns = listOf(
            "\\b\\d{4}-\\d{1,2}-\\d{1,2}\\b",       // 2026-05-21
            "\\b\\d{1,2}/\\d{1,2}/\\d{2,4}\\b",     // 21/05/2026
            "\\b\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}\\b", // 21.05.2026
            "\\b\\d{1,2}\\s+(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\b",
            "\\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\s+\\d{1,2}\\b",
            "\\b(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b"
        )
        return datePatterns.any { pattern ->
            Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text).find()
        }
    }

    private fun isPenalty(text: String): Boolean {
        val penaltyKeywords = listOf(
            "fine", "penalty", "charged", "fee will be applied",
            "late fee", "late payment fee", "overdue", "past due",
            "will be cancelled", "will be canceled", "unpaid", "invoice", "payment failed",
            "collection", "legal action", "avoid a fee", "extra charge"
        )
        // Also check for currency symbols followed by numbers or keywords
        val hasCurrency = text.contains("€") || text.contains("$") || text.contains("£")
        val hasFeeContext = text.contains("fee") || text.contains("charge") || text.contains("pay")
        
        return penaltyKeywords.any { it in text } || (hasCurrency && hasFeeContext)
    }

    private fun isAccountRisk(text: String): Boolean {
        val accountKeywords = listOf(
            "suspicious activity", "unusual activity", "account locked",
            "account suspended", "reset your password", "security alert",
            "unauthorized", "login attempt", "verify your identity", "otp", "verification code"
        )
        return accountKeywords.any { it in text }
    }

    private fun isActionRequired(text: String): Boolean {
        val words = listOf(
            "action required", "please confirm", "please review",
            "approve", "approval needed", "respond", "reply", "update your info",
            "need to do", "what do i need", "download the app", "you need the"
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
