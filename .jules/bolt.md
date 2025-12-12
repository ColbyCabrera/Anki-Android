## 2025-02-18 - RoundedPolygonShape Allocations
**Learning:** `RoundedPolygonShape` in `DeckItem.kt` (and `StudyOptionsScreen.kt`) was creating a new `Matrix` and `Path` on every `createOutline` call (layout pass). Since these shapes are often global constants or reused with fixed sizes, this caused unnecessary allocations during scrolling.
**Action:** Implemented caching in `RoundedPolygonShape` to reuse the `Outline` (and underlying `Path`) when the size hasn't changed.
