package com.ghostkey.ime

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.inputmethodservice.InputMethodService
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.ghostkey.ime.alias.AliasBarView
import com.ghostkey.ime.keyboard.KeyboardState
import com.ghostkey.ime.keyboard.KeyboardView
import com.ghostkey.ime.keyboard.Key
import com.ghostkey.ime.suggestion.SuggestionStripView
import com.ghostkey.transform.TransformService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GhostKeyIME : InputMethodService() {

    companion object {
        private const val TAG = "GhostKeyIME"
    }

    private var transformService: TransformService? = null
    private var keyboardView: KeyboardView? = null
    private var suggestionStripView: SuggestionStripView? = null
    private var aliasBarView: AliasBarView? = null
    private var keyboardState = KeyboardState()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            transformService = (binder as? TransformService.TransformBinder)?.getService()
            Log.d(TAG, "TransformService connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            transformService = null
            Log.d(TAG, "TransformService disconnected")
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
        }.also { root.addView(it) }

        return root
    }

    private fun handleKey(key: Key) {
        val ic = currentInputConnection ?: return
        val char = when (key.label) {
            " " -> " "
            "↵" -> "\n"
            else -> key.label
        }
        ic.commitText(char, 1)
        keyboardState = keyboardState.afterCharacterTyped()
        keyboardView?.state = keyboardState
    }

    private fun handleShift() {
        keyboardState = keyboardState.withShiftToggled()
        keyboardView?.state = keyboardState
    }

    private fun handleBackspace() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}
