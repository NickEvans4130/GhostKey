package com.ghostkey.ime.suggestion

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ghostkey.util.dpToPx

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

    private val stripHeight = context.dpToPx(40)
    private val hPad = context.dpToPx(12).toFloat()
    private val gap  = context.dpToPx(24).toFloat()

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0D1117.toInt()
    }
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

    // Touch regions updated each draw pass
    private var tier2ButtonBounds = RectF()
    private val wordBounds = mutableListOf<Pair<RectF, String>>()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), stripHeight)
    }

    override fun onDraw(canvas: Canvas) {
        val h = stripHeight.toFloat()
        val w = width.toFloat()
        canvas.drawRect(0f, 0f, w, h, bgPaint)
        canvas.drawLine(0f, h - 1f, w, h - 1f, dividerPaint)

        val cy = h / 2f - (textPaint.descent() + textPaint.ascent()) / 2f

        // ── Tier 2 button (right side) ────────────────────────────────────────
        val tier2Label = when (state.tier2Status) {
            is Tier2Status.Loading -> "... Rewriting"
            is Tier2Status.Ready   -> "Apply \u2713"
            else                   -> "\u2605 Rewrite"
        }
        val tier2Paint = accentPaint
        val tier2W = tier2Paint.measureText(tier2Label)
        val tier2X = w - tier2W - hPad
        canvas.drawText(tier2Label, tier2X, cy, tier2Paint)
        tier2ButtonBounds = RectF(tier2X - hPad / 2, 0f, w, h)

        // Divider before tier 2 area
        val divX = tier2X - hPad
        canvas.drawLine(divX, 8f, divX, h - 8f, dividerPaint)

        // ── Content area (left of divider) ────────────────────────────────────
        wordBounds.clear()
        val contentRight = divX - hPad

        val centreItem = state.transformItems.firstOrNull()
        if (centreItem != null) {
            // Transform preview or flag warning — centred
            val label = when (centreItem) {
                is SuggestionItem.TransformPreview ->
                    "${centreItem.original} \u2192 ${centreItem.transformed}"
                is SuggestionItem.Warning ->
                    "\u26A0 ${centreItem.flag.message}"
                else -> ""
            }
            val paint = if (centreItem is SuggestionItem.Warning) warningPaint else textPaint
            val maxW = contentRight - hPad
            val clipped = clipText(label, paint, maxW)
            canvas.drawText(clipped, contentRight / 2f + hPad, cy, paint.apply {
                textAlign = Paint.Align.CENTER
            })
            paint.textAlign = Paint.Align.LEFT
        } else {
            // Word suggestions (up to 3), separated by dividers
            var x = hPad
            state.wordSuggestions.take(3).forEach { word ->
                if (x >= contentRight) return@forEach
                val tw = textPaint.measureText(word)
                val clipped = clipText(word, textPaint, contentRight - x)
                canvas.drawText(clipped, x, cy, textPaint)
                val bounds = RectF(x - hPad / 2, 0f, x + tw + hPad / 2, h)
                wordBounds.add(Pair(bounds, word))
                x += tw + gap
                if (x < contentRight) {
                    canvas.drawLine(x - gap / 2, 8f, x - gap / 2, h - 8f, dividerPaint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked != MotionEvent.ACTION_UP) return true
        val x = event.x
        val y = event.y

        if (tier2ButtonBounds.contains(x, y)) {
            when (val s = state.tier2Status) {
                is Tier2Status.Ready -> listener?.onTier2ResultAccepted(s.rewrittenText)
                is Tier2Status.Idle  -> listener?.onTier2RewriteRequested()
                is Tier2Status.Error -> listener?.onTier2RewriteRequested()
                else -> Unit
            }
            return true
        }

        wordBounds.firstOrNull { it.first.contains(x, y) }?.let { (_, word) ->
            listener?.onWordSuggestionTapped(word)
        }
        return true
    }

    // Clips text to fit within maxWidth, appending "…" if truncated
    private fun clipText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "…"
        val ellipsisW = paint.measureText(ellipsis)
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end)) + ellipsisW > maxWidth) end--
        return text.substring(0, end) + ellipsis
    }
}
