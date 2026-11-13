# Grabbit email verification

Grabbit stores accounts and restaurants through Room, with SQLite underneath. Only the email address and verification request go to the mail service.

## Shared Google Apps Script service

Use the team's shared HTTPS endpoint from `gradle.properties` when configured. The app can then request a code directly from the hosted service.

To deploy or update the service, open the Apps Script project in the sender Gmail account, save `apps_script/Code.gs`, and deploy it as a **Web app**. Choose the sender account as the execution identity and **Anyone** for access. The owner authorizes the MailApp send permission. Put the web app's `/exec` URL in the root `gradle.properties` as `grabbitVerificationUrl`. Updating the existing deployment to a new version keeps the same URL.

If a code is missing, check the Spam folder. In Gmail, open the message and choose **Not spam** to move it to the inbox. Use the same email and password on Sign Up or Log in to resume an unfinished verification.

## Local Python server

Run the Python server on the same Mac as the Android Studio emulator. It keeps short-lived verification challenges in memory. To override a configured shared endpoint for a local test, pass `-PgrabbitVerificationUrl=http://10.0.2.2:8765` when building the debug app.

For automatic startup on macOS, run this once from the Android Studio project's Terminal:

```sh
python3 -B email_server/start.py --configure
```

Enter the sender Gmail address and app password. The password is saved in macOS Keychain. Add `:app:startEmailServer` to **Before launch** in the Android Studio `app` run configuration. Run starts the server when the emulator uses the local verification URL and reuses a running server. The background server log is `.gradle/grabbit-email/server.log`.

For a manual server session:

1. In Android Studio, open the project's Terminal and run:

   ```sh
   python3 email_server/server.py
   ```

2. Enter the sender Gmail address and its 16-character **app password** when prompted. The password input is hidden. For scripted use, provide `SMTP_USER` and `SMTP_PASSWORD` in the server process environment.
3. Check that the project's `local.properties` contains:

   ```properties
   grabbitVerificationUrl=http://10.0.2.2:8765
   ```

4. Run the **debug** app on the emulator. Register a new account with an inbox you can read. The first visit to **Verify your email** requests a code automatically. Enter the 6-digit code from your inbox and tap **Verify code**. Use **Resend code** to request another code. Keep the server terminal open while using the app.

The code lasts five minutes, can be used once, and is invalidated after five incorrect attempts. Resending is available after 60 seconds. Restarting the server clears pending codes. A user who leaves verification can log in again and request a fresh code. Accounts already confirmed in the local demo retain their saved state.

The app saves confirmation only after a successful server response. It shows a configuration message if the verification URL is empty. The debug app permits HTTP to `10.0.2.2`, which routes the emulator to the Mac's loopback interface. An external server URL uses HTTPS.

The server reports when Gmail accepts an email and distinguishes certificate errors from rejected credentials. On macOS it loads the system CA bundle when Python's default certificate store is empty.

For a shared Python server, run `python3 email_server/server.py` as one service instance. Set `EMAIL_SERVER_HOST=0.0.0.0`, the host's `PORT`, and the sender credentials `SMTP_USER` and `SMTP_PASSWORD` in the service settings. The host needs to provide HTTPS and allow outbound Gmail SMTP on port 587. Use `/healthz` for the health check. Keep one process because pending codes live in memory.

After deployment, set `grabbitVerificationUrl` to the shared HTTPS address in the project's `gradle.properties` to use it across the team. The Android app reads this address at build time. The shared server holds the sender credentials.

Run server tests without sending email:

```sh
python3 -B -m unittest discover -s email_server -v
```

References for the project report:

- [Google for Developers — Apps Script web apps](https://developers.google.com/apps-script/guides/web)
- [Google for Developers — MailApp](https://developers.google.com/apps-script/reference/mail/mail-app)
- [Gmail Help — Fix issues with valid emails in Spam](https://support.google.com/mail/answer/16457426)
- [Google Account Help — App passwords](https://support.google.com/accounts/answer/185833)
- [Google for Developers — Gmail SMTP settings](https://developers.google.com/workspace/gmail/imap/imap-smtp)
- [Python — smtplib](https://docs.python.org/3/library/smtplib.html)
- [Android Developers — Emulator network address space](https://developer.android.com/studio/run/emulator-networking-address)
- [Android Developers — Network security configuration](https://developer.android.com/privacy-and-security/security-config)
