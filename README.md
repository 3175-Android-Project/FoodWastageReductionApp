# Food Wastage Reduction App

**Course:** CSIS 3175 – Intro to Mobile Development  
**Platform:** Android (Java, XML Layouts, Material 3)

Welcome to the Food Wastage Reduction App project repository! This `README.md` serves as a practical guide for all team members. It outlines our project setup, design system, shared components, naming conventions, and development rules to ensure UI/UX consistency across all screens.

---

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Project Structure](#project-structure)
3. [Design System](#design-system)
4. [Shared Components](#shared-components)
5. [Naming Conventions](#naming-convention)
6. [Navigation](#navigation)
7. [Current Screens & References](#current-screens--references)
8. [How to Create a New Screen](#how-to-create-a-new-screen)
9. [Development Guidelines](#development-guidelines)
10. [Git Workflow](#git-workflow)

---

## 📌 1. Project Overview
The Food Wastage Reduction App connects customers with local food providers/managers to reduce food waste by offering surplus items. 

This repository contains the initial boilerplate setup, shared design system, navigation menus, and reference activities. Team members should follow this shared structure before implementing their assigned screens.

---

## 📁 2. Project Structure
Our codebase follows a feature-based Java package structure alongside standard Android resource organization:

```text
app/src/main/
├── java/com/project/foodwastagereductionapp/
│   ├── auth/         # Authentication activities (Login, Register, etc.)
│   ├── customer/     # Customer feature activities & fragments
│   └── manager/      # Manager feature activities & fragments
└── res/
    ├── drawable/     # Shared icons and background drawables
    ├── layout/       # XML layout files (Activities and reusable components)
    ├── menu/         # Bottom navigation menus
    ├── values/       # Colors, styles, dimensions, strings
    └── font/         # Custom typography files
```

> 💡 **Note on Resources:** Since Android resource folders follow standard organization, drawable and layout files are organized using clear naming prefixes instead of subfolders.

---

## 🎨 3. Design System

### Colors
Always reuse our defined semantic colors in `res/values/colors.xml`:
* **Backgrounds:** `background_white`, `background_primary`, `background_secondary`, `background_tertiary`, `background_accent1`, `background_accent2`, `background_warning`
* **Text:** `text_primary`, `text_secondary`, `text_tertiary`, `text_warning`, `text_inverse`

### Typography Styles
Use Material 3 text styles (`res/values/styles.xml`):
* Headlines: `@style/Text.HeadlineXL`, `@style/Text.HeadlineLg`, `@style/Text.HeadlineMd`, `@style/Text.HeadlineSm`
* Body: `@style/Text.BodyLg`, `@style/Text.BodyMd`, `@style/Text.BodySm`, `@style/Text.BodyXs`
* Other: `@style/Text.TabSm`

### Fonts
* **Pretendard Font Family:** `pretendard_regular`, `pretendard_semibold`, `pretendard_bold` (Do not use default Android system fonts).

### Dimensions & Spacing
* **Screen Padding:** Always use `@dimen/screen_padding` for consistent horizontal screen margins.
* **Spacing:** Use existing spacing dimensions (`space_xs`, `space_sm`, `space_md`, `space_lg`, etc.), standard button/input heights, and text size dimensions.

---

## 🧩 4. Shared Components
Before creating custom UI pieces, check if a shared component already exists:
* **Buttons:** `@style/Button.Primary`
* **Inputs:** `@style/Input.Default`
* **Cards:** `@style/Card.Default`
* **Badges:** `@style/Badge.Status`
* **Headers:** `component_header.xml` (Use this for screens that need a standard back button and page title header).

---

## 🏷️ 5. Naming Conventions
To keep our resource folders clean and organized, strictly follow these prefixes:

* **Drawables (`res/drawable/`):**
  * `bg_*` for background shapes and custom drawables (e.g., `bg_rounded_button.xml`)
  * `icon_*` for vector or bitmap icons (e.g., `icon_profile.xml`)
* **Layouts (`res/layout/`):**
  * `activity_*` for full-screen Activity layouts (e.g., `activity_customer_home.xml`)
  * `component_*` for reusable layout includes (e.g., `component_header.xml`)
* **Java Classes:**
  * Meaningful Activity names placed inside their respective package (`auth`, `customer`, or `manager`).

---

## 🧭 6. Navigation
We use standardized bottom navigation bars for Customer and Manager flows. Do not recreate bottom navigation menus; reuse the existing menu files (`menu_customer_bottom_nav.xml` and `menu_manager_bottom_nav.xml`).

### Customer Bottom Navigation:
1. **Home**
2. **Search**
3. **Orders**
4. **Profile**

### Manager Bottom Navigation:
1. **Home**
2. **Items**
3. **Orders**
4. **Profile**

---

## 📱 7. Current Screens & References
Before implementing new screens, review the existing example activities for layout structure, spacing, typography, bottom navigation wiring, and shared components:
* `MainActivity` / Landing Page
* `LoginActivity` (`auth/`)
* `CustomerHomeActivity` (`customer/`)
* `ManagerDashboardActivity` (`manager/`)

---

## 🛠️ 8. How to Create a New Screen
1. **Identify the Feature Package:** Place your Java Activity class in `com.project.foodwastagereductionapp.auth`, `customer`, or `manager`.
2. **Create the Layout XML:** Name it `activity_<feature_name>.xml` in `res/layout/`.
3. **Apply Design Standards:**
   * Set root layout padding using `app:layout_constraint...` or `@dimen/screen_padding`.
   * Use Pretendard fonts and `@style/Text.*` styles.
   * Include `component_header.xml` if a header is needed.
   * Include the standard BottomNavigationView if the screen is a main tab.
4. **Wire the Activity:** Initialize views, set up click listeners, and hook up bottom navigation handling.
5. **Test UI:** Verify your screen against Figma wireframes and run a quick build test.

---

## ⚠️ 9. Development Guidelines
1. **Do Not Duplicate Resources:** Never create new colors, custom fonts, button styles, input styles, or spacing values unless absolutely necessary. Check existing XML files first.
2. **Always Use Pretendard:** Ensure all text views use Pretendard font variants.
3. **Consistent Padding:** Use `@dimen/screen_padding` for horizontal screen margins.
4. **Reuse Headers & Nav:** Use `component_header.xml` for sub-screens and existing bottom nav menus for main tabs.
5. **Dummy Data is Fine:** For UI development and layout prototyping, hard-coded/sample data is completely acceptable. Do not implement database logic yet unless agreed upon by the team.
6. **Keep UI Consistent:** Match layouts closely with the Figma wireframes.

---

## 🔀 10. Git Workflow
* **Branches:** Create feature branches off `main` (e.g., `feature/customer-search`, `feature/manager-items`).
* **Commits:** Write clear, descriptive commit messages (e.g., `git commit -m "Add customer search layout and adapter"`).
* **Pull Requests:** Open a Pull Request for review by team members before merging into `main`.
* **Syncing:** Always pull the latest changes from `main` before starting new work to avoid merge conflicts.

---
*Happy coding! Let's reduce food waste together! 🍎🥦*
