package com.ghostkey.ime.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import com.ghostkey.util.dpToPx

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

    private val keyHeight   = context.dpToPx(48)
    private val keySpacing  = context.dpToPx(4)
    private val rowPadding  = context.dpToPx(8)
    private val cornerRadius = context.dpToPx(6).toFloat()

    // ── Paints ────────────────────────────────────────────────────────────────

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0D1117.toInt()
    }
    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF161B22.toInt()
    }
    private val keyPressedBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF2D333B.toInt()
    }
    private val specialKeyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF010409.toInt()
    }
    private val specialKeyPressedBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0D1117.toInt()
    }
    // Shift: single-shift tint (dark blue)
    private val shiftSingleBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0C2044.toInt()
    }
    // Shift: caps-lock tint (full accent)
    private val shiftCapsLockBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1F6FEB.toInt()
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
    private val shiftActiveLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF58A6FF.toInt()
        textSize = context.dpToPx(16).toFloat()
        textAlign = Paint.Align.CENTER
    }
    private val shiftCapsLockLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = context.dpToPx(16).toFloat()
        textAlign = Paint.Align.CENTER
    }
    private val smallLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE6EDF3.toInt()
        textSize = context.dpToPx(13).toFloat()
        textAlign = Paint.Align.CENTER
    }

    // ── State ─────────────────────────────────────────────────────────────────

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

    // ── Drawing ───────────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        keys.forEachIndexed { idx, key ->
            val isPressed = idx == pressedKeyIndex
            val bg = resolveKeyBg(key, isPressed)
            canvas.drawRoundRect(key.bounds, cornerRadius, cornerRadius, bg)
            canvas.drawRoundRect(key.bounds, cornerRadius, cornerRadius, keyBorderPaint)

            val cx = key.bounds.centerX()
            val cy = key.bounds.centerY() -
                    (keyLabelPaint.descent() + keyLabelPaint.ascent()) / 2

            val labelPaint = resolveLabelPaint(key)
            val label = resolveDisplayLabel(key)
            canvas.drawText(label, cx, cy, labelPaint)
        }
    }

    private fun resolveKeyBg(key: Key, pressed: Boolean): Paint {
        if (key.label == "⇧") return when (state.shiftState) {
            ShiftState.SINGLE    -> if (pressed) shiftSingleBgPaint else shiftSingleBgPaint
            ShiftState.CAPS_LOCK -> shiftCapsLockBgPaint
            ShiftState.OFF       -> if (pressed) specialKeyPressedBgPaint else specialKeyBgPaint
        }
        return when {
            key.isSpecial -> if (pressed) specialKeyPressedBgPaint else specialKeyBgPaint
            pressed       -> keyPressedBgPaint
            else          -> keyBgPaint
        }
    }

    private fun resolveLabelPaint(key: Key): Paint = when {
        key.label == "⇧" && state.shiftState == ShiftState.SINGLE    -> shiftActiveLabelPaint
        key.label == "⇧" && state.shiftState == ShiftState.CAPS_LOCK -> shiftCapsLockLabelPaint
        key.label in setOf("123", "ABC", "#+=")                       -> smallLabelPaint
        else                                                           -> keyLabelPaint
    }

    private fun resolveDisplayLabel(key: Key): String = when (key.label) {
        "⇧" -> when (state.shiftState) {
            ShiftState.CAPS_LOCK -> "⇪"
            else -> "⇧"
        }
        else -> key.label
    }

    // ── Touch ─────────────────────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pIdx = event.actionIndex
        val x = event.getX(pIdx)
        val y = event.getY(pIdx)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val idx = findKeyAt(x, y)
                pressedKeyIndex = idx
                if (idx >= 0) haptic()
                invalidate()
            }

            // Secondary finger down — fire immediately (no up tracking for extra pointers)
            MotionEvent.ACTION_POINTER_DOWN -> {
                val idx = findKeyAt(x, y)
                if (idx >= 0) {
                    haptic()
                    dispatchKey(keys[idx])
                }
            }

            MotionEvent.ACTION_MOVE -> {
                // Update visual highlight as finger slides
                val idx = findKeyAt(event.x, event.y)
                if (idx != pressedKeyIndex) {
                    pressedKeyIndex = idx
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {
                val idx = findKeyAt(x, y)
                if (idx >= 0) dispatchKey(keys[idx])
                pressedKeyIndex = -1
                invalidate()
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val idx = findKeyAt(x, y)
                if (idx >= 0) dispatchKey(keys[idx])
            }

            MotionEvent.ACTION_CANCEL -> {
                pressedKeyIndex = -1
                invalidate()
            }
        }
        return true
    }

    private fun haptic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } else {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    private fun findKeyAt(x: Float, y: Float): Int =
        keys.indexOfFirst { it.bounds.contains(x, y) }

    private fun dispatchKey(key: Key) {
        when (key.label) {
            "⇧"  -> keyListener?.onShiftTap()
            "⌫"  -> keyListener?.onBackspace()
            else -> keyListener?.onKey(key)
        }
    }
}
