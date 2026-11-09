"""Grabbit email verification for the Android Studio emulator."""

import getpass
import hashlib
import hmac
import json
import os
import re
import secrets
import smtplib
import ssl
import sys
import threading
import time
from dataclasses import dataclass
from email.message import EmailMessage
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

CODE_LIFETIME = 300
RESEND_DELAY = 60
MAX_ATTEMPTS = 5
SEND_WINDOW = 3600
MAX_SENDS_PER_EMAIL = 5
MAX_TOTAL_SENDS = 60
EMAIL_PATTERN = re.compile(r"[a-z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-z0-9-]+(?:\.[a-z0-9-]+)+", re.ASCII)
CODE_PATTERN = re.compile(r"[0-9]{6}")


class VerificationError(Exception):
    def __init__(self, status, reason, retry_after=0):
        super().__init__(reason)
        self.status = status
        self.reason = reason
        self.retry_after = retry_after


def email_address(value):
    if isinstance(value, str):
        value = value.strip().lower()
        if len(value) <= 254 and EMAIL_PATTERN.fullmatch(value):
            return value
    raise VerificationError(400, "invalid_email")


@dataclass
class Challenge:
    token: str
    digest: bytes
    expires: float
    attempts_left: int = MAX_ATTEMPTS


class EmailVerification:
    def __init__(self, deliver, clock=time.monotonic):
        self.deliver = deliver
        self.clock = clock
        self.secret = secrets.token_bytes(32)
        self.lock = threading.Lock()
        self.challenges = {}
        self.send_requests = []
        self.sending = set()

    def _digest(self, token, code):
        return hmac.digest(self.secret, (token + ":" + code).encode("ascii"), hashlib.sha256)

    def request_code(self, email):
        email = email_address(email)
        with self.lock:
            now = self.clock()
            self.send_requests = [(address, sent) for address, sent in self.send_requests
                                  if now - sent < SEND_WINDOW]
            self.challenges = {address: challenge for address, challenge in self.challenges.items()
                               if challenge.expires > now}
            sent_for_email = [sent for address, sent in self.send_requests if address == email]
            if email in self.sending:
                raise VerificationError(429, "too_many_requests", RESEND_DELAY)
            if sent_for_email and now - sent_for_email[-1] < RESEND_DELAY:
                remaining = RESEND_DELAY - (now - sent_for_email[-1])
                raise VerificationError(429, "too_many_requests", int(remaining) + 1)
            if len(sent_for_email) >= MAX_SENDS_PER_EMAIL or len(self.send_requests) >= MAX_TOTAL_SENDS:
                raise VerificationError(429, "too_many_requests", SEND_WINDOW)
            self.send_requests.append((email, now))
            self.sending.add(email)

        token = secrets.token_urlsafe(24)
        code = f"{secrets.randbelow(1_000_000):06d}"
        try:
            self.deliver(email, code)
            with self.lock:
                self.challenges[email] = Challenge(
                    token, self._digest(token, code), self.clock() + CODE_LIFETIME)
        except ssl.SSLCertVerificationError as error:
            print("Gmail certificate verification failed. Check Python's trusted certificates.",
                  file=sys.stderr, flush=True)
            raise VerificationError(502, "certificate_error", RESEND_DELAY) from error
        except smtplib.SMTPAuthenticationError as error:
            print("Gmail rejected the sender address or app password.", file=sys.stderr, flush=True)
            raise VerificationError(502, "delivery_failed", RESEND_DELAY) from error
        except OSError as error:
            print(f"Email delivery failed ({type(error).__name__}). Check the mail server connection.",
                  file=sys.stderr, flush=True)
            raise VerificationError(502, "delivery_failed", RESEND_DELAY) from error
        finally:
            with self.lock:
                self.sending.remove(email)
        return {"challengeId": token, "expiresIn": CODE_LIFETIME, "retryAfter": RESEND_DELAY}

    def verify_code(self, email, token, code):
        email = email_address(email)
        if not isinstance(code, str) or not CODE_PATTERN.fullmatch(code):
            raise VerificationError(400, "invalid_code")
        if not isinstance(token, str) or not re.fullmatch(r"[A-Za-z0-9_-]{32}", token):
            raise VerificationError(400, "invalid_challenge")
        with self.lock:
            challenge = self.challenges.get(email)
            if challenge is None or challenge.token != token:
                raise VerificationError(400, "invalid_challenge")
            if self.clock() >= challenge.expires:
                del self.challenges[email]
                raise VerificationError(400, "expired_code")
            if challenge.attempts_left == 0:
                raise VerificationError(429, "attempts_exhausted")
            if not hmac.compare_digest(challenge.digest, self._digest(token, code)):
                challenge.attempts_left -= 1
                if challenge.attempts_left == 0:
                    raise VerificationError(429, "attempts_exhausted")
                raise VerificationError(400, "invalid_code")
            del self.challenges[email]
            return {"verified": True, "email": email}


