## 2024-05-23 - Dynamic Content Descriptions for Sync States
**Learning:** When a UI component has a dynamic state (like "Syncing" vs "Idle"), the `contentDescription` must update to reflect this state, even if the button is disabled. A static description based only on the background state (like "Pending Changes") is misleading when an active operation is blocking interaction.
**Action:** Always verify that `contentDescription` logic accounts for transient active states (loading, syncing) and prioritizes them over static status messages.

## 2024-05-23 - Infinite Animations in Compose
**Learning:** To create a continuous background animation (like a spinning sync icon) that doesn't interfere with interaction-based animations (like a click ripple or spring), use `rememberInfiniteTransition` with a separate `LaunchedEffect` to reset state when the animation stops.
**Action:** Use `rememberInfiniteTransition` for loop animations and conditional modifiers to switch between the infinite value and a static/interactive value.
