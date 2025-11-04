# NoteEditor Compose Migration Plan
## Converting NoteEditorActivity from Fragment-based to Full Compose Architecture

**Status**: Planning Phase  
**Target Date**: Q1 2026  
**Owner**: Migration Team  
**Related Issue**: #XXXXX

---

## Executive Summary

This plan outlines the migration of `NoteEditorActivity` from a Fragment-based architecture to a modern, fully Compose-based implementation using `ComponentActivity`. This will eliminate the current three-layer architecture (Activity → Fragment → ComposeView) that causes theme application issues, navigation bar color problems, and unnecessary complexity.

---

## Current Architecture Problems

### 1. **Layering Issues**
```
NoteEditorActivity (AnkiActivity)
  ├─ Toolbar (XML)
  └─ NoteEditorFragment (Fragment)
      └─ ComposeView
          └─ Compose NoteEditor UI
```

**Problems**:
- Fragment adds no value, just wraps ComposeView
- Theme not properly propagating through layers
- Navigation bar color not applying
- Duplicate toolbar confusion (Activity toolbar vs attempted Compose TopAppBar)

### 2. **Technical Debt**
- Fragment lifecycle complexity
- Intent handling split between Activity and Fragment
- ViewModel scoping confusion
- XML layouts for simple container structure
- Fragment transaction boilerplate

---

## Target Architecture

### New Structure
```
NoteEditorActivity (ComponentActivity)
  └─ Compose setContent { }
      ├─ NoteEditorScaffold
      │   ├─ TopAppBar (Compose, with back button)
      │   ├─ Content (fields, deck/type selectors)
      │   └─ BottomBar (formatting toolbar)
      └─ Direct ViewModel integration
```

**Benefits**:
- Single source of truth for UI state
- Direct theme application (including navigation bar)
- Simplified lifecycle management
- Modern Android development practices
- Easier testing and debugging
- Better performance (no XML inflation)

---

## Migration Phases

### Phase 1: Analysis & Planning ✅ (Current)
**Duration**: 1 week  
**Tasks**:
1. ✅ Identify all Fragment dependencies
2. ✅ Document AnkiActivity features used
3. ✅ Map out interface implementations
4. ✅ Create architecture diagram
5. ✅ Define testing strategy

### Phase 2: Preparation (Week 1-2)
**Tasks**:
1. Extract business logic from Fragment to ViewModel
2. Document all lifecycle hooks and their purposes
3. Identify all menu items and shortcuts
4. Map activity result contracts
5. Create feature flag for gradual rollout

**Deliverables**:
- Standalone ViewModel with all business logic
- Documentation of all features to preserve
- Test plan covering all workflows

### Phase 3: Core Migration (Week 2-4)
**Tasks**:
1. Create new `NoteEditorActivity` extending `ComponentActivity`
2. Implement required interfaces (DispatchKeyEventListener, ShortcutGroupProvider, etc.)
3. Port AnkiActivity features needed:
   - Collection loading and management
   - Storage permissions
   - Crash recovery
   - Theme application
4. Implement Compose UI with `setContent { }`
5. Add Compose TopAppBar with back navigation and menu
6. Set navigation bar color directly in activity

**Critical Requirements**:
- Must maintain all existing functionality
- Zero breaking changes to public API
- Preserve all keyboard shortcuts
- Maintain activity result contracts

### Phase 4: UI Migration (Week 4-5)
**Tasks**:
1. Remove duplicate TopAppBar from Compose NoteEditor
2. Ensure proper scaffold structure
3. Implement proper padding with WindowInsets
4. Apply Material3 theming correctly
5. Set navigation bar color (transparent or matching toolbar)
6. Test theme switching (light/dark)

### Phase 5: Intent & Launcher Updates (Week 5-6)
**Tasks**:
1. Update `NoteEditorLauncher` to remove fragment-related code
2. Update all activities that launch NoteEditor:
   - Reviewer
   - Browser
   - DeckPicker
   - Shortcuts
3. Remove `FRAGMENT_NAME_EXTRA` and `FRAGMENT_ARGS_EXTRA`
4. Simplify intent creation
5. Test all launch paths

### Phase 6: Cleanup (Week 6-7)
**Tasks**:
1. Delete `NoteEditorFragment.kt` (3364 lines → 0)
2. Delete `note_editor_fragment.xml`
3. Delete `note_editor.xml` (activity layout)
4. Remove `Theme.NoteEditor` workaround from styles.xml
5. Remove fragment transaction code from activity
6. Remove unused imports and utilities
7. Update documentation

### Phase 7: Testing & Validation (Week 7-8)
**Testing Checklist**:
- [ ] Add new note (all note types)
- [ ] Edit existing note
- [ ] Multimedia buttons (camera, audio, etc.)
- [ ] Sticky field toggle
- [ ] Deck selection
- [ ] Note type selection and change
- [ ] Tags dialog
- [ ] Cards button
- [ ] Image occlusion workflows
- [ ] All formatting toolbar buttons
- [ ] Keyboard shortcuts
- [ ] Save functionality
- [ ] Preview functionality
- [ ] Back navigation
- [ ] Activity results to calling activities
- [ ] Theme switching (light/dark)
- [ ] Navigation bar color
- [ ] Rotation handling
- [ ] Permission requests
- [ ] Crash recovery
- [ ] Snackbar display
- [ ] Menu items (overflow)

### Phase 8: Performance Validation (Week 8)
**Metrics to Measure**:
- Activity launch time (target: <500ms)
- Memory usage (should decrease without XML inflation)
- Recomposition counts
- Frame drop rate
- APK size impact

---

## Risk Assessment

### High Risk
- **AnkiActivity Dependencies**: May need features only available in AnkiActivity
  - *Mitigation*: Extract interfaces, create utility classes, or extend AnkiActivity if necessary
  