def gmail_tls_context():
    context = ssl.create_default_context()
    system_bundle = Path("/etc/ssl/cert.pem")
    if sys.platform == "darwin" and context.cert_store_stats()["x509_ca"] == 0 and system_bundle.is_file():
        context.load_verify_locations(cafile=str(system_bundle))
    return context


def gmail_delivery(sender, app_password):
    tls_context = gmail_tls_context()

    def send(email, code):
        message = EmailMessage()
        message["From"] = sender
        message["To"] = email
        message["Subject"] = "Your Grabbit verification code"
        message.set_content(f"Your Grabbit verification code is {code}.\n\n"
                            "It expires in 5 minutes and can be used once.\n"
                            "If you did not request it, you can ignore this email.\n")
        with smtplib.SMTP("smtp.gmail.com", 587, timeout=12) as smtp:
            smtp.starttls(context=tls_context)
            smtp.login(sender, app_password)
            smtp.send_message(message)
        print("Gmail accepted a verification email.", flush=True)

    return send


def make_server(service, port=8765, host="127.0.0.1"):
    class Handler(BaseHTTPRequestHandler):
        timeout = 15

        def do_GET(self):
            if self.path == "/healthz":
                self.reply(200, {"status": "ok"})
            else:
                self.reply(404, {"error": "not_found"})

        def do_POST(self):
            try:
                if self.path not in ("/v1/email/request", "/v1/email/verify"):
                    raise VerificationError(404, "not_found")
                length = int(self.headers.get("Content-Length", "0"))
                if not 0 < length <= 1024 or self.headers.get_content_type() != "application/json":
                    raise VerificationError(400, "invalid_request")
                data = json.loads(self.rfile.read(length))
                if not isinstance(data, dict):
                    raise VerificationError(400, "invalid_request")
                if self.path == "/v1/email/request":
                    result = service.request_code(data.get("email"))
                else:
                    result = service.verify_code(data.get("email"), data.get("challengeId"), data.get("code"))
                self.reply(200, result)
            except VerificationError as error:
                self.reply(error.status, {"error": error.reason, "retryAfter": error.retry_after})
            except (ValueError, UnicodeError):
                self.reply(400, {"error": "invalid_request"})

        def reply(self, status, result):
            body = json.dumps(result).encode("utf-8")
            self.send_response(status)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Cache-Control", "no-store")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            try:
                self.wfile.write(body)
            except (BrokenPipeError, ConnectionResetError):
                pass

        def log_message(self, format, *args):
            pass

    return ThreadingHTTPServer((host, port), Handler)


def main():
    try:
        host = os.environ.get("EMAIL_SERVER_HOST", "127.0.0.1")
        port = int(os.environ.get("PORT", "8765"))
        if not 1 <= port <= 65535:
            raise ValueError("PORT must be between 1 and 65535.")
        sender = os.environ.get("SMTP_USER")
        app_password = os.environ.get("SMTP_PASSWORD")
        if not sender or not app_password:
            if host != "127.0.0.1" or not sys.stdin.isatty():
                raise ValueError("Set SMTP_USER and SMTP_PASSWORD in the server environment.")
            sender = sender or input("Sender Gmail address: ")
            app_password = app_password or getpass.getpass("Gmail app password: ")
        sender = email_address(sender)
        app_password = "".join(app_password.split())
        if not re.fullmatch(r"[A-Za-z0-9]{16}", app_password):
            raise ValueError("Enter the 16-character Gmail app password.")
        with make_server(EmailVerification(gmail_delivery(sender, app_password)), port, host) as server:
            print(f"Grabbit email server is ready on http://{host}:{port}. Press Ctrl+C to stop.", flush=True)
            server.serve_forever()
    except VerificationError:
        raise SystemExit("Enter a valid sender email address.") from None
    except (ValueError, EOFError, OSError) as error:
        raise SystemExit(f"Could not start the email server: {error}") from None
    except KeyboardInterrupt:
        print("\nGrabbit email server stopped.")


if __name__ == "__main__":
    main()
