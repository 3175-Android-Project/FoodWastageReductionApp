# Grabbit Room Integration

Maintains existing packages (`auth`, `customer`, `manager`), screen XMLs, and resource IDs.
Room is the persistence layer and uses SQLite underneath.

## Storage Structure

DB File: Internal app storage `databases/grabbit.db`, Room schema version 1.

- `data/AppDatabase.java`: Room database and database builder.
- `data/UserEntity.java`, `data/RestaurantEntity.java`: Room entities.
- `data/UserDao.java`, `data/RestaurantDao.java`: Room queries and writes.
- `users`: `id`, `email`, `password_hash`, `role`, `full_name`, `city`, `province`, `postal_code`, `email_verified`.
  - Emails are trimmed and normalized to lowercase, and case-insensitive `UNIQUE` constraints are applied at the database level.
  - Roles are restricted to `customer` or `manager`.
- `restaurants`: `id`, `manager_id`, `name`, `city`, `province`, `postal_code`, `phone`, `cuisine_type`, `description`.
  - `manager_id` is a `UNIQUE` foreign key referencing `users.id`, linking one restaurant per manager.
  - Restaurant saving is only permitted for verified managers, and resaving updates the existing restaurant row.
  - Deleting a user also deletes the associated restaurant. Foreign keys are enabled on every DB connection.

## File Roles

- `data/AppDatabaseHelper.java`: Application facade for account and restaurant operations.
- `data/User.java`, `data/Restaurant.java`: Read-only data models used across screens.
- `data/PasswordHasher.java`: Random salt + PBKDF2-HMAC-SHA1 (120,000 iterations, 256-bit) hash generation/verification. Supports `minSdk 24`.
- `auth/AuthTasks.java`: Executes Room/hashing tasks serially on a background thread while handling button double-clicks and finished screen callbacks.
- `auth/SessionManager.java`: Stores only the current account ID in `SharedPreferences`. Passwords are never stored in the session.
- `auth/AuthNavigator.java`: Determines target navigation based on role and verification status. Clears back stack history on login, registration completion, and logout.
- Connected Room storage, retrieval, and navigation across 6 existing auth screens, Manager Dashboard, and Customer Profile.
- `AndroidManifest.xml`: Removed `.manager.RestaurantInformationActivity` entry which lacked an actual class. Kept existing `.auth.RestaurantInformationActivity`.
- `strings.xml`: Added/updated error messages and local email verification copy while preserving existing layouts and IDs.

## Screen Flow

1. **SignUp**: Required fields, email format, minimum 6-character password with confirmation match, duplicate email check. Hashes password before passing to the next screen.
2. **ChooseRole**: Role selection. Preserves selection across configuration changes (screen rotation).
3. **AccountInformation**: Enters name/city/province/postal code and stores unverified account in `users`. Passes `userId` instead of password to subsequent screens.
4. **EmailVerification**: The verification service confirms the code. Clicking the button updates `email_verified = 1` through Room.
   - Customer → Login with prefilled email.
   - Manager → Session setup then RestaurantInformation.
5. **RestaurantInformation**: Loads account city/province/postal code from DB as editable default values. Name/city/province/postal code/phone/cuisine type are required; description is optional. Navigates to Dashboard only upon successful save.
6. **Login**: Verifies password using stored hash.
   - Unverified account → EmailVerification (resumes paused registration).
   - Verified Customer → Customer Home.
   - Verified Manager + No Restaurant → RestaurantInformation (resumes paused restaurant setup).
   - Verified Manager + Has Restaurant → Manager Dashboard.
7. **Dashboard / Profile**: Dashboard queries actual restaurant name; Customer Profile queries actual account details. Restaurant profile card in Dashboard allows viewing/editing restaurant info. Logout from Customer Profile or Manager Bottom Profile menu clears session and back stack history.

Accounts and restaurants persist across app restarts. The initial screen remains the existing landing page, allowing users to log in again. Uninstalling or clearing app data deletes the local database.

## Automated Tests

Uses the project's Gradle wrapper and Android SDK/JDK. Since `gradlew` execution permissions are not stored in the repository, use `sh` on macOS/Linux.

```sh
# APK build and password hash unit tests
sh ./gradlew :app:assembleDebug :app:testDebugUnitTest

# Connected DB and UI flow tests (requires emulator/device)
sh ./gradlew --no-configuration-cache :app:connectedDebugAndroidTest

# Static analysis
sh ./gradlew --no-configuration-cache :app:lintDebug
```

- `PasswordHasherTest`: Correct password/whitespace/Unicode, salt randomness, rejected invalid hashes.
- `AppDatabaseHelperTest`: Separate Room database save/re-open, email duplicates, malformed input, local verification status, role-based navigation, restaurant relationships/updates/transactions/foreign keys/account isolation.
- `AuthFlowTest`: End-to-end Customer/Manager UI flows with unique test accounts, screen recreation after role selection, invalid login, profile, restaurant re-editing, logout/re-login. Cleans up test accounts.

Initial runs may require downloading additional Android test and lint tools, so avoid `--offline` if caches are missing.

## Execution Results

- Debug APK build successful.
- JVM tests passed (3 added password tests + 1 sample).
- Connected tests passed on Pixel 3a / Android 12 (API 32) (10 DB, 2 UI flow, 1 sample).
- `lintDebug` passed with 0 errors. Warnings regarding legacy resource/layout versions remain.
- Discovered and fixed an exception during PBKDF2 verification with empty passwords by rejecting empty input upfront.

## Verification in Android Studio

- Register a new email/password as a Customer → Fill AccountInformation → Local Confirm → Login → Customer Home → Check name/email/address in Profile → Logout → Login again.
- Register another email as a Manager → Local Confirm → Check/modify restaurant address defaults → Save restaurant → Check restaurant name on Dashboard → Re-view/edit via Restaurant profile → Bottom Profile > Logout → Re-login lands on Dashboard.
- Verify that registering with an already used email (differing only by case) is rejected.
- Verify that logging in with an incorrect password prevents screen navigation.
- Close app before email verification → Login again → Returns to local verification screen.
- Close app before saving restaurant → Manager Login → Returns to restaurant setup screen.
- Close app and restart → Remains logged in with restaurant info preserved.
- Inspect `users`, `restaurants`, and `manager_id` foreign key relations in `grabbit.db` via App Inspection > Database Inspector (Emulator API 26+ recommended).

## Scope of Work

Email verification uses the configured verification service. Existing order/food data remains outside the Room schema and uses its existing demo stores.

The Room database currently uses destructive fallback for databases created by the previous `SQLiteOpenHelper` implementation or for missing migrations. Future Room version bumps should use explicit `Migration` objects to preserve user data.

Room references: [Room persistence library](https://developer.android.com/training/data-storage/room), [Room migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions).
