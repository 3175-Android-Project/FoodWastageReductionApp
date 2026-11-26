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
4. [Demo Accounts and Seed Data](#demo-accounts-and-seed-data)
5. [Customer Features](#customer-features)
6. [Restaurant Manager Features](#restaurant-manager-features)
7. [Password Reset](#password-reset)
8. [Implementation Status](#implementation-status)
9. [Proposal Requirement Coverage](#proposal-requirement-coverage)
10. [Known TODOs](#known-todos)
11. [Project Structure](#project-structure)
12. [Development Setup](#development-setup)
13. [Database Integration](#database-integration)
14. [Design and Navigation](#design-and-navigation)
15. [Testing](#testing)

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

## Demo Accounts and Seed Data

For testing and demonstration, the app automatically seeds the database with pre-configured demo accounts, restaurants, surplus food items, and map location coordinates on first launch (or when the database is empty).

All demo accounts use the password: `123456`

### Customer Accounts

| Role | Email | Password | Name | City | Address |
|---|---|---|---|---|---|
| Customer | `customer@grabbit.demo` | `123456` | Demo Customer | Vancouver | 777 Robson Street |
| Customer | `customer2@grabbit.demo` | `123456` | Sally Customer | Burnaby | 4500 Kingsway |

### Manager Accounts & Restaurants

| Role | Email | Password | Restaurant Name | City | Cuisine |
|---|---|---|---|---|---|
| Manager | `manager@grabbit.demo` | `123456` | Sushi Barff | Vancouver | Japanese |
| Manager | `manager2@grabbit.demo` | `123456` | Italian Trattoria | Coquitlam | Italian |
| Manager | `manager3@grabbit.demo` | `123456` | Burnaby Burger Co. | Burnaby | Burgers |
| Manager | `manager4@grabbit.demo` | `123456` | Seoul Bistro | Richmond | Korean |

- **Surplus Food Items:** Each restaurant contains 2–4 pre-configured surplus food items with original & discounted prices, quantities, descriptions, and available-until times.
- **Location Coordinates:** Each demo restaurant includes map coordinates (latitude/longitude) for distance-based **Nearby** search and location discovery.
- **Auto-Seeding:** If app storage is cleared or the database is deleted, these demo accounts and listings are automatically re-populated upon launch.

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

- **User Roles & Auth:** Customer and restaurant-manager registration, login, email verification, PBKDF2 password hashing, and logout.
- **Password Reset:** Short-lived password reset grants and verification codes.
- **Geolocation & Nearby Search:** Real-time device location detection via GPS/Network, distance-based sorting (`LocationHelper`), and emulator fallback to New Westminster, BC.
- **City Selection:** Metro Vancouver city picker bottom sheet (`CityPickerBottomSheet`) with automatic province matching.
- **Demo Data & Image Seeding:** Automatic seeding of 4 demo restaurants (Sushi Barff, Italian Trattoria, Burnaby Burger Co., Seoul Bistro), surplus food items with photos, and demo accounts (`customer@grabbit.demo` / `manager@grabbit.demo` with password `123456`).
- **Surplus Food Listings:** Food item details, photos, original and discounted pricing, remaining quantity, and pickup times.
- **Manager Dashboard & Management:** Add, edit, delete, and mark availability for surplus food items, with image picker and storage.
- **Persistent Cart:** Room DB-backed shopping cart (`CartItemEntity`, `CartDao`, `CartManager`) persisted per user session.
- **Interactive Cart Steppers:** Slim 36dp pill stepper controls (`[-] count [+]`) in food item rows and cart views.
- **Checkout & Stock Reservation:** Pickup/delivery order placement, live stock verification, and tax calculations.
- **Order Lifecycle & Tracking:** Full order status flow (**Pending → Accepted → Preparing → Ready → Completed**), cancellation, and rejection stock restoration.
- **Manager Order Processing:** Accept/reject orders, update fulfillment statuses, and send ready pickup reminders.
- **In-App Notifications:** Customer status notifications and manager order alerts.
- **Local Persistence & Security:** Room SQLite database with non-destructive migrations (v1 → v8), foreign key constraints, and session isolation.
- **UI/UX Refinements:** Material 3 styling, Facebook Shimmer skeleton loading screens, custom bottom sheets, Pretendard typography, and edge-to-edge layout.

---

## Proposal Requirement Coverage

This section maps the supplied course proposal to the current implementation.

### Implemented proposal features

- Both required roles can register, log in and manage their account or restaurant profile.
- The welcome screen provides Login and Register.
- Manager food management supports adding, editing, deleting and viewing items with photo support.
- Customers can search restaurants, filter by location/cuisine, open restaurant listings, and view discounted food details.
- Geolocation and distance-based "Nearby" discovery are fully implemented with Metro Vancouver location defaults.
- Customers can select multiple items, adjust quantity with interactive steppers, choose pickup or delivery, place an order, and view total pricing.
- Customers can cancel pending orders and view current and previous orders.
- Orders contain restaurant, customer, food-line, quantity, price, date, fulfillment method, status, and total information.
- Managers can view orders, accept/reject them, update fulfillment statuses, and send pickup reminders.
- Ready-order reminders and order status changes are delivered via in-app notifications.
- The app uses Room with SQLite for local persistence, cart state, and non-destructive database migrations.

---

## Known TODOs

All proposal requirements and enhancements have been completed:

### Food validation and management

- [x] Require the discounted price to be lower than the original price when adding or editing an item.
- [x] Require the original price and quantity to be greater than zero.
- [x] Replace the manager-only available-until time with a complete available date and time.
- [x] Reject available date/time values that are in the past.
- [x] Prevent a zero-stock item from being made available.

### Customer search and filtering

- [x] Add dedicated food-item search and category/price filters.
- [x] Add geolocation or an explicit nearby-distance search (`LocationHelper`).
- [x] Combine customer city/location defaults with restaurant search and home recommendations.

### Manager search and filtering

- [x] Add search and filters for manager food items by name, category and date.
- [x] Add dedicated filters to the manager order-history screen.

### Delivery and pickup

- [x] Present a user-selected pickup/delivery time.
- [x] Support editing or confirming the delivery address during checkout.
- [x] Add real external pickup/delivery hand-off status tracking and reminders.

### Data and security

- [x] Replace destructive Room fallback migration behavior with explicit version migrations that preserve existing data.
- [x] Add backup policy and data-extraction rules that exclude sensitive account information.
- [x] Expand automated coverage for food management, cart, checkout, cancellation and manager order workflows.

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
