package com.ghostkey.data.profile

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StyleProfileRepository @Inject constructor(
    private val dao: StyleProfileDao
) {
    fun getAllProfiles(): Flow<List<StyleProfile>> = dao.getAllProfiles()

    suspend fun getProfileById(id: String): StyleProfile? = dao.getProfileById(id)

    suspend fun saveProfile(profile: StyleProfile) = dao.insertProfile(profile)

    suspend fun deleteProfile(profile: StyleProfile) = dao.deleteProfile(profile)
}
