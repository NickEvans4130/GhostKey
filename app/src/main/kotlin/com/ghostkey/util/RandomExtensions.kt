package com.ghostkey.util

import com.ghostkey.data.profile.StyleProfile
import java.util.Random

fun StyleProfile.profileRandom(): Random = Random(id.hashCode().toLong())

fun Random.chance(probability: Float): Boolean = nextFloat() < probability
