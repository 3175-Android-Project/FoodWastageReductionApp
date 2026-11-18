# Account and profile updates

This change uses Room database version 6. The 5→6 migration preserves the six existing tables, adds `users.profile_photo_uri`, and adds a separate table for short-lived password-reset grants. Database-open errors no longer trigger automatic database deletion.

## User-facing changes

- Login/logout clears shared Customer and Manager caches and the cart. Async work from an earlier session cannot publish data to a later session, including logout/login to the same account.
- Registration requires a street address as well as city, province, and postal code. A user photo is optional. Existing accounts can add missing details through Edit Profile.
- Customer: Profile → Edit Profile manages the user photo and full address. Manager: Profile shows Edit Profile → Change Password → Logout as matching rows. Edit Profile keeps the existing restaurant form. There is no separate Account Settings menu.
- Profile photos are copied into private app storage, resized to a maximum dimension of 512 px, and encoded as JPEG without copying metadata. Source files larger than 20 MB or invalid images are rejected. Saving does not require ongoing access to the original gallery file. Remove clears the account's photo selection.
- Edit Profile → Change password requires the current password, validates a six-character minimum and matching confirmation, saves a new salted hash, invalidates outstanding reset grants, and signs out. Passwords are not trimmed.
- Forgot password normalizes email and checks the local account before requesting a code. Only a successful server verification can produce the proof used to issue a reset grant. Grants are random, stored only as hashes, bound to one account, expire after five minutes, and are consumed in the same transaction as the password update. An email extra alone cannot open a usable reset form.
- Delivery address formatting includes street, city, province, and postal code.

## Verification

Build and static checks:

```sh
bash ./gradlew assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest
```

For repeatable email/UI tests without sending real email, run the test server in a separate terminal:

```sh
python3 -B email_server/run_test_server.py
```

It binds only to `127.0.0.1:8766`, never calls SMTP, and uses code `123456`. The emulator connects through `10.0.2.2`. Build and run the separate test application:

```sh
bash ./gradlew connectedDebugAndroidTest \
  -PgrabbitAccountTestBuild=true \
  -PgrabbitVerificationUrl=http://10.0.2.2:8766 \
  -Pandroid.testInstrumentationRunnerArguments.verificationTestCode=123456
```

The opt-in flag adds `.accounttest` to the debug application ID; ordinary builds retain the existing ID. `AccountFeaturesUiTest` requires this separate app. The email integration tests require a local URL/test code. The normal shared mail endpoint and existing app data are not used by this test setup.

Coverage includes existing signup/role routing and Room tests, cross-account cache invalidation, an in-flight callback during account switching, reset grants (expiry, account binding, reuse, concurrent consumption, and database reopening), unknown/uppercase email reset flows, current-password validation, profile photo import/removal/rotation, full addresses, manager Profile password navigation, and preservation of all six original table contents during migration.

The photo UI test delivers a standard gallery result through Android's activity-result registry. It does not depend on a particular device gallery app. Real SMTP/Apps Script delivery, physical-device photo pickers, and older Android versions need their own device checks.

The EXIF dependency follows the [AndroidX ExifInterface documentation](https://developer.android.com/jetpack/androidx/releases/exifinterface).
