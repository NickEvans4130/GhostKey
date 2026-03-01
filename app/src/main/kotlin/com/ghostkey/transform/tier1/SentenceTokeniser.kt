package com.ghostkey.transform.tier1

object SentenceTokeniser {

    private val ABBREVIATIONS = setOf(
        "mr", "mrs", "ms", "dr", "prof", "sr", "jr", "vs", "etc", "inc",
        "corp", "ltd", "approx", "est", "dept", "fig", "govt", "min", "max",
        "no", "vol", "ch", "pp", "ed", "rev", "gen", "col", "sgt", "cpl"
    )

    fun tokenise(text: String): List<String> {
        if (text.isBlank()) return emptyList()

        val sentences = mutableListOf<String>()
        val current = StringBuilder()
        var i = 0

        while (i < text.length) {
            val ch = text[i]
            current.append(ch)

            if (ch == '.' || ch == '?' || ch == '!') {
                val isEnd = when (ch) {
                    '.' -> !isAbbreviationBoundary(current.toString()) && isFollowedBySpace(text, i)
                    else -> true
                }
                if (isEnd) {
                    val sentence = current.toString().trim()
                    if (sentence.isNotBlank()) sentences.add(sentence)
                    current.clear()
                }
            }
            i++
        }

        if (current.isNotBlank()) sentences.add(current.toString().trim())
        return sentences
    }

    private fun isAbbreviationBoundary(text: String): Boolean {
        val words = text.trim().split(Regex("\\s+"))
        val lastWord = words.lastOrNull()?.trimEnd('.') ?: return false
        return lastWord.lowercase() in ABBREVIATIONS || lastWord.length == 1
    }

    private fun isFollowedBySpace(text: String, index: Int): Boolean {
        val next = index + 1
        return next >= text.length || text[next] == ' ' || text[next] == '\n'
    }
}
