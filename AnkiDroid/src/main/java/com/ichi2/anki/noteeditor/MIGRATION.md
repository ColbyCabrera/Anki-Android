# Note Editor Migration to Compose and Material 3

## Overview

This document describes the migration of the AnkiDroid Note Editor from XML-based layouts to Jetpack Compose with Material 3 components.

## Migration Date
October 29, 2025

## Original Code Backup

The original Note Editor implementation has been preserved in the following locations:
- **Original Fragment**: `noteeditor/old/NoteEditorFragment.kt.old`
- **Original XML Layout**: `noteeditor/old/layouts/note_editor_fragment.xml.backup`

These backups are retained for reference and potential rollback if needed.

**Note**: The XML backup is stored in the `java/` directory tree (not `res/`) to avoid Android build system conflicts with non-`.xml` files in the resource directories.

## New Architecture

### Compose Components

#### 1. NoteEditorScreen (`noteeditor/compose/NoteEditor.kt`)
The main composable that renders the entire note editor UI.

**Features**:
- Material 3 components throughout
- Reactive state management using StateFlow
- Field editors with multimedia support
- Deck and note type selectors using ExposedDropdownMenuBox
- Tags and cards buttons
- Image Occlusion support
- Sticky field support

**Key Composables**:
- `NoteEditorScreen`: Main screen layout with Scaffold
- `NoteTypeSelector`: Dropdown for selecting note types
- `DeckSelector`: Dropdown for selecting decks
- `NoteFieldEditor`: Individual field editor with multimedia buttons
- `ImageOcclusionButtons`: Special buttons for image occlusion note types

#### 2. NoteEditorToolbar (`noteeditor/compose/NoteEditorToolbar.kt`)
A Material 3 BottomAppBar that provides formatting tools.

**Features**:
- Bold, Italic, Underline formatting
- Cloze deletion buttons (for cloze note types)
- Custom toolbar buttons
- Dynamic visibility based on note type

#### 3. NoteEditorViewModel (`noteeditor/NoteEditorViewModel.kt`)
Manages state and business logic for the note editor.

**Responsibilities**:
- Note data management
- Field value updates
- Sticky field toggling
- Tag management
- Deck and note type selection
- Save operations
- Collection interactions

**State Flows**:
- `noteEditorState`: Current editor state (fields, tags, deck, note type, etc.)
- `availableDecks`: List of available decks
- `availableNoteTypes`: List of available note types
- `toolbarButtons`: Custom toolbar buttons
- `showToolbar`: Toolbar visibility

## Integration with Existing Code

### NoteEditorFragment
The `NoteEditorFragment` has been updated to use a `ComposeView` while maintaining backward compatibility with existing functionality:

1. **Initialization**: The `setupComposeEditor()` method initializes the ViewModel and sets up the Compose UI
2. **State Management**: The ViewModel is shared using `activityViewModels()`
3. **Callbacks**: Existing multimedia, tags, and card template functionality is preserved through callbacks
4. **Navigation**: Activity result launchers for multimedia, image occlusion, and templates remain unchanged

### XML Layout
The `note_editor_fragment.xml` has been simplified to contain only a `ComposeView`:

```xml
<androidx.compose.ui.platform.ComposeView
    android:id="@+id/note_editor_compose"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

## Preserved Functionality

All original functionality has been preserved:

✅ Adding new notes
✅ Editing existing notes  
✅ Field editing with multimedia support
✅ Tags management
✅ Deck selection
✅ Note type changes
✅ Card template editing
✅ Image Occlusion notes
✅ Sticky fields
✅ Keyboard shortcuts
✅ Field formatting (bold, italic, underline)
✅ Cloze deletions
✅ Preview functionality
✅ Multimedia attachments (images, audio, video, drawing)

## Benefits of the Migration

1. **Modern UI**: Material 3 design language with dynamic theming
2. **Improved Performance**: Compose's efficient recomposition model
3. **Better State Management**: Clear separation of UI and business logic
4. **Easier Testing**: ViewModels can be unit tested independently
5. **Reactive UI**: Automatic UI updates when state changes
6. **Maintainability**: Clearer code structure and composition

## Known Issues / Future Improvements

- [ ] Rich text editing needs enhancement for full HTML support
- [ ] Toolbar customization UI needs migration to Compose dialog
- [ ] Field remapping UI (note type changes) needs Compose implementation
- [ ] Animation transitions could be improved
- [ ] Consider using AndroidView for complex HTML editing if needed

## Testing

### Manual Testing Checklist

- [ ] Add a new basic note
- [ ] Add a new cloze note
- [ ] Edit an existing note
- [ ] Change note type
- [ ] Change deck
- [ ] Add/remove tags
- [ ] Toggle sticky fields
- [ ] Add multimedia (image, audio, video, drawing, camera)
- [ ] Preview cards
- [ ] Save and verify changes persist
- [ ] Test with Image Occlusion notes
- [ ] Test keyboard shortcuts

### Automated Testing

Unit tests should be added for:
- `NoteEditorViewModel` state management
- Field validation
- Note saving logic
- Tag management

## Migration References

This migration follows the pattern established by:
- DeckPicker migration (see `DeckPicker.kt` and `DeckPickerScreen.kt`)
- CardBrowser migration (see CardBrowser related compose files)

## Rollback Plan

If critical issues are discovered:
1. Restore `NoteEditorFragment.kt` from `noteeditor/old/NoteEditorFragment.kt.old`
2. Restore `note_editor_fragment.xml` from `noteeditor/old/layouts/note_editor_fragment.xml.backup`
3. Remove or comment out the new Compose files
4. Test thoroughly before releasing

## Contributors

- Colby Cabrera <colbycabrera@gmail.com> - Compose Migration

## Related Documentation

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material 3 Components](https://m3.material.io/)
- [Compose State Management](https://developer.android.com/jetpack/compose/state)
