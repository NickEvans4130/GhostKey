package com.ghostkey.ime.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ghostkey.util.dpToPx

// Canvas-drawn keyboard view — full implementation in feature/keyboard-view
class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    interface KeyListener {
        fun onKey(key: Key)
        fun onShiftTap()
        fun onBackspace()
    }

    var keyListener: KeyListener? = null
    var state: KeyboardState = KeyboardState()
        set(value) {
            field = value
            rebuildKeys()
            invalidate()
        }

    private val keyHeight = context.dpToPx(48)
    private val keySpacing = context.dpToPx(4)
    private val rowPadding = context.dpToPx(8)
    private val cornerRadius = context.dpToPx(6).toFloat()

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0D1117.toInt()
    }
    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF161B22.toInt()
    }
    private val keyBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF30363D.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    private val keyLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE6EDF3.toInt()
        textSize = context.dpToPx(16).toFloat()
        textAlign = Paint.Align.CENTER
    }
    private val specialKeyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF010409.toInt()
    }

    private var layout: KeyboardLayout? = null
    private var keys: List<Key> = emptyList()
    private var pressedKeyIndex: Int = -1

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        layout = KeyboardLayout(w, keyHeight, keySpacing, rowPadding)
        setMeasuredDimension(w, layout!!.totalHeight())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        layout = KeyboardLayout(w, keyHeight, keySpacing, rowPadding)
        rebuildKeys()
    }

    private fun rebuildKeys() {
        layout?.let { keys = it.buildKeys(state.mode, state.shiftState) }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        keys.forEachIndexed { idx, key ->
            val bg = if (key.isSpecial) specialKeyBgPaint else keyBgPaint
            val pressed = idx == pressedKeyIndex
            if (pressed) {
                val pressedBg = Paint(bg).apply { color = (color and 0x00FFFFFF.toInt()) or 0x22FFFFFF.toInt() }
                canvas.drawRoundRect(key.bounds, cornerRadius, cornerRadius, pressedBg)
            } else {
                canvas.drawRoundRect(key.bounds, cornerRadius, cornerRadius, bg)
            }
            canvas.drawRoundRect(key.bounds, cornerRadius, cornerRadius, keyBorderPaint)

            val cx = key.bounds.centerX()
            val cy = key.bounds.centerY() - (keyLabelPaint.descent() + keyLabelPaint.ascent()) / 2
            canvas.drawText(key.label, cx, cy, keyLabelPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val idx = findKeyAt(event.x, event.y)
                pressedKeyIndex = idx
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val idx = findKeyAt(event.x, event.y)
                if (idx >= 0) dispatchKey(keys[idx])
                pressedKeyIndex = -1
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                pressedKeyIndex = -1
                invalidate()
            }
        }
        return true
    }

    private fun findKeyAt(x: Float, y: Float): Int =
        keys.indexOfFirst { it.bounds.contains(x, y) }

    private fun dispatchKey(key: Key) {
        when (key.label) {
            "⇧" -> keyListener?.onShiftTap()
            "⌫" -> keyListener?.onBackspace()
            else -> keyListener?.onKey(key)
        }
    }
}
