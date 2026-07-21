# classifier.py
from enum import Enum
import re

class ImportanceLevel(str, Enum):
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"

class ImportanceClassifier:
    def classify(self, subject: str | None, body: str | None) -> ImportanceLevel:
        content = f"{subject or ''} {body or ''}".lower()

        # 1) Hard HIGH rules: deadlines, penalties, overdue, critical account, travel urgency
        if (self._is_deadline(content) or
            self._is_penalty(content) or
            self._is_account_risk(content) or
            self._is_travel_urgency(content)):
            return ImportanceLevel.HIGH

        # 2) Medium rules: tasks / reminders without hard deadline
        if self._is_action_required(content) or self._is_reminder(content):
            return ImportanceLevel.MEDIUM

        # 3) Promotion / marketing (Ignore unless it has a deadline/urgency)
        if self._is_promotion(content) and not self._is_deadline(content) and not self._is_travel_urgency(content):
            return ImportanceLevel.LOW

        return ImportanceLevel.LOW

    def _is_travel_urgency(self, text: str) -> bool:
        travel_keywords = ["flight", "boarding", "check-in", "check in", "airline", "gate change", "delayed", "ryanair", "boarding pass"]
        urgency_indicators = [
            "fee", "€", "$", "avoid", "mandatory", "required", "immediately",
            "before you fly", "hours before", "days before", "action required",
            "need to do", "must download", "only way to access"
        ]

        has_travel = any(k in text for k in travel_keywords)
        has_urgency = any(k in text for k in urgency_indicators)
        return has_travel and has_urgency

    def _is_deadline(self, text: str) -> bool:
        deadline_keywords = [
            "deadline", "due date", "due on", "due by", "submit by",
            "submission deadline", "last date", "final date",
            "must submit", "must be submitted", "expiration", "expires",
            "valid until", "cut-off", "cutoff", "before you"
        ]
        time_keywords = [
            "today", "tonight", "tomorrow", "asap", "immediately", "urgent",
            "hours", "minutes", "days", "eod"
        ]
        has_deadline = any(k in text for k in deadline_keywords)
        has_time = any(k in text for k in time_keywords) or self._contains_date(text)

        # Regex for relative time: "2 hours before", "24 hours before"
        has_relative_time = bool(re.search(r"\d+\s*(hour|minute|day)s?\s*before", text))

        return (has_deadline and has_time) or ("urgent" in text and has_deadline) or has_relative_time

    def _contains_date(self, text: str) -> bool:
        patterns = [
            r"\b\d{4}-\d{1,2}-\d{1,2}\b",        # 2026-05-21
            r"\b\d{1,2}/\d{1,2}/\d{2,4}\b",      # 21/05/2026
            r"\b\d{1,2}\.\d{1,2}\.\d{2,4}\b",    # 21.05.2026
            r"\b\d{1,2}\s+(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\b",
            r"\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\s+\d{1,2}\b",
            r"\b(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\b"
        ]
        return any(re.search(p, text, flags=re.IGNORECASE) for p in patterns)

    def _is_penalty(self, text: str) -> bool:
        words = [
            "fine", "penalty", "charged", "fee will be applied",
            "late fee", "overdue", "past due", "will be cancelled", "will be canceled",
            "unpaid", "invoice", "payment failed", "collection", "legal action",
            "avoid a fee", "extra charge"
        ]
        has_currency = any(c in text for c in ["€", "$", "£"])
        has_fee_context = any(w in text for w in ["fee", "charge", "pay"])
        return any(w in text for w in words) or (has_currency and has_fee_context)

    def _is_account_risk(self, text: str) -> bool:
        words = [
            "suspicious activity", "unusual activity", "account locked",
            "account suspended", "reset your password", "security alert",
            "unauthorized", "login attempt", "verify your identity", "otp", "verification code"
        ]
        return any(w in text for w in words)

    def _is_action_required(self, text: str) -> bool:
        words = [
            "action required", "please confirm", "please review",
            "approval needed", "respond", "reply", "update your info",
            "need to do", "what do i need", "download the app", "you need the"
        ]
        return any(w in text for w in words)

    def _is_reminder(self, text: str) -> bool:
        words = [
            "reminder", "don't forget", "this is a reminder",
            "follow up", "follow-up"
        ]
        return any(w in text for w in words)

    def _is_promotion(self, text: str) -> bool:
        words = [
            "sale", "discount", "offer", "promotion", "subscribe",
            "newsletter", "coupon", "deal", "flash sale", "limited time offer"
        ]
        return any(w in text for w in words)
