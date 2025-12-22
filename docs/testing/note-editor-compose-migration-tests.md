# Note Editor Compose Migration - Test Specification

## Overview

This document outlines the integration tests for the Compose-based `NoteEditorFragment`. The legacy XML code paths have been removed, so tests now focus exclusively on the Compose implementation.

> **Note**: The XML→Compose migration was completed on 2025-12-22. All legacy XML references have been removed.

## Test Environment Setup

### Test Configurations
- **API Levels**: 23, 26, 29, 31, 34
- **Screen Sizes**: Phone (small/normal/large), Tablet (7"/10")
- **Orientations**: Portrait, Landscape
- **Accessibility**: TalkBack enabled/disabled
- **Input Methods**: Touch, Hardware keyboard, D-pad navigation

---

## Core Test Suites

### 1. Note Creation and Editing

#### Test 1.1: Add New Note
**Steps**:
1. Open note editor in "Add Note" mode
2. Enter text in fields
3. Tap save
4. Verify note saved successfully

#### Test 1.2: Edit Existing Note
**Steps**:
1. Open note editor in "Edit Note" mode
2. Modify field text
3. Save changes
4. Verify changes persisted

#### Test 1.3: Discard Changes Dialog
**Steps**:
1. Open note editor
2. Make changes
3. Press back
4. Verify discard dialog appears
5. Test both "Discard" and "Keep Editing" options

---

### 2. Field Operations

#### Test 2.1: Field Rendering
- Verify all note type fields display correctly
- Test note types with many fields (10+)
- Verify scrolling works

#### Test 2.2: Sticky Fields
- Toggle sticky button
- Verify field content persists after adding note

#### Test 2.3: Multimedia Insertion
- Test image picker flow
- Test audio recording
- Test video selection
- Verify media inserted into field

#### Test 2.4: Field Focus
- Verify focused field is highlighted
- Test focus transitions between fields

---

### 3. Note Type Selection

#### Test 3.1: Change Note Type
- Verify dropdown shows all note types
- Test changing note type
- Verify fields update correctly
- Verify field content preserved for matching field names

#### Test 3.2: Cloze Note Type
- Switch to Cloze note type
- Verify cloze toolbar buttons appear
- Test cloze insertion

---

### 4. Deck Selection

#### Test 4.1: Select Deck
- Verify dropdown shows all decks
- Test selecting different deck
- Verify note saved to correct deck

---

### 5. Tags

#### Test 5.1: Tag Dialog
- Open tags dialog
- Add/remove tags
- Verify tags persist after save

---

### 6. Image Occlusion

#### Test 6.1: Image Occlusion Mode
- Open with image occlusion note type
- Verify "Select Image" and "Paste Image" buttons appear
- Test image selection workflow

#### Test 6.2: Edit Occlusions
- Open existing image occlusion note
- Verify "Edit Occlusions" button appears
- Test editing workflow

---

### 7. Toolbar

#### Test 7.1: Formatting Buttons
- Bold, Italic, Underline
- Headings, Font size
- Horizontal rule

#### Test 7.2: Cloze Buttons
- Same cloze (Ctrl+Shift+C)
- Increment cloze
- Verify numbering

#### Test 7.3: Custom Buttons
- Create custom button
- Verify prefix/suffix insertion
- Long-press to edit

---

### 8. Keyboard Shortcuts

| Shortcut     | Action          |
|--------------|-----------------|
| Ctrl+Enter   | Save            |
| Ctrl+D       | Deck selector   |
| Ctrl+L       | Template editor |
| Ctrl+Shift+T | Tags dialog     |
| Ctrl+Shift+C | Cloze           |
| Ctrl+P       | Preview         |
| Ctrl+1-9     | Custom buttons  |

---

### 9. Performance

- Field rendering with 10+ fields
- Scrolling performance (60fps target)
- Memory usage

---

### 10. Accessibility

- TalkBack field descriptions
- Button interactions with TalkBack
- Switch Access navigation

---

## Automated Test Commands

```bash
# Run unit tests
./gradlew :AnkiDroid:testPlayDebugUnitTest

# Run instrumentation tests
./gradlew :AnkiDroid:connectedPlayDebugAndroidTest
```

## Manual Testing Checklist

- [ ] Add new Basic note
- [ ] Add new Cloze note
- [ ] Edit existing note
- [ ] Change note type
- [ ] Change deck
- [ ] Add/edit tags
- [ ] Insert image via multimedia button
- [ ] Insert audio recording
- [ ] Preview card
- [ ] Use keyboard shortcuts
- [ ] Image occlusion workflow

---

*Last updated: 2025-12-22 - Compose-only implementation*
