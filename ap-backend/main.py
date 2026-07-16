# main.py
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
from classifier import ImportanceClassifier, ImportanceLevel
from imap_connector import ImapConnector
from smtp_sender import SmtpSender
import os

app = FastAPI(title="AlertPilot Backend")

# Configuration (Preferably use environment variables)
IMAP_HOST = os.getenv("IMAP_HOST", "imap.gmail.com")
IMAP_USER = os.getenv("IMAP_USER", "your-email@gmail.com")
IMAP_PASS = os.getenv("IMAP_PASS", "your-app-password")

SMTP_HOST = os.getenv("SMTP_HOST", "smtp.gmail.com")
SMTP_PORT = int(os.getenv("SMTP_PORT", "587"))
SMTP_USER = os.getenv("SMTP_USER", IMAP_USER)
SMTP_PASS = os.getenv("SMTP_PASS", IMAP_PASS)

classifier = ImportanceClassifier()
imap_connector = ImapConnector(IMAP_HOST, IMAP_USER, IMAP_PASS)
smtp_sender = SmtpSender(SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS)

class EmailSummaryDto(BaseModel):
    id: str
    subject: str
    from_addr: str
    importance: ImportanceLevel
    received_at: str

class EmailRequest(BaseModel):
    recipient: str
    subject: str
    body: str

class EmailResponse(BaseModel):
    success: bool
    message: str

@app.get("/health")
def health():
    return {"status": "ok", "app": "AlertPilot Backend"}

@app.get("/important-emails", response_model=List[EmailSummaryDto])
def get_important_emails(limit: int = 20):
    try:
        raw_emails = imap_connector.fetch_recent(limit=limit)
        summaries = []
        for email in raw_emails:
            importance = classifier.classify(email.subject, email.body_text)
            summaries.append(EmailSummaryDto(
                id=email.id,
                subject=email.subject,
                from_addr=email.from_addr,
                importance=importance,
                received_at=email.received_at
            ))
        return summaries
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/send-email", response_model=EmailResponse)
def send_email(request: EmailRequest):
    success = smtp_sender.send_email(
        recipient=request.recipient,
        subject=request.subject,
        body=request.body
    )
    if success:
        return EmailResponse(success=True, message="Email sent successfully")
    else:
        return EmailResponse(success=False, message="Failed to send email")

@app.post("/classify-email")
def classify_email(subject: str, body: str):
    importance = classifier.classify(subject, body)
    return {"importance": importance}
