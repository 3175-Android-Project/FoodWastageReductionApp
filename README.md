# Food Wastage Reduction App (GRABBIT)

**Course:** CSIS 3175 – Intro to Mobile Development  
**Platform:** Android (Java, XML Layouts, Material 3)

Welcome to the Food Wastage Reduction App (GRABBIT) project repository!

This `README.md` serves as a practical guide for all team members. It outlines our project setup, design system, shared components, naming conventions, and development rules to ensure UI/UX consistency across all screens.

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Project Structure](#project-structure)
3. [Design System & Shimmer Skeletons](#design-system--shimmer-skeletons)
4. [Shared Components & Base Activity](#shared-components--base-activity)
5. [Naming Conventions](#naming-conventions)
6. [Navigation & Edge-to-Edge](#navigation--edge-to-edge)
7. [Current Screens & References](#current-screens--references)
8. [Database Integration & Auto-Recovery](#database-integration--auto-recovery)
9. [How to Create a New Screen](#how-to-create-a-new-screen)
10. [Development Guidelines](#development-guidelines)
11. [Git Workflow](#git-workflow)

---

## Project Overview

The Food Wastage Reduction App connects customers with local restaurants to reduce food waste by offering discounted surplus food.

The app supports two main user roles:
- Customer
- Restaurant Manager

This repository contains the complete project implementation, shared design system, Room persistence layer, shimmer skeleton loading states, global keyboard dismissal, and navigation management.

---

## Project Structure

Our codebase follows a feature-based Java package structure alongside standard Android resource organization.

```text
app/src/main/
├── java/com/project/foodwastagereductionapp/
│   ├── auth/
│   │   └── Authentication & verification activities
│   │
│   ├── customer/
│   │   └── Customer activities (Home, Browse, Orders, Checkout, Profile)
│   │
│   ├── data/
│   │   └── Room database, entities, DAOs, helpers, and models
│   │
│   ├── manager/
│   │   └── Manager activities (Dashboard, Food Items, Profile)
│   │
│   └── ui/
│       └── BaseActivity, KeyboardUtils, and ConfirmationDialog
│
└── res/
    ├── drawable/
    │   └── Shared icons, skeleton backgrounds, and custom shapes
    │
    ├── layout/
    │   └── Activity layouts, reusable components, and shimmer skeletons
    │
    ├── menu/
    │   └── Customer and manager bottom navigation menus
    │
    ├── values/
    │   └── Colors, styles, dimensions, themes, and strings
    │
    └── font/
        └── Pretendard font files
```

---

## Design System & Shimmer Skeletons

### Colors & Typography
- **Semantic Colors:** Reuses colors defined in `colors.xml` (`background_white`, `background_primary`, `background_accent1`, `text_primary`, etc.).
- **Typography:** Uses **Pretendard** font (`@font/pretendard_regular`, `@font/pretendard_semibold`, `@font/pretendard_bold`) with standardized text styles (`@style/Text.HeadlineXL`, `@style/Text.BodyMd`, etc.).

### Shimmer Skeleton Loading & Smart Caching
- **Facebook Shimmer:** Integrated `com.facebook.shimmer:shimmer` for polished skeleton loading states mimicking actual cards (restaurants, orders).
- **Stale-While-Revalidate Caching:** 
  - Cold starts display the skeleton loading state with a minimum 350ms loading duration to prevent jarring flickering.
  - Subsequent tab visits (e.g. switching back and forth) use static memory caching to render cached data **instantly** without skeleton re-triggering while silently refreshing in the background.

---

## Shared Components & Base Activity

### Base Activity & Global Keyboard Dismissal
- **`BaseActivity`:** All primary input/form activities extend `BaseActivity`.
- **`KeyboardUtils`:** Automatically listens for touch events (`dispatchTouchEvent`). Tapping anywhere outside an active `EditText` dismisses the soft keyboard and clears focus globally across the app.

### Shared UI Components
- **Primary Button:** `@style/Button.Primary`
- **Text Input:** `@style/Input.Default`
- **Card:** `@style/Card.Default`
- **Status Badge:** `@style/Badge.Status` (Pending, Accepted, Ready, Completed, Rejected, Cancelled)
- **Common Header:** `component_header.xml` (used for sub-screens; back buttons are hidden on root tab screens).

---

## Navigation & Edge-to-Edge

- **Edge-to-Edge Support:** Enabled across all activities with robust window insets handling (`Math.max(bars.bottom, keyboard)` for IME keyboard padding).
- **Bottom Navigation:**
  - Separate menus for Customer (`menu_customer_bottom_nav.xml`) and Manager (`menu_manager_bottom_nav.xml`).
  - Includes a top border divider (`navDivider`, 1dp `background_secondary`) above the navigation bar.
  - Tab switching uses `finish()` and zero-duration transitions to prevent activity stack nesting.

---

## Current Screens & References

### Landing & Authentication
- `MainActivity`, `auth/LoginActivity`, `auth/SignUpActivity`, `auth/EmailVerificationActivity`, `auth/RestaurantInformationActivity`

### Customer Flow
- `customer/CustomerHomeActivity` (Home with nearby & ending soon lists)
- `customer/CustomerBrowseActivity` (Search & filter restaurants)
- `customer/CustomerOrderActivity` (Current & past orders with polling status simulation)
- `customer/CheckoutActivity` (Delivery / Pickup options, tax calculation, and order placement)
- `customer/CustomerProfileActivity` & `customer/EditProfileActivity` (Profile & address management)

### Manager Flow
- `manager/ManagerDashboardActivity` (Order status overview & quick actions)
- `manager/ManageFoodItemsActivity` & `manager/AddFoodItemActivity` (Surplus food item management)
- `manager/ManagerProfileActivity` (Store profile & logout)

---

## Database Integration & Auto-Recovery

- **Room Persistence Layer:** SQLite underneath with foreign key constraints enabled.
- **Current Schema Version:** `4` (supports `users`, `restaurants`, `orders`, `order_items`, `notifications`, `food_items`).
- **Auto-Recovery Migration Handler:** `AppDatabase` wraps database building and migrations in a robust `try-catch` block. If any migration or schema discrepancy occurs, the app automatically deletes the incompatible database file and recreates it cleanly, preventing runtime crashes.
- **Thread Safety:** Database operations execute on a serial background executor via `AuthTasks`.

---

## Development Guidelines

1. **Do not duplicate resources.** Check `colors.xml`, `dimens.xml`, and `styles.xml` first.
2. **Use `BaseActivity`** for activities requiring global keyboard dismissal on outside touch.
3. **Use `@dimen/screen_padding`** for consistent horizontal margins.
4. **Reuse shared components** (`component_header.xml`, bottom navigation bars with dividers).
5. **Keep database work off the main thread** using `AuthTasks` and `AppDatabaseHelper`.

---

## Git Workflow

Always work on a feature branch (e.g., `git checkout -b feature/...`) and open a Pull Request before merging into `main`.

Happy coding! 🌱
