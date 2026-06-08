# main.py
from classifier import ImportanceClassifier, ImportanceLevel
from pydantic import BaseModel
from fastapi import FastAPI

app = FastAPI()


@app.get("/health")
def health():
    return {"status": "okk"}


# main.py (extend)

classifier = ImportanceClassifier()


class EmailInput(BaseModel):
    subject: str | None = None
    body: str | None = None


class EmailImportanceOutput(BaseModel):
    importance: ImportanceLevel


@app.post("/classify-email", response_model=EmailImportanceOutput)
def classify_email(data: EmailInput):
    importance = classifier.classify(data.subject, data.body)
    return EmailImportanceOutput(importance=importance)
