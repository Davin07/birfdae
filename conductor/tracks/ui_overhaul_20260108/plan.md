# Implementation Plan: UI Overhaul & Functional Enhancements

This plan outlines the steps for a comprehensive UI redesign and functional expansion of the Birthday Reminder app, adhering to the project's TDD workflow and Material Design 3 principles.

## Phase 1: Data Layer & Core Utilities [checkpoint: 91cac4f]
This phase updates the database schema and implements the necessary logic for Zodiac signs, age calculations, and multi-notification scheduling.

- [x] Task: Update Room Database Schema (Image URI, Relationship, Pinned Status, Multi-notification config) dbf43f0
    - [ ] Create a migration for `BirthdayEntity` to include `imageUri` (String?), `relationship` (String), `isPinned` (Boolean), `notificationOffsets` (List<Int>), and `notificationTime` (String).
    - [ ] Update `BirthdayEntity` and `BirthdayRepository` to support new fields.
    - [ ] Write migration tests to ensure data persistence.
- [x] Task: Implement Zodiac and Age Calculation Logic 6a84da8
    - [ ] Write unit tests for `ZodiacUtils` and `AgeUtils`.
    - [ ] Implement `ZodiacUtils` to determine sign from Day/Month.
    - [ ] Implement `AgeUtils` to calculate current and upcoming age.
- [x] Task: Refactor Notification Scheduler for Multiple Alarms 4f8e75c
    - [ ] Write tests for scheduling multiple notifications per birthday based on offsets.
    - [ ] Update `AlarmScheduler` and `WorkManager` logic to handle the new `notificationOffsets`.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Data Layer' (Protocol in workflow.md)

## Phase 2: Design System & Foundation
Establishing the visual identity (colors, typography) and global UI components (Bottom Nav, Permissions).

- [ ] Task: Define Unified Theme & Design System
    - [ ] Create `LuminaTheme` with color palette and typography derived from `reference/`.
    - [ ] Implement reusable components (Cards, Buttons, Inputs) matching the reference style.
- [ ] Task: Implement Standardized Bottom Navigation
    - [ ] Redesign the bottom navigation bar matching the Calendar View reference.
    - [ ] Integrate the "faux FAB" center button for adding birthdays.
    - [ ] Write UI tests for navigation between top-level destinations.
- [ ] Task: Proactive Permission & Haptic/Animation Utilities
    - [ ] Implement immediate notification permission prompt on app launch.
    - [ ] Create utility for "force feedback" (haptics).
    - [ ] Set up global transition animations between screens.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Design System' (Protocol in workflow.md)

## Phase 3: Add Birthday Wizard
Implementing the multi-step wizard for adding and editing birthdays.

- [ ] Task: Wizard Step 1 - Identity (Name, Tabbed Relationship, Image)
    - [ ] Write tests for Relationship selection (tabs only).
    - [ ] Implement UI for Name input and Relationship tabs (Family, Friend, Work, Acquaintance).
    - [ ] Integrate Image Picker for celebrant avatar.
- [ ] Task: Wizard Step 2 - Date (Selection, Zodiac, Age)
    - [ ] Implement Date Picker matching reference.
    - [ ] Integrate dynamic Zodiac and Age display logic.
- [ ] Task: Wizard Step 3 - Personalization (Notes, Multi-Notifications, Preview)
    - [ ] Implement multi-select checkboxes for notification offsets (On day, 3 days prior, etc.).
    - [ ] Implement specific time selector for notifications.
    - [ ] Create the "Notification Preview" card as per references.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Wizard' (Protocol in workflow.md)

## Phase 4: Search & Navigation
Implementing the new Search screen and the redirection to Edit flow.

- [ ] Task: Implement Search Screen UI & Logic
    - [ ] Create Search screen with bottom-positioned search bar.
    - [ ] Implement filtering logic for Name and Month.
    - [ ] Write tests for search results and filtering.
- [ ] Task: Integrate Edit Flow via Search
    - [ ] Update navigation to open the Add Birthday Wizard in "Edit mode" when clicking a search result.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Search' (Protocol in workflow.md)

## Phase 5: Home Screen Redesign & Pinned Logic
Updating the main list and implementing the "Pinned Birthday" hero feature.

- [ ] Task: Redesign Birthday List Cards
    - [ ] Implement the new card design from `reference/upcoming_birthdays_list_*`.
    - [ ] Add haptic feedback and "fun" animations on card interactions.
- [ ] Task: Implement Pinned Birthday Logic
    - [ ] Write tests for the "Pinned > Next Upcoming" sorting/priority logic.
    - [ ] Implement the Hero card at the top of the list for Pinned/Upcoming.
- [ ] Task: Conductor - User Manual Verification 'Phase 5: Home Screen' (Protocol in workflow.md)

## Phase 6: Notification Settings & Final Polish
Redesigning settings and final visual synchronization.

- [ ] Task: Redesign Notification Settings Screen
    - [ ] Implement the ability to modify the Global Default Notification Time.
    - [ ] Align visuals with the rest of the application (correcting reference inconsistencies).
- [ ] Task: Global Polish & Final Integrity Check
    - [ ] Run full project integrity verification (Lint, Tests, Build).
    - [ ] Verify haptic feedback and animations across all user flows.
- [ ] Task: Conductor - User Manual Verification 'Phase 6: Final Polish' (Protocol in workflow.md)
