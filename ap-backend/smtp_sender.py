# smtp_sender.py
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart


class SmtpSender:
    def __init__(self, host: str, port: int, username: str, password: str, use_tls: bool = True):
        self.host = host
        self.port = port
        self.username = username
        self.password = password
        self.use_tls = use_tls

    def send_email(self, recipient: str, subject: str, body: str) -> bool:
        try:
            msg = MIMEMultipart()
            msg["From"] = self.username
            msg["To"] = recipient
            msg["Subject"] = subject
            msg.attach(MIMEText(body, "plain"))

            server = smtplib.SMTP(self.host, self.port)
            if self.use_tls:
                server.starttls()
            server.login(self.username, self.password)
            server.send_message(msg)
            server.quit()
            return True
        except Exception as e:
            print(f"Failed to send email: {e}")
            return False
