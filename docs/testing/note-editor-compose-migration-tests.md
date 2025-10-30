# Note Editor Compose Migration - Integration Test Specification

## Overview
This document outlines the integration tests required before removing legacy XML code from `NoteEditorFragment`. Tests must verify that both the legacy XML path and new Compose path work correctly, ensuring no regressions during the migration.

## Test Environment Setup

### Test Configurations
- **API Levels**: 23, 26, 29, 31, 34
- **Screen Sizes**: Phone (small/normal/large), Tablet (7"/10")
- **Orientations**: Portrait, Landscape
- **Accessibility**: TalkBack enabled/disabled
- **Input Methods**: Touch, Hardware keyboard, D-pad navigation

### Test Modes
Each test should be executed in both modes:
1. **Legacy Mode**: XML-based views (if fallback still exists)
2. **Compose Mode**: Jetpack Compose UI (current default)

## Test Suites

### 1. Snackbar Anchoring Tests

#### Test 1.1: Snackbar Display with Toolbar Visible
**Objective**: Verify snackbar appears above toolbar when toolbar is shown
**Steps**:
1. Open note editor
2. Ensure toolbar is visible (check preferences)
3. Trigger snackbar (e.g., add note with no cards created)
4. Verify snackbar appears above bottom toolbar
5. Verify snackbar doesn't overlap toolbar buttons

**Expected Result**: Snackbar anchored correctly above toolbar

#### Test 1.2: Snackbar Display with Toolbar Hidden
**Objective**: Verify snackbar appears at bottom when toolbar is hidden
**Steps**:
1. Open note editor
2. Hide toolbar via preferences
3. Trigger snackbar
4. Verify snackbar appears at bottom of screen

**Expected Result**: Snackbar anchored to bottom of screen

#### Test 1.3: Snackbar Action Button Interaction
**Objective**: Verify snackbar action buttons remain clickable
**Steps**:
1. Trigger snackbar with action button
2. Tap action button
3. Verify action executes correctly
4. Verify snackbar dismisses appropriately

**Expected Result**: Action buttons work in both modes

---

### 2. Field Layout and Buttons Tests

#### Test 2.1: Field Rendering
**Objective**: Verify all note fields render correctly
**Steps**:
1. Open note editor with Basic note type (2 fields)
2. Verify both fields are visible
3. Switch to note type with many fields (e.g., 10+)
4. Verify all fields render and are scrollable

**Expected Result**: All fields display correctly

#### Test 2.2: Sticky Button Toggle
**Objective**: Verify sticky field button works
**Steps**:
1. Open note editor in "Add Note" mode
2. Enter text in first field
3. Tap sticky button for first field
4. Add note and return to note editor
5. Verify first field retains previous text
6. Verify sticky button shows active state

**Expected Result**: Sticky fields persist correctly

#### Test 2.3: Multimedia Button Interaction
**Objective**: Verify multimedia buttons open correct dialogs
**Steps**:
1. Open note editor
2. Tap multimedia button (attachment icon) on first field
3. Verify multimedia bottom sheet appears
4. Select image option
5. Verify image picker opens
6. Select/cancel and verify field state

**Expected Result**: Multimedia flow works correctly

#### Test 2.4: Field Focus and Highlighting
**Objective**: Verify focused field is highlighted
**Steps**:
1. Open note editor
2. Tap into first field
3. Verify field background changes to indicate focus
4. Tap into second field
5. Verify first field returns to normal, second field highlights

**Expected Result**: Focus indication works correctly

---

### 3. Image Occlusion Tests

#### Test 3.1: Image Occlusion Mode Detection
**Objective**: Verify image occlusion note type shows correct buttons
**Steps**:
1. Open note editor with image occlusion note type
2. In "Add Note" mode, verify "Select Image" and "Paste Image" buttons appear
3. In "Edit Note" mode, verify "Edit Occlusions" button appears

**Expected Result**: Correct buttons shown based on mode

#### Test 3.2: Image Selection Workflow
**Objective**: Verify image selection opens image occlusion editor
**Steps**:
1. Open note editor with image occlusion note type
2. Tap "Select Image" button
3. Select image from gallery/file picker
4. Verify image occlusion editor opens with selected image

