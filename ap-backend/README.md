# AlertPilot Backend

This is the FastAPI backend for the AlertPilot Android application. It handles email fetching (IMAP), classification, and sending (SMTP).

## Setup

1.  **Create a Virtual Environment**:
    ```bash
    python -m venv venv
    source venv/bin/bin/activate  # On Windows: venv\Scripts\activate
    ```

2.  **Install Dependencies**:
    ```bash
    pip install -r requirements.txt
    ```

3.  **Configuration**:
    Set the following environment variables or edit `main.py` directly:
    - `IMAP_HOST`: e.g., `imap.gmail.com`
    - `IMAP_USER`: Your email address
    - `IMAP_PASS`: Your app-specific password
    - `SMTP_HOST`: e.g., `smtp.gmail.com`
    - `SMTP_PORT`: e.g., `587`

4.  **Run the Server**:
    ```bash
    uvicorn main.py:app --reload --host 0.0.0.0 --port 8000
    ```

## API Endpoints

- `GET /health`: Health check.
- `GET /important-emails?limit=20`: Fetches and classifies recent emails.
- `POST /send-email`: Sends an email.
- `POST /classify-email`: Classifies a single subject/body pair.
