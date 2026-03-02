package com.ghostkey.ime.alias

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ghostkey.util.dpToPx

class AliasBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    interface AliasBarListener {
        fun onAliasNameTapped()
        fun onStyleToggleTapped()
        fun onSettingsTapped()
    }

    var listener: AliasBarListener? = null

    /** Display name of the active alias (e.g. "Rowan Anderson"). Null shows "No alias". */
    var activeAliasName: String? = null
        set(value) { field = value; invalidate() }

    var isStyleActive: Boolean = true
        set(value) { field = value; invalidate() }

    private val barHeight    = context.dpToPx(48)
    private val avatarRadius = context.dpToPx(14).toFloat()
    private val hPad         = context.dpToPx(12).toFloat()
    private val dotRadius    = context.dpToPx(5).toFloat()

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF010409.toInt()
    }
    private val avatarBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF161B22.toInt()
    }
    private val avatarTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF58A6FF.toInt()
        textSize = context.dpToPx(12).toFloat()
        textAlign = Paint.Align.CENTER
    }
    private val aliasNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE6EDF3.toInt()
        textSize = context.dpToPx(14).toFloat()
    }
    private val activeDotPaint   = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF58A6FF.toInt() }
    private val inactiveDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF30363D.toInt() }
    private val styleOnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF58A6FF.toInt()
        textSize = context.dpToPx(12).toFloat()
    }
    private val styleOffPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF8B949E.toInt()
        textSize = context.dpToPx(12).toFloat()
    }
    private val settingsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF8B949E.toInt()
        textSize = context.dpToPx(18).toFloat()
        textAlign = Paint.Align.RIGHT
    }
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF30363D.toInt()
        strokeWidth = 1f
    }

    // Touch regions updated each draw pass
    private var aliasTapBounds    = RectF()
    private var styleTapBounds    = RectF()
    private var settingsTapBounds = RectF()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), barHeight)
    }

    override fun onDraw(canvas: Canvas) {
        val h = barHeight.toFloat()
        val w = width.toFloat()
        canvas.drawRect(0f, 0f, w, h, bgPaint)
        canvas.drawLine(0f, h - 1f, w, h - 1f, dividerPaint)

        val cy = h / 2f

        // ── Settings (right) ─────────────────────────────────────────────────
        val settingsLabel = "\u2699"
        canvas.drawText(settingsLabel, w - hPad, cy + settingsPaint.textSize / 3f, settingsPaint)
        val settingsBlockW = settingsPaint.measureText(settingsLabel) + hPad * 2f
        settingsTapBounds = RectF(w - settingsBlockW, 0f, w, h)

        // ── Style toggle (right-centre) ───────────────────────────────────────
        val styleLabel = if (isStyleActive) "Style ON" else "Style OFF"
        val stylePaint = if (isStyleActive) styleOnPaint else styleOffPaint
        val dotPaint   = if (isStyleActive) activeDotPaint else inactiveDotPaint

        val settingsEdge = settingsTapBounds.left - hPad
        val styleLabelW  = stylePaint.measureText(styleLabel)
        val dotDiameter  = dotRadius * 2
        val styleBlockW  = dotDiameter + context.dpToPx(4) + styleLabelW
        val styleStartX  = settingsEdge - styleBlockW - hPad

        canvas.drawCircle(styleStartX + dotRadius, cy, dotRadius, dotPaint)
        canvas.drawText(
            styleLabel,
            styleStartX + dotDiameter + context.dpToPx(4),
            cy + stylePaint.textSize / 3f,
            stylePaint
        )
        styleTapBounds = RectF(styleStartX - hPad, 0f, settingsEdge, h)

        // ── Avatar + alias name (left) ────────────────────────────────────────
        val avatarCx = hPad + avatarRadius
        canvas.drawCircle(avatarCx, cy, avatarRadius, avatarBgPaint)
        canvas.drawText(
            initials(activeAliasName),
            avatarCx,
            cy + avatarTextPaint.textSize / 3f,
            avatarTextPaint
        )

        val nameX = avatarCx + avatarRadius + context.dpToPx(8)
        val label = (activeAliasName ?: "No alias") + "  \u25BE"
        val nameAvailW = styleStartX - hPad - nameX
        canvas.drawText(clipText(label, aliasNamePaint, nameAvailW), nameX, cy + aliasNamePaint.textSize / 3f, aliasNamePaint)

        aliasTapBounds = RectF(0f, 0f, styleStartX - hPad, h)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked != MotionEvent.ACTION_UP) return true
        val x = event.x; val y = event.y
        when {
            settingsTapBounds.contains(x, y) -> listener?.onSettingsTapped()
            styleTapBounds.contains(x, y)    -> listener?.onStyleToggleTapped()
            aliasTapBounds.contains(x, y)    -> listener?.onAliasNameTapped()
        }
        return true
    }

    private fun initials(name: String?): String {
        if (name.isNullOrBlank()) return "?"
        val words = name.trim().split("\\s+".toRegex())
        return when {
            words.size >= 2 -> "${words[0].first().uppercaseChar()}${words[1].first().uppercaseChar()}"
            words.size == 1 -> words[0].take(2).uppercase()
            else -> "?"
        }
    }

    private fun clipText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        val ellipsis = "\u2026"
        val ellipsisW = paint.measureText(ellipsis)
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end)) + ellipsisW > maxWidth) end--
        return text.substring(0, end) + ellipsis
    }
}
