## 2025-02-18 - Compose List Stability with Wrappers
**Learning:** Standard `List<T>` is considered unstable in Compose, preventing skipping. Adding `kotlinx.collections.immutable` adds a dependency. A lightweight alternative is to wrap the `List` in an `@Immutable` data class.
**Action:** Use `@Immutable` data class wrappers for external Lists when adding dependencies is restricted.
