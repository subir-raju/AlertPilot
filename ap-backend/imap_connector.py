# imap_connector.py
from imapclient import IMAPClient
from email import message_from_bytes
from email.header import decode_header, make_header
from dataclasses import dataclass
from typing import List
import html
import re


@dataclass
class NormalizedEmail:
    id: str
    subject: str
    from_addr: str
    to_addr: str
    body_text: str
    received_at: str  # raw Date header for now


class ImapConnector:
    def __init__(self, host: str, username: str, password: str, ssl: bool = True):
        self.host = host
        self.username = username
        self.password = password
        self.ssl = ssl

    def fetch_recent(self, limit: int = 20) -> List[NormalizedEmail]:
        emails: List[NormalizedEmail] = []
        with IMAPClient(self.host, ssl=self.ssl) as client:
            client.login(self.username, self.password)
            client.select_folder("INBOX")
            # Get UIDs of recent messages
            messages = client.search(["ALL"])
            for uid in messages[-limit:]:
                data = client.fetch(uid, ["RFC822"])
                raw = data[uid][b"RFC822"]
                msg = message_from_bytes(raw)

                subject = str(make_header(
                    decode_header(msg.get("Subject", ""))))
                from_addr = msg.get("From", "")
                to_addr = msg.get("To", "")
                date = msg.get("Date", "")

                body_text = self._extract_text(msg)
                emails.append(
                    NormalizedEmail(
                        id=str(uid),
                        subject=subject,
                        from_addr=from_addr,
                        to_addr=to_addr,
                        body_text=body_text,
                        received_at=date,
                    )
                )
        return emails

    def _extract_text(self, msg) -> str:
        # Walk parts and extract text/plain or fallback to stripped HTML
        if msg.is_multipart():
            parts = []
            for part in msg.walk():
                content_type = part.get_content_type()
                if content_type == "text/plain":
                    parts.append(part.get_payload(
                        decode=True).decode(errors="ignore"))
                elif content_type == "text/html" and not parts:
                    html_body = part.get_payload(
                        decode=True).decode(errors="ignore")
                    parts.append(self._strip_html(html_body))
            return "\n".join(parts).strip()
        else:
            payload = msg.get_payload(decode=True)
            if payload is None:
                return ""
            text = payload.decode(errors="ignore")
            if msg.get_content_type() == "text/html":
                return self._strip_html(text)
            return text

    def _strip_html(self, html_text: str) -> str:
        # Very simple HTML → text
        text = re.sub(r"<[^>]+>", " ", html_text)
        text = html.unescape(text)
        return re.sub(r"\s+", " ", text).strip()
