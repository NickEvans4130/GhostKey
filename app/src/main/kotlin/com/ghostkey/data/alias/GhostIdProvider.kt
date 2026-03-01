package com.ghostkey.data.alias

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GhostIdProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aliasCacheDao: AliasCacheDao
) {
    companion object {
        private const val TAG = "GhostIdProvider"
        private const val AUTHORITY = "com.ghostid.aliases"
        private val LIST_URI = Uri.parse("content://$AUTHORITY/list")
    }

    suspend fun syncAliases(): Result<List<AliasCache>> = runCatching {
        val cursor = context.contentResolver.query(
            LIST_URI, null, null, null, null
        ) ?: throw IllegalStateException("GhostID ContentProvider unavailable")

        val aliases = mutableListOf<AliasCache>()
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow("_id")
            val nameIdx = it.getColumnIndexOrThrow("alias_name")
            val avatarIdx = it.getColumnIndex("avatar_url")
            val natIdx = it.getColumnIndex("nationality")
            val ageIdx = it.getColumnIndex("age")
            val occIdx = it.getColumnIndex("occupation")
            val eduIdx = it.getColumnIndex("education_level")
            val colourIdx = it.getColumnIndex("accent_colour")

            while (it.moveToNext()) {
                aliases.add(
                    AliasCache(
                        id = it.getString(idIdx),
                        aliasName = it.getString(nameIdx),
                        avatarUrl = if (avatarIdx >= 0) it.getString(avatarIdx) else null,
                        nationality = if (natIdx >= 0) it.getString(natIdx) else null,
                        age = if (ageIdx >= 0 && !it.isNull(ageIdx)) it.getInt(ageIdx) else null,
                        occupation = if (occIdx >= 0) it.getString(occIdx) else null,
                        educationLevel = if (eduIdx >= 0) it.getString(eduIdx) else null,
                        accentColour = if (colourIdx >= 0) it.getString(colourIdx) else null
                    )
                )
            }
        }

        aliasCacheDao.clearAll()
        aliasCacheDao.insertAll(aliases)
        Log.d(TAG, "Synced ${aliases.size} aliases from GhostID")
        aliases
    }.onFailure { e ->
        Log.w(TAG, "GhostID sync failed — using local cache: ${e.message}")
    }
}