- **Activity Result Contracts**: Breaking changes to how results are returned
  - *Mitigation*: Maintain exact same result contracts, extensive integration testing

### Medium Risk
- **Menu Handling**: MenuProvider interface may not work with ComponentActivity
  - *Mitigation*: Use Compose alternatives or implement custom menu handling
  
- **Theme Application**: ComponentActivity theme handling differs from AnkiActivity
  - *Mitigation*: Explicit theme setting in onCreate, test all theme scenarios

### Low Risk
- **Keyboard Shortcuts**: Already implemented with DispatchKeyEventListener
  - *Mitigation*: Interface already exists, should port cleanly
  
- **ViewModel Scoping**: Already using proper ViewModel architecture
  - *Mitigation*: ViewModels work identically with ComponentActivity

---

## Rollback Plan

1. **Feature Flag**: Keep old Fragment-based code behind a feature flag initially
2. **Gradual Rollout**: Release to beta testers first (10% → 50% → 100%)
3. **Monitoring**: Track crash rates, performance metrics
4. **Quick Revert**: Can disable feature flag and revert to Fragment in emergency

---

## Success Criteria

### Functional
- ✅ All existing features work identically
- ✅ No breaking changes to public API
- ✅ All tests pass
- ✅ Zero new crashes introduced

### Technical
- ✅ Navigation bar color applies correctly
- ✅ Theme propagates properly
- ✅ Single toolbar (no duplicates)
- ✅ Reduced code complexity (delete ~3500+ lines)
- ✅ Improved performance metrics

### User Experience
- ✅ No user-visible changes (unless intentional improvements)
- ✅ Same or better performance
- ✅ Improved visual consistency

---

## Code Structure Comparison

### Before (Current)
```kotlin
// NoteEditorActivity.kt (~200 lines)
class NoteEditorActivity : AnkiActivity() {
    lateinit var noteEditorFragment: NoteEditorFragment
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.note_editor) // Toolbar + Fragment container
        supportFragmentManager.commit {
            replace(R.id.note_editor_fragment_frame, NoteEditorFragment.newInstance())
        }
    }
}

// NoteEditorFragment.kt (~3364 lines)
class NoteEditorFragment : Fragment(R.layout.note_editor_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val composeView = view.findViewById<ComposeView>(R.id.note_editor_compose)
        composeView.setContent {
            NoteEditor(...)
        }
    }
    // 3000+ lines of business logic
}
```

### After (Target)
```kotlin
// NoteEditorActivity.kt (~400-500 lines)
class NoteEditorActivity : ComponentActivity(),
    BaseSnackbarBuilderProvider,
    DispatchKeyEventListener,
    ShortcutGroupProvider {
    
    private lateinit var viewModel: NoteEditorViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Collection loading
        ensureStoragePermissions()
        loadCollection()
        
        // Theme & navigation bar
        window.navigationBarColor = Color.TRANSPARENT
        
        // Compose UI
        setContent {
            AnkiTheme {
                NoteEditorScaffold(
                    viewModel = viewModel,
                    onBackPressed = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }
}

// NoteEditorViewModel.kt (existing, enhanced)
class NoteEditorViewModel : ViewModel() {
    // Business logic moved here
}

// NoteEditorScaffold.kt (new)
@Composable
fun NoteEditorScaffold(...) {
    Scaffold(
        topBar = { NoteEditorTopAppBar(...) },
        bottomBar = { NoteEditorToolbar(...) }
    ) { paddingValues ->
        NoteEditor(...)
    }
}
```

---

## Dependencies & Requirements

### Code Dependencies
- Jetpack Compose 1.5+
- ComponentActivity (already in project)
- Material3 (already in project)
- Existing ViewModels and state management

### Team Dependencies
- UI/UX review for toolbar design
- QA testing for all workflows
- Beta tester feedback
- Performance testing

---

## Timeline Summary

| Phase | Duration | Key Deliverable |
|-------|----------|----------------|
| 1. Analysis | Week 0 | Migration plan (this document) |
| 2. Preparation | Week 1-2 | Extracted ViewModel, test plan |
| 3. Core Migration | Week 2-4 | Working ComponentActivity |
| 4. UI Migration | Week 4-5 | Proper Compose UI with theme |
| 5. Intent Updates | Week 5-6 | Updated launchers |
| 6. Cleanup | Week 6-7 | Deleted legacy code |
| 7. Testing | Week 7-8 | All tests passing |
| 8. Performance | Week 8 | Metrics validated |

**Total Duration**: ~8 weeks  
**Target Completion**: End of Q1 2026

---

## Next Steps

1. **Immediate**: Review and approve this plan
2. **Week 1**: Start Phase 2 (Preparation)
   - Begin extracting business logic
   - Document all features
   - Set up feature flag
3. **Week 2**: Start Phase 3 (Core Migration)
   - Create new ComponentActivity implementation
   - Port essential features

---

## Questions to Resolve

1. **AnkiActivity Dependency**: Can we extend ComponentActivity or must we keep AnkiActivity?
   - If we must keep AnkiActivity, can we still use setContent { } directly?
   
2. **MenuProvider**: Does it work with ComponentActivity or need alternative?

3. **Collection Loading**: Can we extract collection management to a utility/interface?

4. **Theme Configuration**: What's the proper way to apply app theme to ComponentActivity?

5. **Feature Flag**: What system should we use for gradual rollout?

---

## Approval Sign-off

- [ ] Technical Lead Review
- [ ] Architecture Review
- [ ] QA Strategy Review
- [ ] Timeline Approval
- [ ] Resource Allocation

---

**Document Version**: 1.0  
**Last Updated**: October 30, 2025  
**Status**: Awaiting Approval
