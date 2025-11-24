/*
 * Copyright (c) 2025 Ankitects Pty Ltd <https://apps.ankiweb.net>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ichi2.anki

/**
 * The state of the sync icon in the toolbar.
 *
 * This is used to show a badge when the collection is "dirty" (i.e. has [PendingChanges]).
 *
 * @see DeckPicker.updateSyncIconFromState
 * @see com.ichi2.anki.deckpicker.DeckPickerViewModel.fetchSyncIconState
 */
enum class SyncIconState {
    /** No changes to sync. */
    Normal,

    /** The collection has been modified, but not synced. */
    PendingChanges,

    /** A one-way sync is recommended. */
    OneWay,

    /** The user is not logged in. */
    NotLoggedIn,
}
