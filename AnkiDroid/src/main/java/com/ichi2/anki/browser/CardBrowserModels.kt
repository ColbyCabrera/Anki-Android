package com.ichi2.anki.browser

import anki.search.BrowserRow

data class BrowserRowWithId(
    val browserRow: BrowserRow,
    val id: Long
)