**Expected Result**: Image occlusion workflow functions

#### Test 3.3: Image Paste Workflow
**Objective**: Verify clipboard image paste works
**Steps**:
1. Copy image to clipboard
2. Open note editor with image occlusion note type
3. Tap "Paste Image" button
4. Verify image occlusion editor opens with clipboard image

**Expected Result**: Clipboard paste functions

---

### 4. Note Type Selection Tests

#### Test 4.1: Note Type Dropdown Display
**Objective**: Verify note type selector shows all types
**Steps**:
1. Create multiple note types (Basic, Cloze, Custom)
2. Open note editor
3. Tap note type selector
4. Verify dropdown shows all note types
5. Verify current type is highlighted

**Expected Result**: All note types listed correctly

#### Test 4.2: Note Type Change with Empty Fields
**Objective**: Verify changing note type with empty fields
**Steps**:
1. Open note editor with Basic note type
2. Change to Cloze note type via selector
3. Verify fields update to Cloze note type fields
4. Verify toolbar shows cloze buttons

**Expected Result**: Note type changes cleanly

#### Test 4.3: Note Type Change with Field Content Preservation
**Objective**: Verify field content preserved when fields match
**Steps**:
1. Create Custom1 note type with fields: Front, Back, Extra
2. Create Custom2 note type with fields: Front, Extra, Notes
3. Open note editor with Custom1
4. Enter text in Front, Back, and Extra fields
5. Change to Custom2 note type
6. Verify Front and Extra fields retain their text
7. Verify Back content is lost (expected, no matching field)

**Expected Result**: Matching field names preserve content

#### Test 4.4: Note Type Change Listener
**Objective**: Verify note type changes trigger appropriate updates
**Steps**:
1. Open note editor
2. Change note type
3. Verify toolbar updates (cloze buttons appear/disappear)
4. Verify field count updates
5. Verify deck selector remains functional

**Expected Result**: All UI elements update correctly

---

### 5. Deck Selection Tests

#### Test 5.1: Deck Selector Display
**Objective**: Verify deck selector shows all decks
**Steps**:
1. Create multiple decks (nested and flat)
2. Open note editor
3. Tap deck selector
4. Verify dropdown shows all decks with proper hierarchy
5. Verify current deck is highlighted

**Expected Result**: All decks listed correctly

#### Test 5.2: Deck Selection Persistence
**Objective**: Verify selected deck is used when saving
**Steps**:
1. Open note editor
2. Select different deck from dropdown
3. Fill in note fields
4. Save note
5. Open card browser
6. Verify card appears in selected deck

**Expected Result**: Note saved to correct deck

#### Test 5.3: Deck Selection with Deck ID
**Objective**: Verify deck ID updates when deck selected
**Steps**:
1. Open note editor
2. Change deck selection
3. Save note
4. Verify `_deckId` in ViewModel updated
5. Verify note saved to correct deck ID in database

**Expected Result**: Deck ID synchronizes correctly

---

### 6. Tags Dialog Tests

#### Test 6.1: Tags Button Opens Dialog
**Objective**: Verify tags button opens tags dialog
**Steps**:
1. Open note editor
2. Tap "Tags" button (or "Add Tag" if no tags)
3. Verify tags dialog opens
4. Add/remove tags
5. Verify dialog closes and button updates

**Expected Result**: Tags dialog functions correctly

#### Test 6.2: Tag Persistence
**Objective**: Verify tags save with note
**Steps**:
1. Open note editor
2. Add tags via dialog
3. Save note
4. Reopen note
5. Verify tags display on button and in dialog

**Expected Result**: Tags persist correctly

#### Test 6.3: Tags Button Enable/Disable (Legacy)
**Objective**: Verify tags button disabled during note type change (if applicable)
**Steps**:
1. Open note editor in edit mode
2. Change note type
3. Verify tags button state (should be disabled during change)
4. Complete note type change
5. Verify tags button re-enabled

