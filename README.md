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
8. [Proposal Requirement Coverage](#proposal-requirement-coverage)
9. [Known TODOs](#known-todos)
10. [Project Structure](#project-structure)
11. [Development Setup](#development-setup)
12. [Database Integration](#database-integration)
13. [Design and Navigation](#design-and-navigation)
14. [Testing](#testing)

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
- A profile photo is optional in the project proposal. The app currently provides profile-photo data/UI support, but photo selection is not required for registration.

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

- Customer and restaurant-manager roles.
- Welcome, login and registration flows with role-based navigation.
- Name, email, password and address collection; email uniqueness and verification.
- Password hashing, signed-in password changes and logout.
- Password reset codes and short-lived reset grants.
- Customer and manager profile editing.
- Restaurant information setup and manager restaurant profile management.
- Restaurant search by name, city, province or cuisine type.
- Surplus-food listings with food details and photos.
- Add, edit, remove, view and mark availability of manager food items.
- Cart with quantity changes, subtotal, tax and total.
- Pickup/delivery checkout and stock reservation.
- Order placement, cancellation and order-status history.
- Pending, accepted, preparing, ready, completed, rejected and cancelled statuses.
- Manager acceptance/rejection, status updates and reminders.
- In-app customer notifications.
- Local Room database persistence on the Android device.
- Light/dark theme resources, edge-to-edge layout and shimmer loading states.

Password recovery and password verification are additional features included in the project. The email service is described in [email_server/README.md](email_server/README.md).

---

## Proposal Requirement Coverage

This section maps the supplied course proposal to the current implementation.

### Implemented proposal features

- Both required roles can register, log in and manage their account or restaurant profile.
- The welcome screen provides Login and Register.
- Manager food management supports adding, editing, deleting and viewing items.
- Customers can search restaurants, open restaurant listings and view discounted food details.
- Customers can select multiple items, choose pickup or delivery, place an order and view the total.
- Customers can cancel pending orders and view current and previous orders.
- Orders contain restaurant, customer, food-line, quantity, price, date, fulfillment method, status and total information.
- Managers can view orders, accept/reject them and update fulfillment statuses.
- Ready-order reminders are available as in-app notifications.
- The app uses Room with a SQLite database, as permitted by the course project.

### Proposal features still needing work

- Restaurant search uses text matching rather than geolocation or a defined nearby-distance search.
- Customer food-item search/filter controls for name, category and price are incomplete.
- Manager food-item search/filter controls for name, category and date are incomplete.
- Manager order-history filtering is not yet separated as a dedicated control.
- Food validation is incomplete:
  - Discounted price must be lower than original price.
  - Original price must be greater than zero.
  - Quantity must be at least 1.
  - Available date and time should replace the time-only field and reject past values.
- A user-selected pickup/delivery time is not yet distinct from the item's available-until time.
- Delivery address editing or confirmation during checkout should be completed.
- Real external pickup/delivery hand-off confirmation is not implemented.

### Optional items

- A profile photo is optional according to the proposal and is not a TODO requirement.
- Password recovery is a listed bonus/additional feature and is implemented locally with email verification infrastructure.

---

## Known TODOs

The proposal requirements are not all complete yet. The following items remain:

### Food validation and management

- [ ] Require the discounted price to be lower than the original price when adding or editing an item.
- [ ] Require the original price and quantity to be greater than zero.
- [ ] Replace the manager-only available-until time with a complete available date and time.
- [ ] Reject available date/time values that are in the past.
- [ ] Prevent a zero-stock item from being made available.

### Customer search and filtering

- [ ] Add dedicated food-item search and category/price filters.
- [ ] Add geolocation or an explicit nearby-distance search instead of text matching only.
- [ ] Combine customer city/location defaults with restaurant search and home recommendations.

### Manager search and filtering

- [ ] Add search and filters for manager food items by name, category and date.
- [ ] Add dedicated filters to the manager order-history screen.

### Delivery and pickup

- [ ] Present a user-selected pickup/delivery time instead of deriving it only from the cart's available-until time.
- [ ] Support editing or confirming the delivery address during checkout.
- [ ] Add real external pickup/delivery hand-off confirmation.

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
