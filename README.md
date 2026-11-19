# Food Wastage Reduction App (GRABBIT)

**Course:** CSIS 3175 - Intro to Mobile Development
**Platform:** Android (Java, XML layouts, Material 3)
**Persistence:** Room / SQLite

Grabbit helps restaurants sell surplus food at a discount instead of discarding it. Restaurant managers publish items and process orders, while customers find nearby listings, place pickup or delivery orders, and track their status.

---

## Table of Contents

1. [Purpose](#purpose)
2. [Target Users](#target-users)
3. [Registration and Authentication](#registration-and-authentication)
4. [Customer Features](#customer-features)
5. [Restaurant Manager Features](#restaurant-manager-features)
6. [Password Reset](#password-reset)
7. [Implementation Status](#implementation-status)
8. [Known TODOs](#known-todos)
9. [Project Structure](#project-structure)
10. [Development Setup](#development-setup)
11. [Database Integration](#database-integration)
12. [Design and Navigation](#design-and-navigation)
13. [Testing](#testing)

---

## Purpose

The app supports three goals:

1. **Reduce waste:** edible surplus food reaches a customer instead of the bin.
2. **Recover revenue:** restaurants earn from food that would otherwise be a loss.
3. **Save money:** customers buy restaurant food below its original price.

### Similar applications

The concept follows established food-waste applications:

| Application | How it works | Feature adopted by Grabbit |
|---|---|---|
| Too Good To Go | Restaurants and shops sell surplus food for discounted collection before closing | Restaurant listings and time-limited pickup |
| Karma | Customers buy selected surplus items from restaurants and cafes | Item-level browsing with original and discounted prices |
| Olio | Neighbours and local shops share surplus food | Location/name-based local discovery |
| Phenix | Businesses connect unsold food with consumers and charities | Dedicated restaurant-manager tools |

Sources: Which? review of Too Good To Go, Olio and Karma; Silicon Canals on European food-waste startups; Sifted on food-waste app downloads.

In addition to pickup, Grabbit supports delivery selection and direct restaurant-manager control over order statuses and customer reminders.

---

## Target Users

The app has two account roles selected during registration:

| Role | User | Main need |
|---|---|---|
| Restaurant Manager | Restaurant, cafe or bakery owners and managers | List surplus items, set prices and quantities, and process orders |
| Customer | Students, families and environmentally or budget-conscious local users | Find discounted food, place orders and track fulfillment |

---

## Registration and Authentication

The welcome flow provides **Login** and **Register**.

### Registration

- Enter name, email, password and address information.
- Select a **Customer** or **Restaurant Manager** role.
- Email addresses are normalized and checked for uniqueness.
- Passwords are stored using a salted PBKDF2-HMAC-SHA1 hash.
- Email verification confirms the account before normal app access.
- Manager registration continues to restaurant information setup.

The optional profile-photo data model and profile-photo UI components exist, but profile photos are not currently collected during account creation.

### Login and profile management

- Login validates the email and password.
- The app routes verified customers to the customer home and verified managers to the manager dashboard.
- Customers and managers can view and update account information.
- Signed-in passwords can be changed by providing the current password.
- Logout clears the current session and returns to the welcome flow.
- For managers, the restaurant name and address come from the manager's restaurant record.

---

## Customer Features

### Find food

- Browse available restaurant listings.
- Search restaurants by name, city, province or cuisine type.
- Open a restaurant to view its available surplus-food items.
- View item name, description, original price, discounted price, quantity, pickup time and photo.

### Order

- Add multiple items to the cart from one restaurant.
- Increase or decrease quantities within the available stock.
- View subtotal, tax and total price.
- Choose pickup or delivery.
- Confirm and place an order.
- Cancel an order while it is still **Pending**.
- Delivery requires an address saved to the customer account.

### Follow up

- View current and past orders.
- Track the **Pending - Accepted - Preparing - Ready - Completed** flow.
- View rejected and cancelled orders.
- Open order details and customer-facing status screens.
- Receive in-app notifications for order status changes and reminders.

---

## Restaurant Manager Features

### Manage food items

- Add a surplus food item with name, description, category, original price, discounted price, quantity and available-until time.
- Edit existing items.
- Delete items.
- Mark items as available or unavailable.
- View all food items belonging to the manager's restaurant.

### Manage and track orders

- View pending, accepted/preparing and ready orders.
- Accept or reject pending orders.
- Move accepted orders to Preparing and then Ready.
- Complete ready orders as picked up or delivered.
- Send a ready-order reminder that appears in the customer's notifications.
- View order history and order details.

Stock is reserved when an order is placed and restored if the customer cancels or the manager rejects the order.

---

## Password Reset

Users can reset a password from the Login screen:

1. Select **Forgot password**.
2. Enter the registered email address.
3. The app verifies the account and sends a one-time email code.
4. Enter the code, new password and password confirmation.
5. The app validates the confirmation and minimum password length.
6. It updates the password hash and consumes the reset grant.
7. The user returns to Login and signs in with the new password.

The reset grant is short-lived and stored as a hash. Invalid, expired or already-used grants do not change the password.

---

## Implementation Status

The following features are currently implemented:

- Customer and restaurant-manager roles
- Login, registration and role-based navigation
- Email verification
- Password hashing and signed-in password changes
- Password reset codes and reset grants
- Customer and manager profile editing
- Restaurant information management
- Restaurant search
- Restaurant and surplus-food listings
- Cart, pickup/delivery checkout and stock reservation
- Order placement, cancellation and status history
- Manager accept/reject and status updates
- In-app notifications and pickup reminders
- Local Room database persistence
- Light/dark theme resources, edge-to-edge layout and shimmer loading states

---

## Known TODOs

The proposal requirements are not all complete yet. The following items remain to be implemented:

### Food validation and management

- [ ] Require a discounted price to be lower than the original price before saving or editing an item.
- [ ] Require the original price to be greater than zero.
- [ ] Require quantity to be at least `1`.
- [ ] Replace the manager-only **available-until time** field with a complete available date and time.
- [ ] Validate that available date/time are not in the past.
- [ ] Prevent a manager from adding a quantity of `0` or making a zero-stock item available.

### Customer search and filtering

- [ ] Add food-item search and filters for name, category and price.
- [ ] Add dedicated category and price controls to restaurant and browse screens.
- [ ] Add geolocation or an explicit nearby-distance search instead of text matching only.
- [ ] Combine customer city/location defaults with restaurant search and home recommendations.

### Manager search and filtering

- [ ] Add search and filters for manager food items by name, category and date.
- [ ] Add a dedicated order-history filter beyond the existing order list.

### Delivery and pickup

- [ ] Present a user-selected pickup/delivery time instead of deriving it only from the cart's available-until time.
- [ ] Support editing or confirming the delivery address during checkout.
- [ ] Add real pickup/delivery hand-off confirmation for external delivery workflows.

### Profiles

- [ ] Enable profile-photo selection in account registration.
- [ ] Enable profile-photo selection and removal in customer and manager profile editing.
- [ ] Make profile-photo permissions and storage behavior consistent across registration and editing.

### Notifications and backend

- [ ] Replace the demo order-status simulator with server-driven, real-time status updates.
- [ ] Add remote/push reminders rather than only in-app notification records.
- [ ] Validate password-reset delivery through a production email service and keep reset codes in secure server-side storage.
- [ ] Add live cross-device synchronization and authentication; current persistence is local to the installed app.

### Data and security

- [ ] Replace destructive Room fallback migration behavior with explicit version migrations that preserve existing data.
- [ ] Add backup policy and data-extraction rules that exclude sensitive account information.
- [ ] Expand automated coverage for food management, cart, checkout, cancellation and manager order workflows.

---

## Project Structure

```text
app/src/main/
├── java/com/project/foodwastagereductionapp/
│   ├── auth/             Registration, login, verification and password reset
│   ├── customer/         Customer home, browse, cart, checkout and orders
│   ├── data/             Room entities, DAOs, database and app data models
│   ├── manager/          Manager dashboard, food items and incoming orders
│   ├── profile/          Profile-photo storage and display helpers
│   └── ui/               Shared activity behavior and confirmation dialogs
└── res/
    ├── drawable/         Icons, images and custom backgrounds
    ├── layout/           Activities and reusable XML components
    ├── menu/             Customer and manager bottom navigation
    ├── values/           Colors, dimensions, strings, styles and themes
    └── xml/              Backup and data-extraction rules

email_server/
├── apps_script/          Shared HTTPS email verification service
└── server.py             Local Python verification server
```

---

## Development Setup

Requirements:

- Android Studio
- JDK 11
- Android SDK 36
- A physical device or emulator with API 24 or newer

Build and test:

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

On macOS/Linux, use `sh ./gradlew` if Gradle wrapper execute permissions are missing.

Email verification requires `grabbitVerificationUrl` in `gradle.properties` or `local.properties`. See [email_server/README.md](email_server/README.md) for local and shared HTTPS server setup.

---

## Database Integration

- Room uses SQLite under `databases/grabbit.db`.
- The database currently includes users, restaurants, food items, orders, order items, notifications and password-reset grants.
- Database writes execute off the main thread.
- Foreign keys are enabled for account, restaurant and order relationships.
- Email addresses are stored normalized and case-insensitive uniqueness is enforced.
- Session storage keeps only the current account ID; passwords are never stored in session preferences.
- The database is local to the app and is removed when app data is cleared or the app is uninstalled.

Detailed Room information is documented in [ROOM_INTEGRATION.md](ROOM_INTEGRATION.md).

---

## Design and Navigation

- Pretendard is used for regular, semibold and bold typography.
- Shared colors, dimensions and text styles are centralized in `app/src/main/res/values/`.
- `BaseActivity` provides global keyboard dismissal.
- Activities use edge-to-edge windows and system/IME inset handling.
- Customer and manager flows use role-specific bottom navigation.
- Tab switches finish the previous activity to avoid deep activity-stack nesting.
- Facebook Shimmer provides skeleton loading states.
- Cached data is reused for quick revisits while stale data is refreshed in the background.

---

## Testing

The project includes JVM and Android instrumentation tests.

```sh
# Build and local JVM tests
./gradlew :app:assembleDebug :app:testDebugUnitTest

# Connected database and UI tests (device/emulator required)
./gradlew --no-configuration-cache :app:connectedDebugAndroidTest

# Email-server tests
python3 -B -m unittest discover -s email_server -v
```

Representative test areas include password hashing, account and restaurant persistence, role isolation, login/navigation flows, profile updates and password-reset verification.
