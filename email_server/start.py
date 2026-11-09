"""Start the local Grabbit mail server before Android Studio launches the app."""

import fcntl
import json
import os
from pathlib import Path
import subprocess
import sys
import time
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

from server import VerificationError, email_address

SERVER_FILE = Path(__file__).with_name("server.py").resolve()
STATE_DIR = SERVER_FILE.parent.parent / ".gradle" / "grabbit-email"
KEYCHAIN_SERVICE = "Grabbit Email SMTP"
SETUP_COMMAND = "python3 -B email_server/start.py --configure"


def configure():
    sender = email_address(input("Sender Gmail address: "))
    subprocess.run([
        "/usr/bin/security", "add-generic-password", "-U", "-a", sender,
        "-s", KEYCHAIN_SERVICE, "-w",
    ], check=True)
    (STATE_DIR / "sender").write_text(sender + "\n", encoding="utf-8")
    print("Grabbit email credentials saved in macOS Keychain.")


def server_is_ready():
    # This probe also recognizes a server started manually using server.py.
    request = Request("http://127.0.0.1:8765/v1/email/verify", data=b"{}",
                      headers={"Content-Type": "application/json"})
    try:
        with urlopen(request, timeout=1):
            pass
    except HTTPError as response:
        with response:
            if response.code == 400:
                try:
                    if json.loads(response.read(1024)).get("error") == "invalid_email":
                        return True
                except (ValueError, AttributeError):
                    pass
    except URLError as error:
        if isinstance(error.reason, ConnectionRefusedError):
            return False
        raise RuntimeError("Cannot reach the Grabbit email server on port 8765.") from error
    raise RuntimeError("Port 8765 is already in use by another service.")


def server_environment():
    environment = os.environ.copy()
    sender_file = STATE_DIR / "sender"
    sender = environment.get("SMTP_USER")
    if not sender and sender_file.is_file():
        sender = sender_file.read_text(encoding="utf-8").strip()
    if not sender:
        raise RuntimeError(f"Set up the sender once in Android Studio Terminal: {SETUP_COMMAND}")
    environment["SMTP_USER"] = email_address(sender)
    if not environment.get("SMTP_PASSWORD"):
        result = subprocess.run([
            "/usr/bin/security", "find-generic-password", "-a", sender,
            "-s", KEYCHAIN_SERVICE, "-w",
        ], capture_output=True, text=True)
        if result.returncode != 0 or not result.stdout.strip():
            raise RuntimeError(f"Unable to read the Gmail app password. Run {SETUP_COMMAND}.")
        environment["SMTP_PASSWORD"] = result.stdout.strip()
    return environment


def start():
    with (STATE_DIR / "start.lock").open("a") as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        if server_is_ready():
            print("Grabbit email server is already running.")
            return
        environment = server_environment()
        log_path = STATE_DIR / "server.log"
        with log_path.open("a", encoding="utf-8") as log:
            process = subprocess.Popen([sys.executable, str(SERVER_FILE)], env=environment,
                                       stdin=subprocess.DEVNULL, stdout=log,
                                       stderr=subprocess.STDOUT, start_new_session=True)
        ready = False
        try:
            deadline = time.monotonic() + 5
            while process.poll() is None and time.monotonic() < deadline:
                if server_is_ready():
                    ready = True
                    print(f"Grabbit email server started. Log: {log_path}")
                    return
                time.sleep(0.1)
            raise RuntimeError(f"The email server did not start. See {log_path}.")
        finally:
            if not ready and process.poll() is None:
                process.terminate()


def main():
    STATE_DIR.mkdir(mode=0o700, parents=True, exist_ok=True)
    try:
        if sys.argv[1:] == ["--configure"]:
            configure()
        elif not sys.argv[1:]:
            start()
        else:
            raise SystemExit("Usage: python3 -B email_server/start.py [--configure]")
    except VerificationError:
        raise SystemExit("Enter a valid Gmail sender address.") from None
    except subprocess.CalledProcessError:
        raise SystemExit("Could not save the credentials in Keychain.") from None
    except (OSError, RuntimeError, EOFError) as error:
        raise SystemExit(str(error)) from None


if __name__ == "__main__":
    main()
