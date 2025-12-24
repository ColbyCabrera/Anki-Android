/*
 *  Copyright (c) 2025 Hari Srinivasan <harisrini21@gmail.com>
 *
 *  This program is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 3 of the License, or (at your option) any later
 *  version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 *  PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ichi2.anki

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.ichi2.anki.android.input.ShortcutGroup
import com.ichi2.anki.android.input.ShortcutGroupProvider
import com.ichi2.anki.libanki.Collection
import com.ichi2.anki.noteeditor.NoteEditorLauncher
import com.ichi2.anki.snackbar.BaseSnackbarBuilderProvider
import com.ichi2.anki.snackbar.SnackbarBuilder
import timber.log.Timber
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

/**
 * To find the actual note Editor, @see [NoteEditorFragment]
 * This activity contains the NoteEditorFragment, and, on x-large screens, the previewer fragment.
 * It also ensures that changes in the note are transmitted to the previewer
 */

// TODO: Move intent handling to [NoteEditorActivity] from [NoteEditorFragment]
class NoteEditorActivity :
    AnkiActivity(),
    BaseSnackbarBuilderProvider,
    DispatchKeyEventListener,
    ShortcutGroupProvider {
    override val baseSnackbarBuilder: SnackbarBuilder = { }

    lateinit var noteEditorFragment: NoteEditorFragment

    private val mainToolbar: androidx.appcompat.widget.Toolbar
        get() = findViewById(R.id.toolbar)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        if (showedActivityFailedScreen(savedInstanceState)) {
            return
        }
        super.onCreate(savedInstanceState)
        if (!ensureStoragePermissions()) {
            return
        }

        // Set window background to match theme surface color (fixes keyboard dismiss flash)
        val backgroundColor = resolveThemeSurfaceColor()
        window.decorView.setBackgroundColor(backgroundColor)

        setContentView(R.layout.note_editor)

        if (savedInstanceState == null) {
            val launcher = NoteEditorLauncher.fromIntent(intent)
            supportFragmentManager.commit {
                replace(
                    R.id.note_editor_fragment_frame,
                    NoteEditorFragment.newInstance(launcher),
                    FRAGMENT_TAG
                )
                setReorderingAllowed(true)
                runOnCommit {
                    noteEditorFragment =
                        supportFragmentManager.findFragmentByTag(FRAGMENT_TAG) as NoteEditorFragment
                }
            }
        } else {
            noteEditorFragment =
                supportFragmentManager.findFragmentByTag(FRAGMENT_TAG) as NoteEditorFragment
        }

        enableToolbar()
        // Hide legacy toolbar in favor of the Compose top app bar managed by the fragment
        mainToolbar.visibility = View.GONE

        // R.id.home is handled in setNavigationOnClickListener
        // Set a listener for back button clicks in the toolbar
        mainToolbar.setNavigationOnClickListener {
            Timber.i("NoteEditor:: Back button on the menu was pressed")
            onBackPressedDispatcher.onBackPressed()
        }

        startLoadingCollection()
    }

    override fun onCollectionLoaded(col: Collection) {
        super.onCollectionLoaded(col)
        Timber.d("onCollectionLoaded()")
        registerReceiver()
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean =
        noteEditorFragment.dispatchKeyEvent(event) || super.dispatchKeyEvent(event)

    override val shortcuts: ShortcutGroup
        get() = noteEditorFragment.shortcuts

    /**
     * Resolves the colorSurface attribute from the current theme.
     * Handles both resource references and direct color values robustly.
     *
     * @return The resolved color, or a sensible fallback if resolution fails.
     */
    private fun resolveThemeSurfaceColor(): Int {
        val typedValue = TypedValue()
        val resolved = theme.resolveAttribute(
            com.google.android.material.R.attr.colorSurface,
            typedValue,
            true
        )

        if (!resolved) {
            // Attribute not found, return fallback
            return Color.DKGRAY
        }

        return when {
            // If it's a resource reference, resolve it properly
            typedValue.resourceId != 0 -> {
                ContextCompat.getColor(this, typedValue.resourceId)
            }
            // If it's a direct color value (type is between TYPE_FIRST_COLOR_INT and TYPE_LAST_COLOR_INT)
            typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
                typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT -> {
                typedValue.data
            }
            // Fallback for unexpected types
            else -> Color.DKGRAY
        }
    }

    companion object {
        const val FRAGMENT_ARGS_EXTRA = "fragmentArgs"
        const val FRAGMENT_NAME_EXTRA = "fragmentName"
        const val FRAGMENT_TAG = "NoteEditorFragmentTag"

        /**
         * Creates an Intent to launch the NoteEditor activity with a specific fragment class and arguments.
         *
         * @param context The context from which the intent will be launched
         * @param fragmentClass The Kotlin class of the Fragment to instantiate
         * @param arguments Optional bundle of arguments to pass to the fragment
         * @param intentAction Optional action to set on the intent
         * @return An Intent configured to launch NoteEditor with the specified fragment
         */
        fun getIntent(
            context: Context,
            fragmentClass: KClass<out Fragment>,
            arguments: Bundle? = null,
            intentAction: String? = null,
        ): Intent =
            Intent(context, NoteEditorActivity::class.java).apply {
                putExtra(FRAGMENT_NAME_EXTRA, fragmentClass.jvmName)
                putExtra(FRAGMENT_ARGS_EXTRA, arguments)
                action = intentAction
            }
    }
}
