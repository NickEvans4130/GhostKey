package com.ghostkey.data.alias

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
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
        ) ?: throw IllegalStateException("GhostID ContentProvider unavailable — install GhostID and grant permission")

        val aliases = mutableListOf<AliasCache>()
        cursor.use {
            // Columns from GhostID's GhostKeyProvider:
            // _id, alias_name, photo_path, nationality, date_of_birth, occupation, accent_colour, gender
            val idIdx = it.getColumnIndexOrThrow("_id")
            val nameIdx = it.getColumnIndexOrThrow("alias_name")
            val photoIdx = it.getColumnIndex("photo_path")
            val natIdx = it.getColumnIndex("nationality")
            val dobIdx = it.getColumnIndex("date_of_birth")
            val occIdx = it.getColumnIndex("occupation")
            val colourIdx = it.getColumnIndex("accent_colour")
            val genderIdx = it.getColumnIndex("gender")

            while (it.moveToNext()) {
                val dob = if (dobIdx >= 0) it.getString(dobIdx) else null
                val accentInt = if (colourIdx >= 0) it.getString(colourIdx) else null
                aliases.add(
                    AliasCache(
                        id = it.getString(idIdx),
                        aliasName = it.getString(nameIdx),
                        avatarUrl = if (photoIdx >= 0) it.getString(photoIdx) else null,
                        nationality = if (natIdx >= 0) it.getString(natIdx) else null,
                        age = dob?.let { d -> ageFromDob(d) },
                        occupation = if (occIdx >= 0) it.getString(occIdx) else null,
                        educationLevel = null,
                        accentColour = accentInt?.let { i -> argbIntToHex(i) }
                    )
                )
            }
        }

        aliasCacheDao.clearAll()
        aliasCacheDao.insertAll(aliases)
        Log.d(TAG, "Synced ${aliases.size} aliases from GhostID")
        aliases
    }.onFailure { e ->
        Log.w(TAG, "GhostID sync failed: ${e.message}")
    }

    private fun ageFromDob(dob: String): Int? = runCatching {
        val parts = dob.split("-")
        val birthYear = parts[0].toInt()
        val birthMonth = parts[1].toInt()
        val birthDay = parts[2].toInt()
        val cal = Calendar.getInstance()
        var age = cal.get(Calendar.YEAR) - birthYear
        if (cal.get(Calendar.MONTH) + 1 < birthMonth ||
            (cal.get(Calendar.MONTH) + 1 == birthMonth && cal.get(Calendar.DAY_OF_MONTH) < birthDay)
        ) age--
        age
    }.getOrNull()

    private fun argbIntToHex(intStr: String): String? = runCatching {
        "#%08X".format(intStr.toInt())
    }.getOrNull()
}
