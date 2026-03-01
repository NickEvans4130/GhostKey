package com.ghostkey.transform

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.ghostkey.data.alias.GhostIdProvider
import com.ghostkey.data.profile.StyleProfile
import com.ghostkey.transform.stylometry.StylometryAnalyser
import com.ghostkey.transform.tier1.Tier1Engine
import com.ghostkey.transform.tier1.TransformResult
import com.ghostkey.transform.tier2.Tier2Engine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TransformService : Service() {

    companion object {
        private const val TAG = "TransformService"
        const val ACTION_ALIAS_UPDATED = "com.ghostid.ACTION_ALIAS_UPDATED"
    }

    @Inject lateinit var tier1Engine: Tier1Engine
    @Inject lateinit var tier2Engine: Tier2Engine
    @Inject lateinit var stylometryAnalyser: StylometryAnalyser
    @Inject lateinit var ghostIdProvider: GhostIdProvider

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val binder = TransformBinder()

    var activeProfile: StyleProfile? = null
    var isStyleActive: Boolean = true

    private val aliasUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_ALIAS_UPDATED) {
                Log.d(TAG, "Alias update broadcast received — refreshing cache")
                serviceScope.launch { ghostIdProvider.syncAliases() }
            }
        }
    }

    inner class TransformBinder : Binder() {
        fun getService(): TransformService = this@TransformService
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(
            aliasUpdateReceiver,
            IntentFilter(ACTION_ALIAS_UPDATED),
            RECEIVER_NOT_EXPORTED
        )
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(aliasUpdateReceiver)
    }

    fun applyTier1(text: String): TransformResult {
        val profile = activeProfile
        if (!isStyleActive || profile == null) return TransformResult(text, text)
        val result = tier1Engine.transform(text, profile)
        stylometryAnalyser.onTextCommitted(text)
        return result
    }

    suspend fun requestTier2Rewrite(paragraph: String): Result<String> {
        val profile = activeProfile
            ?: return Result.failure(IllegalStateException("No active profile"))
        return tier2Engine.rewrite(paragraph, profile)
    }
}
