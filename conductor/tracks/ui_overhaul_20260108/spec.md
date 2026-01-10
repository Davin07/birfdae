# Specification: UI Overhaul & Functional Enhancements

## 1. Overview
This track involves a comprehensive redesign of the Birthday Reminder application's user interface and significant functional enhancements. The goal is to align the app with a specific set of visual references provided in the `reference/` directory, while ensuring design consistency (colors, typography, component styles) across all screens. Major functional changes include a new "Search" screen, a multi-step "Add Birthday" wizard with enhanced data entry, pinned birthdays on the home screen, and upgraded notification logic.

## 2. Goals
*   **Visual Alignment:** Implement the visual design found in the `reference/` directory for the List, Calendar, and Add Birthday screens.
*   **Standardization:** Unify the visual language, particularly correcting the inconsistent "Notification Settings" screen and standardizing the Bottom Navigation Bar.
*   **Functional Enhancement:** Introduce Search, Pinned Birthdays, "Add Birthday" wizard, and dynamic Zodiac/Age info.
*   **UX Refinement:** Elegant animations, haptic feedback, and proactive permission handling.
*   **Data Layer Update:** Schema updates for images, relationships, multiple notifications, and pinned status.

## 3. Scope & Requirements

### 3.1 Global Design System
*   **Theme:** Define a unified color palette and typography system derived from the references.
*   **Bottom Navigation:** 
    *   Standardized design matching `reference/calendar_view_*`.
    *   Destinations: Home, Calendar, Search, Settings.
    *   "Add Birthday" accessible via a prominent center button on the nav bar.
*   **Animations:** Implement elegant transitions and micro-interactions.
*   **Permissions:** Prompt for notification permissions immediately upon first app launch.

### 3.2 Screen-Specific Requirements

#### A. Home Screen (Upcoming Birthdays)
*   **Reference:** `reference/upcoming_birthdays_list_*`
*   **Features:**
    *   **New:** Pinned Birthdays.
        *   Users can pin specific birthdays.
        *   The pinned birthday appears at the very top as a larger card.
        *   **Fallback:** If no birthday is pinned, the *next upcoming* birthday takes this spot.
    *   Scrollable list of subsequent upcoming birthdays.

#### B. Search Screen (New)
*   **Features:**
    *   Search bar at the bottom.
    *   Filter by Name or Month.
    *   **Interaction:** Clicking a search result card opens the **Edit** flow for that birthday.

#### C. Calendar Screen
*   **Reference:** `reference/calendar_view_*`
*   **Features:**
    *   Month/Week view with birthday indicators.

#### D. Add Birthday Wizard (Refactored)
*   **Reference:** `reference/add_birthday_*`
*   **Flow:**
    *   **Step 1: Identity:** 
        *   Name input.
        *   **Relationship:** Selectable tabs (Options: "Family", "Friend", "Work", "Acquaintance").
        *   **Image:** Image/Avatar picker.
    *   **Step 2: Date:** 
        *   Date selection.
        *   **Dynamic:** Display Zodiac Sign and Current/Upcoming Age.
    *   **Step 3: Personalization:** 
        *   Notes input.
        *   **Notifications:** Multi-select preferences (e.g., "On day", "3 days prior").
        *   **Time:** Specific time selection for the notification.
        *   **Preview:** Live visual preview of the notification.

#### E. Notification Settings Screen
*   **Reference:** `reference/notification_settings` (Redesign).
*   **Features:**
    *   Modify *Default* Notification Time (global setting).
    *   Standardized visual design.

### 3.3 Data & Logic Layer
*   **Database:** Update Room schema for:
    *   `imageUri` (String/Nullable).
    *   `relationship` (String/Enum).
    *   `isPinned` (Boolean).
    *   `notificationConfig` (List of offsets).
    *   `notificationTime` (Time for specific birthday).
*   **Logic:**
    *   Zodiac calculation.
    *   Age calculation.
    *   Pinned item logic (Pinned > Next Upcoming).

## 4. Technical Constraints
*   **Framework:** Jetpack Compose (Kotlin).
*   **Orientation:** Portrait mode.

## 5. Success Criteria
*   Visual fidelity to references.
*   Functional "Add Birthday" wizard (Tabs, Image, Zodiac, Multi-notify).
*   Functional Search with Edit redirection.
*   Pinned Birthday logic functioning correctly on Home.
*   Database migration successful.
