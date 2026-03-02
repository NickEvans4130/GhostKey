package com.ghostkey.ime.keyboard

import android.graphics.RectF

data class Key(
    val label: String,
    val code: Int,
    val bounds: RectF,
    val isSpecial: Boolean = false,
    val widthMultiplier: Float = 1f
)

class KeyboardLayout(
    private val viewWidth: Int,
    private val keyHeight: Int,
    private val keySpacing: Int,
    private val rowPadding: Int
) {
    companion object {
        val ALPHA_ROWS = listOf(
            listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
            listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
            listOf("⇧", "Z", "X", "C", "V", "B", "N", "M", "⌫"),
            listOf("123", ",", " ", ".", "↵")
        )

        // Standard phone-style numeric pad
        val NUMERIC_ROWS = listOf(
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
            listOf("-", "/", ":", ";", "(", ")", "$", "&", "@", "*"),
            listOf("#+=", ".", ",", "?", "!", "'", "⌫"),
            listOf("ABC", " ", "↵")
        )

        // Extended symbols (accessed via #+= from numeric)
        val SYMBOL_ROWS = listOf(
            listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "="),
            listOf("_", "\\", "|", "~", "<", ">", "€", "£", "¥", "•"),
            listOf("123", ".", ",", "?", "!", "'", "⌫"),
            listOf("ABC", " ", "↵")
        )

        // Keys wider than 1.0 unit — anything not listed defaults to 1.0
        private val KEY_WIDTHS = mapOf(
            "⇧"   to 1.5f,
            "⌫"   to 1.5f,
            "#+=" to 1.5f,
            "123" to 1.5f,
            "ABC" to 1.5f,
            " "   to 4.0f,
            "↵"   to 1.5f,
            ","   to 1.0f,
            "."   to 1.0f
        )

        private val SPECIAL_KEYS = setOf("⇧", "⌫", "123", "ABC", "#+=", "↵", " ")
    }

    fun buildKeys(mode: KeyboardMode, shiftState: ShiftState): List<Key> {
        val rows = when (mode) {
            KeyboardMode.ALPHA   -> ALPHA_ROWS
            KeyboardMode.NUMERIC -> NUMERIC_ROWS
            KeyboardMode.SYMBOL  -> SYMBOL_ROWS
        }

        val keys = mutableListOf<Key>()
        var y = rowPadding.toFloat()

        rows.forEachIndexed { rowIndex, row ->
            val usableWidth = viewWidth - rowPadding * 2
            val numKeys = row.size
            val hasVariableWidth = row.any { it in KEY_WIDTHS }

            if (hasVariableWidth) {
                val totalMult = row.sumOf { (KEY_WIDTHS[it] ?: 1f).toDouble() }.toFloat()
                val totalSpacing = keySpacing * (numKeys - 1)
                val unitW = (usableWidth - totalSpacing) / totalMult
                var x = rowPadding.toFloat()

                row.forEach { label ->
                    val mult = KEY_WIDTHS[label] ?: 1f
                    val kw = unitW * mult
                    keys.add(Key(
                        label = shifted(label, mode, shiftState),
                        code = label.codePointAt(0),
                        bounds = RectF(x, y, x + kw, y + keyHeight),
                        isSpecial = label in SPECIAL_KEYS,
                        widthMultiplier = mult
                    ))
                    x += kw + keySpacing
                }
            } else {
                val totalSpacing = keySpacing * (numKeys - 1)
                val kw = (usableWidth - totalSpacing) / numKeys
                // QWERTY row 1 (A-row) is inset half a key to centre it visually
                val offset = if (mode == KeyboardMode.ALPHA && rowIndex == 1) kw * 0.5f else 0f
                var x = rowPadding + offset

                row.forEach { label ->
                    keys.add(Key(
                        label = shifted(label, mode, shiftState),
                        code = label.codePointAt(0),
                        bounds = RectF(x, y, x + kw, y + keyHeight),
                        isSpecial = label in SPECIAL_KEYS
                    ))
                    x += kw + keySpacing
                }
            }

            y += keyHeight + keySpacing
        }

        return keys
    }

    private fun shifted(label: String, mode: KeyboardMode, shiftState: ShiftState): String =
        if (label !in SPECIAL_KEYS && mode == KeyboardMode.ALPHA) {
            if (shiftState != ShiftState.OFF) label.uppercase() else label.lowercase()
        } else label

    fun totalHeight(): Int =
        ALPHA_ROWS.size * keyHeight + (ALPHA_ROWS.size - 1) * keySpacing + rowPadding * 2
}
