---
name: Complete NoteEditorFragment XML→Compose Migration
about: Track the complete removal of legacy XML code paths from NoteEditorFragment
title: '[MIGRATION] Complete NoteEditorFragment XML→Compose migration and remove legacy code'
labels: 'compose, migration, refactoring, note-editor'
assignees: ''
---

## Overview
Complete the migration of `NoteEditorFragment` from XML-based Views to Jetpack Compose by removing all legacy XML code paths and commented-out references.

## Background
The core Compose UI is now functional (toolbar, fields, deck/notetype selectors, tags/cards buttons), but legacy XML references remain as comments and fallback code throughout the file.

## Areas Requiring Cleanup

### 1. Snackbar Anchoring (~Line 489-493)
- [ ] Migrate snackbar anchor from XML toolbar to Compose scaffold
- [ ] Test snackbar positioning with and without toolbar visible
- [ ] Remove commented `anchorView` XML reference

**File:** `NoteEditorFragment.kt:489-493`
```kotlin
override val baseSnackbarBuilder: SnackbarBuilder = {
    // TODO: Re-implement with Compose
    // if (sharedPrefs().getBoolean(PREF_NOTE_EDITOR_SHOW_TOOLBAR, true)) {
    //     anchorView = requireView().findViewById<Toolbar>(R.id.editor_toolbar)
    // }
}
```

### 2. Field/Tags/Cards Button References (~Line 840-851)
- [ ] Remove commented XML view lookups for fields container, tags button, cards button
- [ ] Verify all functionality now handled by Compose
- [ ] Clean up related commented code in `setupEditor()`

**File:** `NoteEditorFragment.kt:840-851`
```kotlin
// TODO: These XML view references need to be migrated to Compose/ViewModel
// fieldsLayoutContainer = requireView().findViewById(R.id.CardEditorEditFieldsLayout)
// tagsButton = requireView().findViewById(R.id.CardEditorTagButton)
// cardsButton = requireView().findViewById(R.id.CardEditorCardsButton)
```

### 3. Note Type and Deck Selector XML Comments (~Line 963-966)
- [ ] Remove commented note type spinner setup code
- [ ] Verify deck selector fully migrated
- [ ] Clean up related initialization code

**File:** `NoteEditorFragment.kt:963-966`
```kotlin
// TODO: Note type and Deck selectors are now in Compose - remove these XML references
// Note type Selector
// noteTypeSpinner = requireView().findViewById(R.id.note_type_spinner)
```

### 4. Tags Button and Note Type Listeners (~Line 1032-1040)
- [ ] Remove commented note type spinner listener assignment
- [ ] Remove commented tags button click listener
- [ ] Verify all handled through Compose callbacks

**File:** `NoteEditorFragment.kt:1032-1040`
```kotlin
// TODO: Note type selector is now in Compose
// noteTypeSpinner!!.onItemSelectedListener = EditNoteTypeListener()

// TODO: Tags button is now in Compose
// requireView().findViewById<View>(R.id.CardEditorTagButton).setOnClickListener {
//     Timber.i("NoteEditor:: Tags button pressed... opening tags editor")
//     showTagsDialog()
// }
```

### 5. Tab Order for Deck Spinner (~Line 2080-2083)
- [ ] Handle tab order/focus navigation in Compose
- [ ] Test keyboard navigation on Android <O
- [ ] Remove XML focus chain references

**File:** `NoteEditorFragment.kt:2080-2083`
```kotlin
if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
    if (i == 0) {
        requireView().findViewById<View>(R.id.note_deck_spinner).nextFocusForwardId = newEditText.id
    }
    // ...
}
```

### 6. Note Type Change Listener (~Line 2972-2983, 3027-3030)
- [ ] Remove `EditNoteTypeListener` inner class
- [ ] Remove commented tags button enable/disable logic
- [ ] Verify note type changes fully handled through ViewModel

**File:** `NoteEditorFragment.kt:2972-2983, 3027-3030`
```kotlin
private inner class EditNoteTypeListener : OnItemSelectedListener {
    // ...
}

// TODO: Tags button is now in Compose - handle enabling/disabling through ViewModel
// requireView().findViewById<View>(R.id.CardEditorTagButton).isEnabled = false
```

## Testing Requirements

### Integration Tests
- [ ] Test snackbar anchoring in both legacy XML and Compose modes
- [ ] Test field layout rendering and sticky button toggling
- [ ] Test multimedia button interactions
- [ ] Test image occlusion button workflows
- [ ] Test note type selection and field preservation
- [ ] Test deck selection and ID persistence
- [ ] Test tags dialog integration
- [ ] Test keyboard shortcuts (Ctrl+1-9, Ctrl+D, Ctrl+L, etc.)
- [ ] Test tab order and focus navigation

### Compatibility Testing
- [ ] Test on Android API 23-34
- [ ] Test on different screen sizes and orientations
- [ ] Test with TalkBack accessibility
- [ ] Test with hardware keyboards
- [ ] Test with Android <O focus handling

### Performance Testing
- [ ] Benchmark layout inflation times (XML vs Compose)
- [ ] Measure memory usage
- [ ] Test with large notes (many fields)
- [ ] Test with many custom toolbar buttons

### Regression Testing
- [ ] Cloze deletion insertion (same/increment)
- [ ] Multimedia field insertion (audio, video, images)
- [ ] Image occlusion workflows
- [ ] Sticky field persistence
- [ ] Note type field mapping
- [ ] Tag editing and persistence
- [ ] Deck selection and saving

## Migration Timeline

### Phase 2 (Current - Q4 2025): Integration Tests
- Write comprehensive integration tests covering both code paths
- Document test coverage and edge cases
- Set up CI to run both legacy and Compose test suites

### Phase 3 (Q1 2026): Remove Legacy Code
- Remove all commented XML references
- Remove legacy field container and toolbar XML layouts
- Clean up conditional fallback code
- Update documentation

### Phase 4 (Q2 2026): Full Compose Adoption
- Remove feature flags/preferences guarding Compose
- Archive legacy layout XML files
- Final performance and accessibility audit
- Update developer documentation

## Acceptance Criteria
- [ ] All TODO comments referencing XML views removed
- [ ] All commented XML view lookups removed
- [ ] All legacy XML layout files archived or removed
- [ ] Integration tests passing for all core workflows
- [ ] No regressions in functionality
- [ ] Performance benchmarks meet targets
- [ ] Accessibility audit passed
- [ ] Code review approved

## Related Issues
- Related to toolbar migration
- Related to ViewModel state management improvements
- Blocks: Material 3 design system full adoption

## Additional Context
See comprehensive migration plan in `NoteEditorFragment.kt` header comments (lines 19-69).
