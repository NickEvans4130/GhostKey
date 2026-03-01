package com.ghostkey.transform.stylometry

import com.ghostkey.data.baseline.UserStyleBaseline
import com.ghostkey.data.profile.StyleProfile
import javax.inject.Inject
import javax.inject.Singleton

// Full implementation in feature/stylometry
@Singleton
class DivergenceCalculator @Inject constructor() {

    fun calculate(baseline: UserStyleBaseline, profile: StyleProfile): Float {
        // Returns 0..1 where 1 = maximum divergence (best obfuscation)
        // Full metric weighting implemented in feature/stylometry
        return 0f
    }
}
