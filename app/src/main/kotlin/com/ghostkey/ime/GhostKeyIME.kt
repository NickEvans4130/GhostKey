package com.ghostkey.ime

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.PixelFormat
import android.inputmethodservice.InputMethodService
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.ghostkey.data.profile.StyleProfile
import com.ghostkey.data.profile.StyleProfileRepository
import com.ghostkey.ime.alias.AliasBarView
import com.ghostkey.ime.keyboard.Key
import com.ghostkey.ime.keyboard.KeyboardMode
import com.ghostkey.ime.keyboard.KeyboardState
import com.ghostkey.ime.keyboard.KeyboardView
import com.ghostkey.ime.keyboard.ShiftState
import com.ghostkey.ime.suggestion.SuggestionStripView
import com.ghostkey.transform.TransformService
import com.ghostkey.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GhostKeyIME : InputMethodService() {

    companion object {
        private const val TAG = "GhostKeyIME"
        private const val DOUBLE_TAP_MILLIS = 400L
    }

    @Inject lateinit var profileRepository: StyleProfileRepository

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var transformService: TransformService? = null

    // View references
    private var rootView: LinearLayout? = null
    private var keyboardContent: LinearLayout? = null
    private var keyboardView: KeyboardView? = null
    private var suggestionStripView: SuggestionStripView? = null
    private var aliasBarView: AliasBarView? = null
    private var switcherPanel: View? = null

    // State
    private var profiles: List<StyleProfile> = emptyList()
    private var activeProfile: StyleProfile? = null
    private var isStyleActive = true
    private var keyboardState = KeyboardState()
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
        scope.launch {
            profileRepository.getAllProfiles().collect { list ->
                profiles = list
                if (activeProfile == null || list.none { it.id == activeProfile?.id }) {
                    activeProfile = list.firstOrNull()
                }
                aliasBarView?.activeAliasName = activeProfile?.aliasName
            }
        }
    }

    // Never go fullscreen — prevents system from overlaying its own IME chrome
    override fun onEvaluateFullscreenMode(): Boolean = false


    override fun onCreateInputView(): View {
        // Solid opaque window so no other keyboard shows through
        window?.window?.let { win ->
            win.setLayout(MATCH_PARENT, WRAP_CONTENT)
            win.setFormat(PixelFormat.OPAQUE)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0D1117.toInt())
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
        rootView = root

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
        keyboardContent = content

        aliasBarView = AliasBarView(this).apply {
            activeAliasName = activeProfile?.aliasName
            isStyleActive = this@GhostKeyIME.isStyleActive
            listener = aliasBarListener
        }.also { content.addView(it) }

        suggestionStripView = SuggestionStripView(this).also { content.addView(it) }

        keyboardView = KeyboardView(this).apply {
            keyListener = keyboardKeyListener
            state = keyboardState
        }.also { content.addView(it) }

        root.addView(content)
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (keyboardState.shiftState == ShiftState.SINGLE) {
            keyboardState = keyboardState.copy(shiftState = ShiftState.OFF)
            keyboardView?.state = keyboardState
        }
        // Dismiss any open switcher panel on field change
        hideAliasSwitcher()
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    private val aliasBarListener = object : AliasBarView.AliasBarListener {
        override fun onAliasNameTapped() = showAliasSwitcher()
        override fun onStyleToggleTapped() = toggleStyle()
        override fun onSettingsTapped() = openCompanionApp()
    }

    // Named differently from KeyboardView.keyListener to avoid shadowing in apply { }
    private val keyboardKeyListener = object : KeyboardView.KeyListener {
        override fun onKey(key: Key) = handleKey(key)
        override fun onShiftTap() = handleShift()
        override fun onBackspace() = handleBackspace()
    }

    // ── Alias switcher panel ───────────────────────────────────────────────────

    private fun showAliasSwitcher() {
        if (switcherPanel != null) return
        val root = rootView ?: return

        val dp = resources.displayMetrics.density
        val hPad = (16 * dp).toInt()
        val vPad = (10 * dp).toInt()

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0D1117.toInt())
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }

        // Header label
        TextView(this).apply {
            text = "Switch profile"
            setTextColor(0xFF8B949E.toInt())
            textSize = 12f
            setPadding(hPad, vPad, hPad, vPad)
        }.also { panel.addView(it) }

        dividerView(0xFF30363D.toInt()).also { panel.addView(it) }

        // Profile list (scrollable if many profiles)
        val listContainer = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).also {
                it.weight = 0f
            }
        }
        val listInner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        listContainer.addView(listInner)

        if (profiles.isEmpty()) {
            TextView(this).apply {
                text = "No profiles. Create one in the GhostKey app."
                setTextColor(0xFF8B949E.toInt())
                textSize = 13f
                setPadding(hPad, vPad * 2, hPad, vPad * 2)
            }.also { listInner.addView(it) }
        } else {
            profiles.forEach { profile ->
                val isActive = profile.id == activeProfile?.id
                TextView(this).apply {
                    text = profile.aliasName
                    setTextColor(if (isActive) 0xFF58A6FF.toInt() else 0xFFE6EDF3.toInt())
                    textSize = 15f
                    setPadding(hPad, vPad + (6 * dp).toInt(), hPad, vPad + (6 * dp).toInt())
                    isClickable = true
                    isFocusable = false
                    setOnClickListener {
                        selectProfile(profile)
                        hideAliasSwitcher()
                    }
                }.also { listInner.addView(it) }
                dividerView(0xFF21262D.toInt()).also { listInner.addView(it) }
            }
        }
        panel.addView(listContainer)

        dividerView(0xFF30363D.toInt()).also { panel.addView(it) }

        // Cancel
        TextView(this).apply {
            text = "Cancel"
            setTextColor(0xFF8B949E.toInt())
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(hPad, vPad, hPad, vPad)
            isClickable = true
            isFocusable = false
            setOnClickListener { hideAliasSwitcher() }
        }.also { panel.addView(it) }

        switcherPanel = panel
        keyboardContent?.visibility = View.GONE
        root.addView(panel)
    }

    private fun hideAliasSwitcher() {
        val panel = switcherPanel ?: return
        rootView?.removeView(panel)
        keyboardContent?.visibility = View.VISIBLE
        switcherPanel = null
    }

    private fun dividerView(colour: Int): View = View(this).apply {
        setBackgroundColor(colour)
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 1)
    }

    private fun selectProfile(profile: StyleProfile) {
        activeProfile = profile
        aliasBarView?.activeAliasName = profile.aliasName
    }

    private fun toggleStyle() {
        isStyleActive = !isStyleActive
        aliasBarView?.isStyleActive = isStyleActive
    }

    private fun openCompanionApp() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        })
    }

    // ── Key handling ──────────────────────────────────────────────────────────

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
            "#+=" -> {
                keyboardState = keyboardState.copy(mode = KeyboardMode.SYMBOL)
                keyboardView?.state = keyboardState
                return
            }
            "↵" -> {
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
                return
            }
        }

        ic.commitText(key.label, 1)
        keyboardState = keyboardState.afterCharacterTyped()
        keyboardView?.state = keyboardState
    }

    private fun handleShift() {
        val now = SystemClock.elapsedRealtime()
        val isSingleShift = keyboardState.shiftState == ShiftState.SINGLE
        keyboardState = if (isSingleShift && now - lastShiftTapMs < DOUBLE_TAP_MILLIS) {
            keyboardState.copy(shiftState = ShiftState.CAPS_LOCK)
        } else {
            keyboardState.withShiftToggled()
        }
        lastShiftTapMs = now
        keyboardView?.state = keyboardState
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        runCatching { unbindService(serviceConnection) }
    }
}
