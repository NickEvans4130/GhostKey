package com.ghostkey.ime.suggestion

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.ghostkey.util.dpToPx

// Canvas-drawn suggestion strip — full implementation in feature/tier1-wired
class SuggestionStripView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    interface SuggestionListener {
        fun onWordSuggestionTapped(word: String)
        fun onTier2RewriteRequested()
        fun onTier2ResultAccepted(text: String)
        fun onTier2ResultDismissed()
    }

    var listener: SuggestionListener? = null
    var state: SuggestionState = SuggestionState()
        set(value) {
            field = value
            invalidate()
        }

    private val height = context.dpToPx(40)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF0D1117.toInt() }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE6EDF3.toInt()
        textSize = context.dpToPx(13).toFloat()
    }
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF58A6FF.toInt()
        textSize = context.dpToPx(13).toFloat()
    }
    private val warningPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFF59E0B.toInt()
        textSize = context.dpToPx(13).toFloat()
    }
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF30363D.toInt()
        strokeWidth = 1f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        canvas.drawLine(0f, height - 1f, width.toFloat(), height - 1f, dividerPaint)

        val cy = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2

        // Tier 2 button on right
        val tier2Label = when (state.tier2Status) {
            is Tier2Status.Loading -> "* ..."
            is Tier2Status.Ready -> "* Apply"
            else -> "* Rewrite"
        }
        val tier2X = width - accentPaint.measureText(tier2Label) - context.dpToPx(12)
        canvas.drawText(tier2Label, tier2X, cy, accentPaint)

        // Divider before tier 2 button
        canvas.drawLine(tier2X - context.dpToPx(8), 8f, tier2X - context.dpToPx(8), height - 8f, dividerPaint)

        // Transform preview or warning in centre
        val centreItem = state.transformItems.firstOrNull()
        if (centreItem != null) {
            val label = when (centreItem) {
                is SuggestionItem.TransformPreview ->
                    "${centreItem.original} -> ${centreItem.transformed}"
                is SuggestionItem.Warning -> "! ${centreItem.flag.message}"
                else -> ""
            }
            val paint = if (centreItem is SuggestionItem.Warning) warningPaint else textPaint
            canvas.drawText(label, width / 2f, cy, paint.apply { textAlign = Paint.Align.CENTER })
            paint.textAlign = Paint.Align.LEFT
        } else if (state.wordSuggestions.isNotEmpty()) {
            var x = context.dpToPx(12).toFloat()
            state.wordSuggestions.take(3).forEach { word ->
                canvas.drawText(word, x, cy, textPaint)
                x += textPaint.measureText(word) + context.dpToPx(24)
            }
        }
    }
}
