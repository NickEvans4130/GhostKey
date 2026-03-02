package com.ghostkey.ime

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.inputmethodservice.InputMethodService
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.ghostkey.ime.alias.AliasBarView
import com.ghostkey.ime.keyboard.Key
import com.ghostkey.ime.keyboard.KeyboardMode
import com.ghostkey.ime.keyboard.KeyboardState
import com.ghostkey.ime.keyboard.KeyboardView
import com.ghostkey.ime.keyboard.ShiftState
import com.ghostkey.ime.suggestion.SuggestionStripView
import com.ghostkey.transform.TransformService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GhostKeyIME : InputMethodService() {

    companion object {
        private const val TAG = "GhostKeyIME"
        private const val DOUBLE_TAP_MILLIS = 400L
    }

    private var transformService: TransformService? = null
    private var keyboardView: KeyboardView? = null
    private var suggestionStripView: SuggestionStripView? = null
    private var aliasBarView: AliasBarView? = null
    private var keyboardState = KeyboardState()

    // Double-tap shift → caps lock
    private var lastShiftTapMs = 0L

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            transformService = (binder as? TransformService.TransformBinder)?.getService()
            Log.d(TAG, "TransformService connected")
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            transformService = null
        }
    }

    override fun onCreate() {
        super.onCreate()
        bindService(
            Intent(this, TransformService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onCreateInputView(): View {
        // Ensure the keyboard window fills full width, wraps height
        window?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        aliasBarView = AliasBarView(this).also { root.addView(it) }
        suggestionStripView = SuggestionStripView(this).also { root.addView(it) }
        keyboardView = KeyboardView(this).apply {
            keyListener = object : KeyboardView.KeyListener {
                override fun onKey(key: Key) = handleKey(key)
                override fun onShiftTap() = handleShift()
                override fun onBackspace() = handleBackspace()
            }
            state = keyboardState
        }.also { root.addView(it) }

        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Reset shift after each field focus (unless caps lock)
        if (keyboardState.shiftState == ShiftState.SINGLE) {
            keyboardState = keyboardState.copy(shiftState = ShiftState.OFF)
            keyboardView?.state = keyboardState
        }
    }

    // ── Key handling ─────────────────────────────────────────────────────────

    private fun handleKey(key: Key) {
        val ic = currentInputConnection ?: return

        when (key.label) {
            "123" -> {
                keyboardState = keyboardState.copy(mode = KeyboardMode.NUMERIC)
                keyboardView?.state = keyboardState
                return
            }
            "ABC" -> {
                keyboardState = keyboardState.copy(mode = KeyboardMode.ALPHA)
                keyboardView?.state = keyboardState
                return
            }
            "↵" -> {
                // Respect the field's requested action (Search, Done, Send, etc.)
                val action = currentInputEditorInfo?.imeOptions
                    ?.and(EditorInfo.IME_MASK_ACTION)
                    ?: EditorInfo.IME_ACTION_NONE

                if (action != EditorInfo.IME_ACTION_NONE &&
                    action != EditorInfo.IME_ACTION_UNSPECIFIED
                ) {
                    ic.performEditorAction(action)
                } else {
                    ic.commitText("\n", 1)
                }
                return
            }
            " " -> {
                ic.commitText(" ", 1)
                // Don't reset shift on space
                return
            }
        }

        // Normal character — label already reflects shift state from KeyboardLayout
        ic.commitText(key.label, 1)
        keyboardState = keyboardState.afterCharacterTyped()
        keyboardView?.state = keyboardState
    }

    private fun handleShift() {
        val now = SystemClock.elapsedRealtime()
        val isSingleShift = keyboardState.shiftState == ShiftState.SINGLE

        keyboardState = if (isSingleShift && now - lastShiftTapMs < DOUBLE_TAP_MILLIS) {
            // Double-tap on SINGLE → caps lock
            keyboardState.copy(shiftState = ShiftState.CAPS_LOCK)
        } else {
            keyboardState.withShiftToggled()
        }

        lastShiftTapMs = now
        keyboardView?.state = keyboardState
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return
        // If there's a selection, delete it; otherwise delete one character
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { unbindService(serviceConnection) }
    }
}
