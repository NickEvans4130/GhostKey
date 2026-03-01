package com.ghostkey.transform.tier1

// Implemented in feature/tier1-full
object PunctuationProcessor {
    fun processEllipsis(text: String, frequency: Float, random: java.util.Random): String = text
    fun processExclamation(text: String, frequency: Float, random: java.util.Random): String = text
    fun processEmDash(text: String, emDashVsParentheses: Float): String = text
    fun processOxfordComma(text: String, enabled: Boolean): String = text
}
