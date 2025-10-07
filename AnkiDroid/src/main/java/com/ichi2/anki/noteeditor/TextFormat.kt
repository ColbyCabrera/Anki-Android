package com.ichi2.anki.noteeditor

/**
 * A function which takes and returns a [StringFormat] structure
 * Providing a method of inserting text and knowledge of how the selection should change
 */
fun interface TextFormatter {
    fun format(s: String): StringFormat
}

/**
 * A [TextFormatter] which wraps the selected string with [prefix] and [suffix]
 * If there's no selected, the cursor is in the middle of the prefix and suffix
 * If there is text selected, the whole string is selected
 */
class TextWrapper(
    private val prefix: String,
    private val suffix: String
) : TextFormatter {
    override fun format(s: String): StringFormat =
        StringFormat(result = prefix + s + suffix).apply {
            if (s.isEmpty()) {
                // if there's no selection: place the cursor between the start and end tag
                selectionStart = prefix.length
                selectionEnd = prefix.length
            } else {
                // otherwise, wrap the newly formatted context
                selectionStart = 0
                selectionEnd = result.length
            }
        }
}

/**
 * Defines a string insertion, and the selection which should occur once the string is inserted
 *
 * @param result The string which should be inserted
 * @param selectionStart The number of characters inside [result] where the selection should start
 * @param selectionEnd The number of character inside [result] where the selection should end
 */
data class StringFormat(
    var result: String = "",
    var selectionStart: Int = 0,
    var selectionEnd: Int = 0
)