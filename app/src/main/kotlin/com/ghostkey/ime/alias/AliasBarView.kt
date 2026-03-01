package com.ghostkey.ime.alias

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.ghostkey.data.alias.AliasCache
import com.ghostkey.util.dpToPx

// Canvas-drawn alias bar — full implementation in feature/ghostid-integration
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
    var activeAlias: AliasCache? = null
        set(value) {
            field = value
            invalidate()
        }
    var isStyleActive: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    private val barHeight = context.dpToPx(48)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF010409.toInt() }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE6EDF3.toInt()
        textSize = context.dpToPx(14).toFloat()
    }
    private val activeDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF58A6FF.toInt() }
    private val inactiveDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF8B949E.toInt() }
    private val avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF161B22.toInt() }
    private val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF30363D.toInt()
        strokeWidth = 1f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), barHeight)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), barHeight.toFloat(), bgPaint)
        canvas.drawLine(0f, barHeight - 1f, width.toFloat(), barHeight - 1f, dividerPaint)

        val cy = barHeight / 2f
        val padding = context.dpToPx(12).toFloat()
        val avatarRadius = context.dpToPx(14).toFloat()

        // Avatar circle
        canvas.drawCircle(padding + avatarRadius, cy, avatarRadius, avatarPaint)

        // Alias name
        val alias = activeAlias
        val aliasLabel = if (alias != null) "${alias.aliasName} v" else "No alias selected v"
        canvas.drawText(aliasLabel, padding + avatarRadius * 2 + context.dpToPx(8), cy + avatarRadius / 2, textPaint)

        // Style toggle indicator
        val dotX = width / 2f
        val dotPaint = if (isStyleActive) activeDotPaint else inactiveDotPaint
        canvas.drawCircle(dotX, cy, context.dpToPx(5).toFloat(), dotPaint)
        val styleLabel = if (isStyleActive) "Style: ON" else "Style: OFF"
        canvas.drawText(styleLabel, dotX + context.dpToPx(10), cy + context.dpToPx(5), textPaint)

        // Settings gear placeholder
        canvas.drawText("Settings", width - context.dpToPx(60).toFloat(), cy + context.dpToPx(5), textPaint)
    }
}
