# classifier.py
from enum import Enum
import re


class ImportanceLevel(str, Enum):
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"


class ImportanceClassifier:
    def classify(self, subject: str | None, body: str | None) -> ImportanceLevel:
        text = f"{subject or ''} {body or ''}".lower()

        if self._is_deadline(text) or self._is_penalty(text) or self._is_account_risk(text):
            return ImportanceLevel.HIGH

        if self._is_action_required(text) or self._is_reminder(text):
            return ImportanceLevel.MEDIUM

        if self._is_promotion(text):
            return ImportanceLevel.LOW

        return ImportanceLevel.LOW

    def _is_deadline(self, text: str) -> bool:
        deadline_keywords = [
            "deadline", "due date", "due on", "due by", "submit by",
            "submission deadline", "last date", "final date",
            "must submit", "must be submitted"
        ]
        time_keywords = [
            "today", "tonight", "tomorrow",
            "by end of day", "eod", "within 24 hours"
        ]
        has_deadline = any(k in text for k in deadline_keywords)
        has_time = any(
            k in text for k in time_keywords) or self._contains_date(text)
        return has_deadline and has_time

    def _contains_date(self, text: str) -> bool:
        patterns = [
            r"\b\d{4}-\d{1,2}-\d{1,2}\b",        # 2026-05-21
            r"\b\d{1,2}/\d{1,2}/\d{2,4}\b",      # 21/05/2026
            r"\b\d{1,2}\.\d{1,2}\.\d{2,4}\b",    # 21.05.2026
            r"\b\d{1,2}\s+(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\b",
            r"\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\s+\d{1,2}\b",
        ]
        return any(re.search(p, text, flags=re.IGNORECASE) for p in patterns)

    def _is_penalty(self, text: str) -> bool:
        words = [
            "fine", "penalty", "charged", "fee will be applied",
            "late fee", "overdue", "past due", "will be cancelled", "will be canceled"
        ]
        return any(w in text for w in words)

    def _is_account_risk(self, text: str) -> bool:
        words = [
            "suspicious activity", "unusual activity", "account locked",
            "account suspended", "reset your password", "security alert"
        ]
        return any(w in text for w in words)

    def _is_action_required(self, text: str) -> bool:
        words = [
            "action required", "please confirm", "please review",
            "approval needed", "respond", "reply", "update your info"
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
