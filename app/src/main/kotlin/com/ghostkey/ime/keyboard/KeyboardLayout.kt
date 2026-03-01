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

        val SPACE_ROW_WIDTHS = mapOf(
            "123" to 1.5f,
            "," to 1f,
            " " to 4f,
            "." to 1f,
            "↵" to 1.5f
        )

        private val SPECIAL_KEYS = setOf("⇧", "⌫", "123", "↵", " ")
    }

    fun buildKeys(mode: KeyboardMode, shiftState: ShiftState): List<Key> {
        val rows = ALPHA_ROWS
        val keys = mutableListOf<Key>()
        var y = rowPadding.toFloat()

        rows.forEachIndexed { rowIndex, row ->
            val totalWidth = viewWidth - rowPadding * 2
            val isSpecialRow = rowIndex == rows.lastIndex
            val numKeys = row.size
            val totalSpacing = keySpacing * (numKeys - 1)

            if (isSpecialRow) {
                val totalMultiplier = row.sumOf { (SPACE_ROW_WIDTHS[it] ?: 1f).toDouble() }.toFloat()
                val unitWidth = (totalWidth - totalSpacing) / totalMultiplier
                var x = rowPadding.toFloat()
                row.forEach { label ->
                    val multiplier = SPACE_ROW_WIDTHS[label] ?: 1f
                    val keyWidth = unitWidth * multiplier
                    keys.add(
                        Key(
                            label = label,
                            code = label.codePointAt(0),
                            bounds = RectF(x, y, x + keyWidth, y + keyHeight),
                            isSpecial = label in SPECIAL_KEYS,
                            widthMultiplier = multiplier
                        )
                    )
                    x += keyWidth + keySpacing
                }
            } else {
                val keyWidth = (totalWidth - totalSpacing) / numKeys
                val rowOffset = if (rowIndex == 1) keyWidth * 0.5f else 0f
                var x = rowPadding + rowOffset

                row.forEach { label ->
                    val displayLabel = when {
                        label in SPECIAL_KEYS -> label
                        shiftState != ShiftState.OFF -> label.uppercase()
                        else -> label.lowercase()
                    }
                    keys.add(
                        Key(
                            label = displayLabel,
                            code = displayLabel.codePointAt(0),
                            bounds = RectF(x, y, x + keyWidth, y + keyHeight),
                            isSpecial = label in SPECIAL_KEYS
                        )
                    )
                    x += keyWidth + keySpacing
                }
            }
            y += keyHeight + keySpacing
        }
        return keys
    }

    fun totalHeight(): Int = ALPHA_ROWS.size * keyHeight + (ALPHA_ROWS.size + 1) * keySpacing + rowPadding * 2
}
