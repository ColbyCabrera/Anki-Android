# Settings Audit Report: Material 3 Migration

This report analyzes the functionality of various application settings in the context of the migration to Jetpack Compose and Material 3. It identifies settings that are functional, those that are currently non-functional ("dead"), and provides technical details on the underlying causes.

## Executive Summary

| Category | Status | Count | Notes |
| :--- | :--- | :--- | :--- |
| **Appearance** | ⚠️ Partial | - | Theme settings work; **Background Image** is broken. |
| **Reviewer** | ✅ Functional | - | Gestures and "Command" keys are correctly wired to the new Reviewer. |
| **Card Browser** | ⚠️ Partial | - | Basic search works; **"Display Filenames"** setting is ignored. |
| **Commands** | ✅ Functional | 30+ | "Command" keys (e.g., `answer_easy_command_key`) are functional. |
| **General** | ❓ Mixed | - | Many legacy keys appear unused but may be handled dynamically. |

---

## 1. Appearance Settings

### ✅ Functional: Themes
*   **Keys:** `app_theme_key`, `night_theme_key`, `day_theme_key`
*   **Status:** **Functional**
*   **Analysis:**
    *   The `Themes` object centrally manages theme application.
    *   It correctly respects the "Follow System" setting (`app_theme_key`) and switches between Day/Night themes accordingly.
    *   The Compose UI (`AnkiDroidTheme`) picks up these changes via the underlying AppCompat/Material context.

### ❌ Broken: Background Image
*   **Key:** `pref_deck_picker_background_key` (Internal: `deckPickerBackground`)
*   **Status:** **Broken / Non-Functional**
*   **Analysis:**
    *   **Logic Exists:** The `DeckPicker` composable (`DeckPickerScreen.kt`) has logic to display an image if `deckPickerPainter()` returns one.
    *   **Missing Wiring:**
        1.  **Key Mismatch:** The code reads a hardcoded preference key `"deckPickerBackground"`, but the XML defines `pref_deck_picker_background_key`.
        2.  **Missing File Generation:** The code looks for a specific file `DeckPickerBackground.png` in the app directory. The legacy "Image Picker" UI that allowed users to select and copy an image to this location appears to be disconnected or missing from the new Settings flow.
    *   **Result:** Users can toggle the setting, but no image will ever appear because the mechanism to set the image file is absent.

---

## 2. Reviewer Settings

### ✅ Functional: Gestures
*   **Key:** `gestures_preference`
*   **Status:** **Functional**
*   **Analysis:**
    *   The new `ReviewerFragment` (Compose-based) implements gesture support using a `BindingMap`.
    *   It intercepts touch events via a custom `WebViewClient` (`ReviewerWebViewClient`) which parses "gesture" scheme URLs.
    *   These are mapped to actions using the user's preferences stored under `gestures_preference`.

### ✅ Functional: Command Bindings
*   **Keys:** `answer_easy_command_key`, `undo_command_key`, `mark_command_key`, etc.
*   **Status:** **Functional**
*   **Analysis:**
    *   These keys (e.g., `binding_ANSWER_EASY`) are used to store custom key/gamepad bindings.
    *   The `ViewerAction` enum in the new Reviewer correctly maps these preference keys to actions (`ViewerAction.fromPreferenceKey`).
    *   The `BindingMap` loads these preferences to handle hardware key events (`dispatchKeyEvent`).

### ✅ Functional: "New Reviewer" Toggle
*   **Key:** `new_reviewer_options_key`
*   **Status:** **Functional**
*   **Analysis:**
    *   This master switch (`Prefs.isNewStudyScreenEnabled`) correctly routes the user between the Legacy Reviewer (`Reviewer.class`) and the New Reviewer (`ReviewerFragment.class`).

---

## 3. Card Browser Settings

### ❌ Broken: Display Filenames
*   **Key:** `pref_display_filenames_in_browser_key` (Mapped to `card_browser_show_media_filenames`)
*   **Status:** **Broken / Ignored**
*   **Analysis:**
    *   **Logic Missing in Compose:** In the legacy system, `BrowserMultiColumnAdapter` checked this preference and actively stripped media tags (e.g., `[sound:foo.mp3]`) from the display text if disabled.
    *   **Current State:** The new `CardBrowserViewModel` reads the preference into a property `showMediaFilenames`, but **does not apply it**.
    *   The Compose UI (`CardBrowserScreen`) renders the raw text received from the backend without any transformation or filtering.
    *   **Result:** Media filenames are always shown, regardless of the user's setting.

---

## 4. Unused / "Dead" Settings
The following keys exist in the XML resource files but have **0 references** in the Java/Kotlin code. These are likely candidates for removal.

*   `abort_command_key`
*   `browse_command_key`
*   `bury_card_command_key`
*   `bury_note_command_key`
*   `card_info_command_key`
*   `change_whiteboard_pen_color_command_key`
*   `clear_whiteboard_command_key`
*   `delete_command_key`
*   `edit_command_key`
*   `flag_blue_command_key` (and other flag colors)
*   `mark_command_key`
*   `page_down_command_key`
*   `page_up_command_key`
*   `play_media_command_key`
*   `record_voice_command_key`
*   `redo_command_key`
*   `remove_flag_command_key`
*   `replay_voice_command_key`
*   `save_voice_command_key`
*   `show_all_hints_command_key`
*   `show_answer_command_key`
*   `show_hint_command_key`
*   `statistics_command_key`
*   `suspend_card_command_key`
*   `suspend_note_command_key`
*   `tag_command_key`
*   `toggle_auto_advance_command_key`
*   `toggle_eraser_command_key`
*   `toggle_whiteboard_command_key`
*   `undo_command_key`

*Note: While "Command" keys are generally functional, the specific XML resource IDs listed above (e.g., `R.string.abort_command_key`) appear unused. The code likely constructs the preference key strings dynamically (e.g., `"binding_" + actionName`) rather than looking up these specific resource IDs.*

---

## Recommendations

1.  **Fix Background Image:** Re-implement the "Pick Image" functionality in the Appearance settings to copy the selected image to `DeckPickerBackground.png` and ensure the preference key matches `pref_deck_picker_background_key`.
2.  **Fix Browser Filenames:** Update `CardBrowserViewModel` to apply the text transformation (stripping media tags) to `browserRows` based on the `showMediaFilenames` state before emitting them to the UI.
3.  **Cleanup:** Remove the unused XML string resources identified in Section 4 to reduce clutter.
