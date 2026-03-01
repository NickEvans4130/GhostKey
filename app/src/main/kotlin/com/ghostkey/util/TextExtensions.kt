package com.ghostkey.util

fun String.wordCount(): Int = trim().split(Regex("\\s+")).count { it.isNotBlank() }

fun String.sentences(): List<String> = com.ghostkey.transform.tier1.SentenceTokeniser.tokenise(this)

fun String.containsIgnoreCase(other: String): Boolean = contains(other, ignoreCase = true)

fun String.replaceIgnoreCase(old: String, new: String): String {
    val regex = Regex(Regex.escape(old), RegexOption.IGNORE_CASE)
    return regex.replace(this, new)
}
