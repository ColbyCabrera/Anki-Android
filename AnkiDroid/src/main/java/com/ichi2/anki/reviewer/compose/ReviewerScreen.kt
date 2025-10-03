/****************************************************************************************
 * Copyright (c) 2009 Edu Zamora <edu.zasu@gmail.com>                                   *
 * Copyright (c) 2009 Casey Link <unnamedrambler@gmail.com>                             *
 * Copyright (c) 2014 Timothy Rae <perceptualchaos2@gmail.com>                          *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/
package com.ichi2.anki.reviewer.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ichi2.anki.reviewer.ReviewerEvent
import com.ichi2.anki.reviewer.ReviewerViewModel

@Composable
fun ReviewerScreen(
    viewModel: ReviewerViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Column {
        ReviewerTopBar(
            newCount = state.newCount,
            learnCount = state.learnCount,
            reviewCount = state.reviewCount,
            timer = state.timer,
            chosenAnswer = state.chosenAnswer,
            isMarked = state.isMarked,
            flag = state.flag,
            onToggleMark = { viewModel.onEvent(ReviewerEvent.ToggleMark) },
            onSetFlag = { flag -> viewModel.onEvent(ReviewerEvent.SetFlag(flag)) }
        )
        Flashcard(
            html = state.html,
            onTap = { viewModel.onEvent(ReviewerEvent.ShowAnswer) },
            onLinkClick = { url -> viewModel.onEvent(ReviewerEvent.LinkClicked(url)) },
            imageLoader = ankiImageLoader(context, state.mediaDirectory),
            mediaDirectory = state.mediaDirectory
        )
    }
}