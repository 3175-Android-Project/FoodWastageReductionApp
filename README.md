# Food Wastage Reduction App

**Course:** CSIS 3175 – Intro to Mobile Development  
**Platform:** Android (Java, XML Layouts, Material 3)

Welcome to the Food Wastage Reduction App (GRABBIT) project repository!

This `README.md` serves as a practical guide for all team members. It outlines our project setup, design system, shared components, naming conventions, and development rules to ensure UI/UX consistency across all screens.

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Project Structure](#project-structure)
3. [Design System](#design-system)
4. [Shared Components](#shared-components)
5. [Naming Conventions](#naming-conventions)
6. [Navigation](#navigation)
7. [Current Screens & References](#current-screens--references)
8. [Database Integration](#database-integration)
9. [How to Create a New Screen](#how-to-create-a-new-screen)
10. [Development Guidelines](#development-guidelines)
11. [Git Workflow](#git-workflow)

---

## Project Overview

The Food Wastage Reduction App connects customers with local restaurants to reduce food waste by offering discounted surplus food.

The app supports two main user roles:

- Customer
- Restaurant Manager

This repository contains the initial project setup, shared design system, navigation menus, reusable components, and reference activities.

Before implementing assigned screens, team members should review the existing structure and reuse shared resources whenever possible.

---

## Project Structure

Our codebase follows a feature-based Java package structure alongside the standard Android resource organization.

```text
app/src/main/
├── java/com/project/foodwastagereductionapp/
│   ├── auth/
│   │   └── Authentication-related activities
│   │
│   ├── customer/
│   │   └── Customer-related activities
│   │
│   ├── data/
│   │   └── Room database, entities, DAOs, and data models
│   │
│   └── manager/
│       └── Manager-related activities
│
└── res/
    ├── drawable/
    │   └── Shared icons and background drawables
    │
    ├── layout/
    │   └── Activity layouts and reusable components
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

> **Note:** Android resource folders do not use custom subfolders such as `drawable/icons/` or `layout/customer/`.  
> Resources are organized using clear file naming prefixes instead.

---

## Design System

### Colors

Always reuse the semantic colors defined in `res/values/colors.xml`.

**Background colors**

- `background_white`
- `background_primary`
- `background_secondary`
- `background_tertiary`
- `background_accent1`
- `background_accent2`
- `background_warning`

**Text colors**

- `text_primary`
- `text_secondary`
- `text_tertiary`
- `text_warning`
- `text_inverse`

Avoid hard-coding hex colors directly in layout files unless absolutely necessary.

---

### Typography

Typography styles are defined in `res/values/styles.xml`.

**Headline styles**

- `@style/Text.HeadlineXL`
- `@style/Text.HeadlineLg`
- `@style/Text.HeadlineMd`
- `@style/Text.HeadlineSm`

**Body styles**

- `@style/Text.BodyLg`
- `@style/Text.BodyMd`
- `@style/Text.BodySm`
- `@style/Text.BodyXs`

**Tab style**

- `@style/Text.TabSm`

Example:

```xml
<TextView
    style="@style/Text.HeadlineMd"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Orders" />
```

---

### Fonts

The project uses **Pretendard**.

Available font resources:

- `@font/pretendard_regular`
- `@font/pretendard_semibold`
- `@font/pretendard_bold`

Do not use the Android default system font unless there is a specific reason.

Most shared text styles already include the correct Pretendard font, so reuse the existing styles instead of manually assigning fonts.

---

### Dimensions & Spacing

Use the values already defined in `res/values/dimens.xml`.

For horizontal screen padding:

```xml
@dimen/screen_padding
```

Spacing values include:

```text
space_xs
space_sm
space_md
space_lg
space_xl
space_xxl
```

Example:

```xml
android:layout_marginTop="@dimen/space_lg"
```

Avoid creating random values such as `17dp`, `21dp`, or `27dp` unless required by the Figma design.

---

## Shared Components

Before creating a new UI element, check whether a shared style or component already exists.

### Primary Button

```xml
@style/Button.Primary
```

Example:

```xml
<Button
    style="@style/Button.Primary"
    android:layout_width="0dp"
    android:text="Login"
    app:backgroundTint="@null" />
```

---

### Text Input

```xml
@style/Input.Default
```

Example:

```xml
<EditText
    style="@style/Input.Default"
    android:layout_width="match_parent"
    android:hint="Email" />
```

---

### Card

```xml
@style/Card.Default
```

Example:

```xml
<LinearLayout
    style="@style/Card.Default"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
</LinearLayout>
```

---

### Status Badge

```xml
@style/Badge.Status
```

Use this for order statuses such as:

- Pending
- Accepted
- Ready
- Picked Up
- Delivered

Warning/error statuses may use a separate warning style later if needed.

---

### Common Header

Reusable header:

```text
component_header.xml
```

Use this for screens that need:

- Back button
- Page title

Example:

```xml
<include
    android:id="@+id/header"
    layout="@layout/component_header"
    android:layout_width="0dp"
    android:layout_height="56dp"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

The title should be set inside the Activity.

Example:

```java
TextView txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
txtHeaderTitle.setText("Forgot Password");
```

---

## Naming Conventions

Follow these naming conventions to keep the project organized.

### Drawable Resources

Use:

```text
bg_*
```

for background drawables.

Examples:

```text
bg_card.xml
bg_primary_button.xml
bg_status_badge.xml
bg_text_input.xml
```

Use:

```text
icon_*
```

for icons.

Examples:

```text
icon_home.xml
icon_search.xml
icon_orders.xml
icon_profile.xml
icon_bell.xml
icon_map.xml
icon_right.xml
```

---

### Layout Files

Use:

```text
activity_*
```

for Activity layouts.

Examples:

```text
activity_login.xml
activity_customer_home.xml
activity_manager_dashboard.xml
```

Use:

```text
component_*
```

for reusable layouts.

Example:

```text
component_header.xml
```

---

### Java Classes

Use descriptive Activity names.

Examples:

```text
LoginActivity
CustomerHomeActivity
ManagerDashboardActivity
```

Place each Activity in the appropriate package:

```text
auth
customer
manager
```

---

## Navigation

The app uses separate bottom navigation menus for Customer and Manager screens.

Do not recreate the navigation menu for every Activity.

Reuse the existing menu files.

### Customer Bottom Navigation

File:

```text
menu_customer_bottom_nav.xml
```

Tabs:

1. Home
2. Search
3. Orders
4. Profile

---

### Manager Bottom Navigation

File:

```text
menu_manager_bottom_nav.xml
```

Tabs:

1. Home
2. Items
3. Orders
4. Profile

---

### Bottom Navigation Colors

The selected tab automatically uses the primary green color.

Unselected tabs use the secondary text color.

The shared color selector is:

```text
bottom_nav_item_color.xml
```

---

## Current Screens & References

The following screens have already been created and should be used as reference examples.

### Landing Page

```text
MainActivity
```

Reference for:

- Logo usage
- Primary buttons
- Basic screen layout
- Screen padding

---

### Login

```text
auth/LoginActivity
```

Reference for:

- Shared header
- Text inputs
- Primary button
- Click listeners
- Activity navigation

---

### Customer Home

```text
customer/CustomerHomeActivity
```

Reference for:

- Customer bottom navigation
- Search input
- Cards
- Horizontal content
- Scrollable layouts
- Notification icon

---

### Manager Dashboard

```text
manager/ManagerDashboardActivity
```

Reference for:

- Manager bottom navigation
- Dashboard cards
- Order status summary
- Reusable icons
- Manager layout structure

---

## Database Integration

The app uses Android Room as its persistence layer, with SQLite underneath. The implementation details and verification checklist are also documented in [`ROOM_INTEGRATION.md`](./ROOM_INTEGRATION.md).

### Storage

- Database file: internal app storage at `databases/grabbit.db`
- Current schema version: `2`
- Room database: `data/AppDatabase.java`
- Room entities and DAOs: `data/UserEntity.java`, `data/RestaurantEntity.java`, `data/UserDao.java`, `data/RestaurantDao.java`
- Application facade: `data/AppDatabaseHelper.java`
- Foreign-key constraints are enabled for every database connection.
- Room operations run through `AuthTasks` on a serial background executor, not the main UI thread.
- Uninstalling the app or clearing app data removes the local database.

### Schema

#### `users`

Stores customer and manager accounts.

| Column | Description |
| --- | --- |
| `id` | Auto-incrementing primary key |
| `email` | Required, trimmed/lowercased, case-insensitive unique value |
| `password_hash` | PBKDF2-HMAC-SHA1 hash; plaintext passwords are never stored |
| `role` | `customer` or `manager` |
| `full_name` | Required account name |
| `city`, `province`, `postal_code` | Required account address fields |
| `email_verified` | `0` or `1` |

#### `restaurants`

Stores the restaurant profile associated with a manager.

| Column | Description |
| --- | --- |
| `id` | Auto-incrementing primary key |
| `manager_id` | Required unique foreign key to `users.id` |
| `name`, `city`, `province`, `postal_code` | Required restaurant details |
| `phone`, `cuisine_type` | Required restaurant details |
| `description` | Optional description, stored as an empty string when omitted |
| `image_uri` | Optional local image URI |

Each manager can have at most one restaurant. Deleting a user cascades to the associated restaurant. `onUpgrade()` adds new columns while preserving existing data.

### Authentication and Password Flow

1. **Sign up:** validates email and password, generates a salted PBKDF2-HMAC-SHA1 hash using 120,000 iterations and a 256-bit output, then stores the account after profile information is completed.
2. **Email verification:** the app uses the email verification service to verify the address, then updates `users.email_verified` locally. The local demo verification flow is described in [`ROOM_INTEGRATION.md`](./ROOM_INTEGRATION.md).
3. **Login:** normalizes the email, verifies the password hash, and routes the user based on role, verification status, and restaurant setup.
4. **Forgot/reset password:** the verification service confirms ownership of the email address; the new password is hashed and saved through `updatePasswordByEmail()`.
5. **Session:** `SessionManager` stores only the current account ID in `SharedPreferences`; it never stores a password.

### Database Development Rules

- Use `AppDatabaseHelper.getInstance(context)` for the application database facade.
- Keep database and password work off the main thread by using `AuthTasks`.
- Put new queries in Room DAOs rather than accessing `SQLiteDatabase` from Activities.
- Never store plaintext passwords or commit verification URLs, credentials, or private keys.
- Add schema changes through `DATABASE_VERSION` and `onUpgrade()` migrations.
- Use a separate database name in tests so test accounts cannot affect local app data.
- Keep food listings, orders, and notifications as sample/demo data unless their persistence is explicitly added to Room.

The current Room integration persists accounts and restaurant profiles. Order, food-listing, and notification stores used by the UI are separate demo components and are not part of the Room schema yet.

---

## How to Create a New Screen

### Step 1: Choose the Correct Package

Place the Java Activity inside the appropriate package.

```text
auth
customer
manager
```

Example:

```text
customer/CustomerOrdersActivity.java
```

---

### Step 2: Create the Layout

Create the XML file in:

```text
res/layout/
```

Use the naming convention:

```text
activity_<feature_name>.xml
```

Example:

```text
activity_customer_orders.xml
```

---

### Step 3: Apply the Design System

Reuse existing resources.

For example:

```xml
android:background="@color/background_white"
```

```xml
android:paddingStart="@dimen/screen_padding"
android:paddingEnd="@dimen/screen_padding"
```

```xml
style="@style/Text.HeadlineMd"
```

```xml
style="@style/Button.Primary"
```

Do not create new colors or styles before checking whether an existing one can be reused.

---

### Step 4: Add the Shared Header if Needed

For sub-screens:

```xml
<include
    android:id="@+id/header"
    layout="@layout/component_header"
    android:layout_width="0dp"
    android:layout_height="56dp"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

Set the title in Java.

---

### Step 5: Add Bottom Navigation if Needed

Customer main screens should use:

```xml
app:menu="@menu/menu_customer_bottom_nav"
```

Manager main screens should use:

```xml
app:menu="@menu/menu_manager_bottom_nav"
```

---

### Step 6: Use Sample Data for Now

The authentication and restaurant-profile flows use Room as described in [Database Integration](#database-integration). For UI features that are not yet connected to Room, hard-coded sample data is acceptable, but keep it local to the screen or feature and label it as sample data.

Example:

```java
String restaurantName = "Awesome sushi";
String status = "Pending";
double price = 8.50;
```

If a new feature needs persistence, add an entity and DAO, then extend `AppDatabase` with an explicit migration instead of creating a second database or accessing SQLite directly from an Activity.

---

### Step 7: Test the Screen

Before submitting your work:

- Compare the screen with the Figma wireframe
- Check horizontal padding
- Check typography
- Check button and card styles
- Check icons
- Run the app
- Make sure there are no build errors

---

## Development Guidelines

1. **Do not duplicate resources.**  
   Check `colors.xml`, `dimens.xml`, `styles.xml`, and `drawable/` before creating something new.

2. **Reuse the existing design system.**  
   Buttons, inputs, cards, typography, colors, and spacing should stay consistent.

3. **Use Pretendard.**  
   Do not mix Android default fonts with Pretendard.

4. **Use `@dimen/screen_padding`.**  
   Keep horizontal margins consistent across screens.

5. **Reuse `component_header.xml`.**  
   Do not rebuild the same back-button header in every layout.

6. **Reuse the existing bottom navigation.**  
   Customer and Manager menus are already defined.

7. **Follow the Figma wireframes.**  
   Do not redesign screens unless the team agrees on a change.

8. **Use the existing Room integration for persisted account data.**
   Do not bypass `AppDatabaseHelper` or create a separate database for authentication or restaurant profiles.

9. **Hard-coded sample data is acceptable only for features not connected to Room.**
   Keep sample data separate from persisted account and restaurant data.

10. **Keep your code readable.**  
    Use meaningful IDs and Activity names.

Good:

```text
btnLogin
txtRestaurantName
editEmail
cardOrderHistory
```

Avoid:

```text
button1
textView7
layout3
```

---

## Git Workflow

### Create a Feature Branch

Do not develop directly on `main`.

Example:

```bash
git checkout -b feature/customer-search
```

Other examples:

```text
feature/auth-signup
feature/customer-orders
feature/customer-profile
feature/manager-items
feature/manager-orders
```

---

### Commit Your Work

Use clear commit messages.

Good:

```bash
git commit -m "Add customer search screen"
```

```bash
git commit -m "Implement manager food item layout"
```

Avoid vague messages such as:

```text
update
fix
test
changes
```

---

### Before Starting Work

Pull the latest version of `main`.

```bash
git checkout main
git pull
```

Then create or update your feature branch.

---

### Before Merging

Make sure:

- The project builds successfully
- Your screen runs correctly
- There are no unnecessary files
- Shared resources were reused
- The UI matches Figma

Then open a Pull Request for review before merging into `main`.

---

Happy coding! 🌱
```