**Expected Result**: Button state managed correctly

---

### 7. Keyboard Shortcuts Tests

#### Test 7.1: Formatting Shortcuts
**Objective**: Verify Ctrl+B/I/U shortcuts work
**Steps**:
1. Open note editor with hardware keyboard
2. Focus on field
3. Type text and select it
4. Press Ctrl+B
5. Verify text becomes bold (`<b>text</b>`)
6. Repeat for Ctrl+I (italic) and Ctrl+U (underline)

**Expected Result**: Formatting shortcuts work

#### Test 7.2: Custom Button Shortcuts
**Objective**: Verify Ctrl+1-9 shortcuts trigger custom buttons
**Steps**:
1. Create custom toolbar buttons
2. Open note editor
3. Focus on field
4. Press Ctrl+1 (for button 1)
5. Verify button 1 formatting applied
6. Test Ctrl+0 for button 10

**Expected Result**: Number shortcuts work

#### Test 7.3: Deck Selection Shortcut
**Objective**: Verify Ctrl+D opens deck selector
**Steps**:
1. Open note editor
2. Press Ctrl+D
3. Verify deck selector dialog opens

**Expected Result**: Deck shortcut works

#### Test 7.4: Template Editor Shortcut
**Objective**: Verify Ctrl+L opens template editor
**Steps**:
1. Open note editor
2. Press Ctrl+L
3. Verify card template editor opens

**Expected Result**: Template shortcut works

---

### 8. Tab Order and Focus Navigation Tests

#### Test 8.1: Tab Navigation Through Fields
**Objective**: Verify Tab key moves between fields
**Steps**:
1. Open note editor with hardware keyboard
2. Focus on first field
3. Press Tab
4. Verify focus moves to second field
5. Continue tabbing through all fields

**Expected Result**: Tab order is logical

#### Test 8.2: Tab Navigation from Deck Selector (Android <O)
**Objective**: Verify tab from deck selector goes to first field
**Steps**:
1. Test on Android API 23-25 device
2. Open note editor
3. Focus on deck selector
4. Press Tab
5. Verify focus moves to first field

**Expected Result**: Focus chain works on older Android

#### Test 8.3: D-pad Navigation
**Objective**: Verify D-pad navigation works
**Steps**:
1. Use device with D-pad or emulator
2. Open note editor
3. Navigate with D-pad down
4. Verify focus moves between elements correctly

**Expected Result**: D-pad navigation works

---

### 9. Toolbar Functionality Tests

#### Test 9.1: Formatting Buttons
**Objective**: Verify toolbar formatting buttons work
**Steps**:
1. Open note editor
2. Focus on field and select text
3. Tap Bold button
4. Verify text becomes bold
5. Repeat for Italic, Underline buttons

**Expected Result**: All formatting buttons work

#### Test 9.2: Horizontal Rule Button
**Objective**: Verify horizontal rule insertion
**Steps**:
1. Open note editor
2. Focus on field
3. Tap horizontal rule button
4. Verify `<hr>` inserted at cursor

**Expected Result**: HR inserted correctly

#### Test 9.3: Heading Button
**Objective**: Verify heading dialog and insertion
**Steps**:
1. Open note editor
2. Focus on field
3. Tap heading button
4. Select H1 from dialog
5. Verify `<h1></h1>` inserted with cursor in middle

**Expected Result**: Heading insertion works

#### Test 9.4: Font Size Button
**Objective**: Verify font size dialog and insertion
**Steps**:
1. Open note editor
2. Focus on field
3. Tap font size button
4. Select size from dialog
5. Verify `<span style="font-size:...">` inserted

**Expected Result**: Font size works

#### Test 9.5: MathJax Button
**Objective**: Verify MathJax insertion and long-press
**Steps**:
1. Open note editor
2. Focus on field
3. Tap MathJax button (short press)
4. Verify `\( \)` inserted
5. Long-press MathJax button
6. Verify MathJax dialog opens with block/chemistry options

**Expected Result**: MathJax insertion works

