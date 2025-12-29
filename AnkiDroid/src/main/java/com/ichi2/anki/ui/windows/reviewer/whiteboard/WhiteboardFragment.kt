/*
 * Copyright (c) 2025 Brayan Oliveira <69634269+brayandso@users.noreply.github.com>
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
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.ichi2.anki.ui.windows.reviewer.whiteboard

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.ThemeUtils
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ichi2.anki.AnkiDroidApp
import com.ichi2.anki.R
import com.ichi2.anki.reviewer.compose.BrushOptionsPopup
import com.ichi2.anki.reviewer.compose.ColorPickerDialog
import com.ichi2.anki.reviewer.compose.EraserOptionsPopup
import com.ichi2.anki.snackbar.showSnackbar
import com.ichi2.anki.ui.windows.reviewer.whiteboard.compose.AddBrushButton
import com.ichi2.anki.ui.windows.reviewer.whiteboard.compose.ColorBrushButton
import com.ichi2.themes.Themes
import com.ichi2.utils.dp
import com.ichi2.utils.increaseHorizontalPaddingOfMenuIcons
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

/**
 * Fragment that displays a whiteboard and its controls.
 */
class WhiteboardFragment : Fragment(R.layout.fragment_whiteboard),
    PopupMenu.OnMenuItemClickListener {
    private val viewModel: WhiteboardViewModel by viewModels {
        WhiteboardViewModel.factory(AnkiDroidApp.sharedPrefs())
    }
    private lateinit var brushToolbarContainerHorizontal: LinearLayout
    private lateinit var brushToolbarContainerVertical: LinearLayout
    private val showEraserOptions = MutableStateFlow(false)
    private val showBrushOptionsIndex = MutableStateFlow<Int?>(null)
    private val showAddBrushDialog = MutableStateFlow(false)
    private val showRemoveBrushDialogIndex = MutableStateFlow<Int?>(null)

    /**
     * Sets up the view, observers, and event listeners.
     */
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val whiteboardView = view.findViewById<WhiteboardView>(R.id.whiteboard_view)
        brushToolbarContainerHorizontal = view.findViewById(R.id.brush_toolbar_container_horizontal)
        brushToolbarContainerVertical = view.findViewById(R.id.brush_toolbar_container_vertical)

        val isNightMode = Themes.systemIsInNightMode(requireContext())
        viewModel.loadState(isNightMode)

        setupUI(view)
        observeViewModel(whiteboardView)

        whiteboardView.onNewPath = viewModel::addPath
        whiteboardView.onEraseGestureStart = viewModel::startPathEraseGesture
        whiteboardView.onEraseGestureMove = viewModel::erasePathsAtPoint
        whiteboardView.onEraseGestureEnd = viewModel::endPathEraseGesture

        setupPopups(view)
    }

    private fun setupPopups(view: View) {
        view.findViewById<ComposeView>(R.id.whiteboard_popups_compose_view).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val eraserVisible by showEraserOptions.collectAsState()
                val brushIndex by showBrushOptionsIndex.collectAsState()

                if (eraserVisible) {
                    EraserOptionsPopup(
                        viewModel = viewModel,
                        onDismissRequest = { showEraserOptions.value = false })
                }

                brushIndex?.let { _ ->
                    BrushOptionsPopup(
                        viewModel = viewModel,
                        onDismissRequest = { showBrushOptionsIndex.value = null })
                }

                val addBrushVisible by showAddBrushDialog.collectAsState()
                if (addBrushVisible) {
                    ColorPickerDialog(
                        defaultColor = viewModel.brushColor.collectAsState().value,
                        onColorPicked = {
                            viewModel.addBrush(it)
                            showAddBrushDialog.value = false
                        },
                        onDismiss = { showAddBrushDialog.value = false })
                }

                val removeBrushIndex by showRemoveBrushDialogIndex.collectAsState()
                removeBrushIndex?.let { index ->
                    AlertDialog(
                        onDismissRequest = { showRemoveBrushDialogIndex.value = null },
                        confirmButton = {
                            TextButton(onClick = {
                                Timber.i("Removed brush of index %d", index)
                                viewModel.removeBrush(index)
                                showRemoveBrushDialogIndex.value = null
                            }) {
                                Text(getString(R.string.dialog_remove))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRemoveBrushDialogIndex.value = null }) {
                                Text(getString(R.string.dialog_cancel))
                            }
                        },
                        text = { Text(getString(R.string.whiteboard_remove_brush_message)) }
                    )
                }
            }
        }
    }

    private fun setupUI(view: View) {
        val undoButton = view.findViewById<ImageButton>(R.id.undo_button)
        val redoButton = view.findViewById<ImageButton>(R.id.redo_button)
        val eraserButton = view.findViewById<EraserButton>(R.id.eraser_button)

        val overflowMenuButton = view.findViewById<ImageButton>(R.id.overflow_menu_button)
        overflowMenuButton.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), overflowMenuButton)
            requireActivity().menuInflater.inflate(R.menu.whiteboard, popupMenu.menu)
            with(popupMenu.menu) {
                findItem(R.id.action_toggle_stylus).isChecked = viewModel.isStylusOnlyMode.value
                (this as? MenuBuilder)?.setOptionalIconsVisible(true)
                context?.increaseHorizontalPaddingOfMenuIcons(this)

                val alignmentMenuItemId = when (viewModel.toolbarAlignment.value) {
                    ToolbarAlignment.LEFT -> R.id.action_align_left
                    ToolbarAlignment.RIGHT -> R.id.action_align_right
                    ToolbarAlignment.BOTTOM -> R.id.action_align_bottom
                }
                findItem(alignmentMenuItemId).isEnabled = false
            }
            popupMenu.setOnMenuItemClickListener(this)
            popupMenu.show()
        }

        undoButton.setOnClickListener { viewModel.undo() }
        redoButton.setOnClickListener { viewModel.redo() }
        eraserButton.setOnClickListener {
            if (viewModel.isEraserActive.value) {
                eraserButton.isChecked = true
                showEraserOptions.value = !showEraserOptions.value
            } else {
                viewModel.enableEraser()
            }
        }

        viewModel.canUndo.onEach { undoButton.isEnabled = it }.launchIn(lifecycleScope)
        viewModel.canRedo.onEach { redoButton.isEnabled = it }.launchIn(lifecycleScope)
    }

    /**
     * Sets up observers for the ViewModel's state flows.
     */
    private fun observeViewModel(whiteboardView: WhiteboardView) {
        val eraserButton = view?.findViewById<EraserButton>(R.id.eraser_button)

        viewModel.paths.onEach(whiteboardView::setHistory).launchIn(lifecycleScope)

        combine(
            viewModel.brushColor,
            viewModel.activeStrokeWidth,
        ) { color, width ->
            whiteboardView.setCurrentBrush(color, width)
        }.launchIn(lifecycleScope)

        combine(
            viewModel.isEraserActive,
            viewModel.eraserMode,
            viewModel.eraserDisplayWidth,
        ) { isActive, mode, width ->
            whiteboardView.isEraserActive = isActive
            eraserButton?.updateState(isActive, mode, width)
            whiteboardView.eraserMode = mode
            if (!isActive) {
                showEraserOptions.value = false
            }
        }.launchIn(lifecycleScope)

        viewModel.brushes.onEach { brushesInfo ->
            updateBrushToolbar(brushesInfo)
            updateToolbarSelection()
        }.launchIn(lifecycleScope)

        viewModel.activeBrushIndex.onEach { updateToolbarSelection() }.launchIn(lifecycleScope)
        viewModel.isEraserActive.onEach {
            updateToolbarSelection()
        }.launchIn(lifecycleScope)

        viewModel.isStylusOnlyMode.onEach { isEnabled ->
            whiteboardView.isStylusOnlyMode = isEnabled
        }.launchIn(lifecycleScope)

        viewModel.toolbarAlignment.onEach { alignment ->
            updateLayoutForAlignment(alignment)
        }.launchIn(lifecycleScope)
    }

    private fun updateBrushToolbar(brushesInfo: List<BrushInfo>) {
        val activeIndex = viewModel.activeBrushIndex.value
        val isEraserActive = viewModel.isEraserActive.value

        val colorNormal = Color(
            ThemeUtils.getThemeAttrColor(
                requireContext(), androidx.appcompat.R.attr.colorControlNormal
            )
        )
        val colorHighlight = Color(
            ThemeUtils.getThemeAttrColor(
                requireContext(), androidx.appcompat.R.attr.colorControlHighlight
            )
        )

        fun setupComposeView(container: LinearLayout, isHorizontal: Boolean) {
            container.removeAllViews()
            container.addView(
                ComposeView(requireContext()).apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    setContent {
                        if (isHorizontal) {
                            Row {
                                RenderBrushes(
                                    brushesInfo,
                                    activeIndex,
                                    isEraserActive,
                                    colorNormal,
                                    colorHighlight
                                )
                            }
                        } else {
                            Column {
                                RenderBrushes(
                                    brushesInfo,
                                    activeIndex,
                                    isEraserActive,
                                    colorNormal,
                                    colorHighlight
                                )
                            }
                        }
                    }
                })
        }

        setupComposeView(brushToolbarContainerHorizontal, true)
        setupComposeView(brushToolbarContainerVertical, false)
    }

    @androidx.compose.runtime.Composable
    private fun RenderBrushes(
        brushesInfo: List<BrushInfo>,
        activeIndex: Int,
        isEraserActive: Boolean,
        colorNormal: Color,
        colorHighlight: Color
    ) {
        brushesInfo.forEachIndexed { index, brush ->
            ColorBrushButton(
                brush = brush,
                isSelected = (index == activeIndex && !isEraserActive),
                onClick = { _ ->
                    if (viewModel.activeBrushIndex.value == index && !viewModel.isEraserActive.value) {
                        showBrushOptionsIndex.value = index
                    } else {
                        viewModel.setActiveBrush(index)
                    }
                },
                onLongClick = {
                    if (viewModel.brushes.value.size > 1) {
                        showRemoveColorDialog(index)
                    } else {
                        Timber.i("Tried to remove the last brush of the whiteboard")
                        showSnackbar(R.string.cannot_remove_last_brush_message)
                    }
                },
                colorNormal = colorNormal,
                colorHighlight = colorHighlight
            )
        }
        AddBrushButton(
            onClick = { showAddColorDialog() },
            colorNormal = colorNormal,
            tooltip = getString(R.string.add_brush)
        )
    }

    /**
     * Updates the selection state of the eraser and brush buttons.
     */
    private fun updateToolbarSelection() {
        // Recreate brush toolbar when selection or eraser state changes; acceptable for small lists
        updateBrushToolbar(viewModel.brushes.value)
    }

    /**
     * Shows a popup for adding a new brush color.
     */
    private fun showAddColorDialog() {
        showAddBrushDialog.value = true
    }

    /**
     * Shows a confirmation dialog for removing a brush.
     */
    private fun showRemoveColorDialog(index: Int) {
        showRemoveBrushDialogIndex.value = index
    }

    /**
     * Updates the toolbar's constraints and orientation.
     */
    private fun updateLayoutForAlignment(alignment: ToolbarAlignment) {
        val controlsContainer = view?.findViewById<View>(R.id.controls_container) ?: return
        val innerLayout = view?.findViewById<LinearLayout>(R.id.inner_controls_layout) ?: return
        val divider = view?.findViewById<View>(R.id.controls_divider) ?: return
        val rootLayout = view as? ConstraintLayout ?: return
        val brushScrollViewHorizontal =
            view?.findViewById<HorizontalScrollView>(R.id.brush_scroll_view_horizontal) ?: return
        val brushScrollViewVertical =
            view?.findViewById<ScrollView>(R.id.brush_scroll_view_vertical) ?: return

        val isVertical = alignment == ToolbarAlignment.LEFT || alignment == ToolbarAlignment.RIGHT
        innerLayout.orientation = if (isVertical) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL

        if (isVertical) {
            brushScrollViewHorizontal.visibility = View.GONE
            brushScrollViewVertical.visibility = View.VISIBLE
        } else {
            brushScrollViewHorizontal.visibility = View.VISIBLE
            brushScrollViewVertical.visibility = View.GONE
        }

        val dp = 1.dp.toPx(requireContext())
        val dividerParams = divider.layoutParams as LinearLayout.LayoutParams
        val dividerMargin = 4 * dp
        if (isVertical) {
            dividerParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            dividerParams.height = 1 * dp
            dividerParams.setMargins(0, dividerMargin, 0, dividerMargin)
        } else {
            dividerParams.width = 1 * dp
            dividerParams.height = LinearLayout.LayoutParams.MATCH_PARENT
            dividerParams.setMargins(dividerMargin, 0, dividerMargin, 0)
        }
        divider.layoutParams = dividerParams

        val constraintSet = ConstraintSet()
        constraintSet.clone(rootLayout)
        val containerId = controlsContainer.id
        constraintSet.clear(containerId)

        when (alignment) {
            ToolbarAlignment.BOTTOM -> {
                constraintSet.connect(
                    containerId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM
                )
                constraintSet.connect(
                    containerId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START
                )
                constraintSet.connect(
                    containerId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END
                )
                constraintSet.constrainWidth(containerId, ConstraintSet.WRAP_CONTENT)
                constraintSet.constrainHeight(containerId, ConstraintSet.WRAP_CONTENT)
                constraintSet.constrainedWidth(containerId, true)
                constraintSet.setMargin(containerId, ConstraintSet.START, 24 * dp)
                constraintSet.setMargin(containerId, ConstraintSet.END, 24 * dp)
                constraintSet.setMargin(containerId, ConstraintSet.BOTTOM, 8 * dp)
            }

            ToolbarAlignment.RIGHT -> {
                constraintSet.connect(
                    containerId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END
                )
                constraintSet.connect(
                    containerId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP
                )
                constraintSet.connect(
                    containerId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM
                )
                constraintSet.constrainWidth(containerId, ConstraintSet.WRAP_CONTENT)
                constraintSet.constrainHeight(containerId, ConstraintSet.WRAP_CONTENT)
                constraintSet.setMargin(containerId, ConstraintSet.END, 8 * dp)
            }

            ToolbarAlignment.LEFT -> {
                constraintSet.connect(
                    containerId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START
                )
                constraintSet.connect(
                    containerId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP
                )
                constraintSet.connect(
                    containerId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM
                )
                constraintSet.constrainWidth(containerId, ConstraintSet.WRAP_CONTENT)
                constraintSet.constrainHeight(containerId, ConstraintSet.WRAP_CONTENT)
                constraintSet.setMargin(containerId, ConstraintSet.START, 8 * dp)
            }
        }

        constraintSet.applyTo(rootLayout)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        Timber.i("WhiteboardFragment::onMenuItemClick %s", item.title)
        when (item.itemId) {
            R.id.action_toggle_stylus -> {
                item.isChecked = !item.isChecked
                viewModel.toggleStylusOnlyMode()
            }

            R.id.action_align_left -> viewModel.setToolbarAlignment(ToolbarAlignment.LEFT)
            R.id.action_align_bottom -> viewModel.setToolbarAlignment(ToolbarAlignment.BOTTOM)
            R.id.action_align_right -> viewModel.setToolbarAlignment(ToolbarAlignment.RIGHT)
            else -> return false
        }
        return true
    }

    fun resetCanvas() = viewModel.reset()
}
