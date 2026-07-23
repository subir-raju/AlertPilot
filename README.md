# AlertPilot ✈️🚨

**AlertPilot** is a smart notification and email management system designed to filter out the noise and surface critical information that usually gets buried. It doesn't just look at subject lines; it scans the **full body text** of notifications and emails to detect hidden deadlines, penalties, and travel-related urgencies.

Whether it's a mandatory airline check-in to avoid a €55 fee or a critical account security alert, AlertPilot ensures you stay informed about what truly matters.

---

## 🚀 Key Features

- **Deep Text Extraction**: Goes beyond notification previews to read `BigText`, `MessagingStyle` conversations, and multi-line alerts.
- **Urgency Classifier**: A sophisticated engine that detects:
    - **Travel Urgency**: Flight check-ins, boarding passes, and airline fees.
    - **Deadlines**: "Due by", "Action required", "Expiration", and relative timeframes (e.g., "2 hours before").
    - **Financial Penalties**: Late fees, unpaid invoices, and "avoid a fee" warnings.
    - **Account Security**: Suspicious activity, OTPs, and verification codes.
- **Dual-Mode Operation**:
    - **Android Listener**: Real-time monitoring of all device notifications.
    - **Email Integration**: Backend fetching of important emails via IMAP.
- **Clean & Minimal UI**: A high-density list view for quick scanning, with detailed popups for deep dives.
- **Smart Filtering**: Toggle between "Priority Only" and "Everything" to focus on what needs immediate action.

---

## 🛠️ Technology Stack

### Android App
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit 3.0 + Moshi
- **Services**: `NotificationListenerService` for system-wide alert interception.

### Python Backend
- **Framework**: FastAPI
- **Mail Protocol**: IMAP (fetching) and SMTP (sending)
- **Server**: Uvicorn
- **Validation**: Pydantic

---

## 📦 Project Structure

```text
AlertPilot/
├── app/                        # Android Application
│   ├── src/main/java/com/dey/alertpilot/
│   │   ├── data/               # Repositories, API models, & Classifiers
│   │   ├── di/                 # Manual Dependency Injection (AppModule)
│   │   ├── notification/       # NotificationListener logic
│   │   └── ui/                 # Compose Screens & ViewModels
│   └── src/main/AndroidManifest.xml # Permissions & Service declarations
├── ap-backend/                 # Python FastAPI Backend
│   ├── main.py                 # API Endpoints
│   ├── classifier.py           # Urgency Detection Logic
│   ├── imap_connector.py       # IMAP Fetching
│   ├── smtp_sender.py          # SMTP Sending
│   └── requirements.txt        # Python Dependencies
└── README.md                   # You are here!
```

---

## ⚙️ Setup & Installation

### 1. Android App
1. Open the project in **Android Studio**.
2. Sync the project with Gradle files.
3. Build and run the `:app` module on an Android device (API 24+).
4. **Grant Permissions**: 
    - Upon launching, click the **Settings (gear) icon** -> **Notification Access**.
    - Toggle **AlertPilot** to "On" to allow the app to intercept notifications.

### 2. Python Backend
1. Navigate to the `ap-backend` directory.
2. Create and activate a virtual environment:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. **Configuration**:
   Set environment variables or edit `main.py` with your credentials:
   - `IMAP_HOST`: e.g., `imap.gmail.com`
   - `IMAP_USER`: Your email address
   - `IMAP_PASS`: Your app-specific password
5. Run the server:
   ```bash
   uvicorn main:app --reload --host 0.0.0.0 --port 8000
   ```

---

## 📄 License

This project is developed for personal use to solve the problem of missed travel and utility deadlines. Feel free to fork and adapt it for your own needs.

---

*Stay informed. Stay ahead. Never miss a flight again with AlertPilot.*