#### Test 9.6: Cloze Buttons (Cloze Note Type)
**Objective**: Verify cloze insertion
**Steps**:
1. Open note editor with Cloze note type
2. Focus on field and select text
3. Tap cloze increment button
4. Verify `{{c1::text}}` inserted
5. Select more text and tap same cloze button
6. Verify `{{c1::text}}` inserted (same number)
7. Select more text and tap increment button
8. Verify `{{c2::text}}` inserted

**Expected Result**: Cloze numbering works correctly

#### Test 9.7: Custom Toolbar Buttons
**Objective**: Verify custom buttons work
**Steps**:
1. Create custom toolbar button with prefix/suffix
2. Open note editor
3. Tap custom button
4. Verify prefix/suffix inserted correctly
5. Long-press custom button
6. Verify edit dialog opens

**Expected Result**: Custom buttons work

---

### 10. Performance Tests

#### Test 10.1: Layout Inflation Time
**Objective**: Compare XML vs Compose render times
**Steps**:
1. Measure time to render note editor with 10 fields (XML mode if available)
2. Measure time to render same in Compose mode
3. Compare results

**Expected Result**: Compose performance acceptable (within 10% of XML or better)

#### Test 10.2: Memory Usage
**Objective**: Compare memory footprint
**Steps**:
1. Open note editor with many fields
2. Measure memory usage in both modes
3. Compare heap allocations

**Expected Result**: Compose memory usage acceptable

#### Test 10.3: Scroll Performance
**Objective**: Verify smooth scrolling with many fields
**Steps**:
1. Open note editor with 20+ fields
2. Scroll quickly through all fields
3. Measure frame drops

**Expected Result**: 60fps maintained during scroll

---

### 11. Accessibility Tests

#### Test 11.1: TalkBack Field Descriptions
**Objective**: Verify fields have proper content descriptions
**Steps**:
1. Enable TalkBack
2. Open note editor
3. Navigate to each field
4. Verify TalkBack announces field name and hint

**Expected Result**: All elements have descriptions

#### Test 11.2: TalkBack Button Interactions
**Objective**: Verify buttons are tappable with TalkBack
**Steps**:
1. Enable TalkBack
2. Open note editor
3. Navigate to each toolbar button
4. Double-tap to activate
5. Verify actions work correctly

**Expected Result**: All buttons work with TalkBack

#### Test 11.3: Switch Access Navigation
**Objective**: Verify switch access can navigate UI
**Steps**:
1. Enable Switch Access
2. Open note editor
3. Navigate through all elements
4. Verify all interactive elements are reachable

**Expected Result**: Full navigation with switch access

---

## Test Execution Plan

### Phase 1: Core Functionality (Week 1)
- Tests 1.1-1.3 (Snackbar)
- Tests 2.1-2.4 (Fields)
- Tests 4.1-4.4 (Note Type)
- Tests 5.1-5.3 (Deck)

### Phase 2: Advanced Features (Week 2)
- Tests 3.1-3.3 (Image Occlusion)
- Tests 6.1-6.3 (Tags)
- Tests 9.1-9.7 (Toolbar)

### Phase 3: Input Methods (Week 3)
- Tests 7.1-7.4 (Keyboard Shortcuts)
- Tests 8.1-8.3 (Tab Order)

### Phase 4: Performance & Accessibility (Week 4)
- Tests 10.1-10.3 (Performance)
- Tests 11.1-11.3 (Accessibility)

## Success Criteria
- 100% of tests pass in Compose mode
- If legacy XML mode still exists, 100% pass there too
- No regressions identified
- Performance metrics within acceptable ranges
- All accessibility requirements met

## Test Automation
Priority tests to automate with Espresso/Compose Testing:
1. Field rendering and text entry
2. Note type selection and field preservation
3. Deck selection and persistence
4. Toolbar button interactions
5. Keyboard shortcuts

Manual testing required for:
- Image occlusion workflows (file pickers)
- TalkBack interactions
- Hardware keyboard edge cases
- Device-specific issues

## Reporting
- Test results logged in GitHub issue comments
- Performance metrics tracked in separate document
- Accessibility audit results documented
- Regression report created if any issues found
