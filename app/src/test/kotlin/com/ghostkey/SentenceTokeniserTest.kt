package com.ghostkey

import com.ghostkey.transform.tier1.SentenceTokeniser
import org.junit.Assert.assertEquals
import org.junit.Test

class SentenceTokeniserTest {

    @Test
    fun `simple sentences tokenise correctly`() {
        val text = "Hello world. How are you? I am fine!"
        val sentences = SentenceTokeniser.tokenise(text)
        assertEquals(3, sentences.size)
        assertEquals("Hello world.", sentences[0])
        assertEquals("How are you?", sentences[1])
        assertEquals("I am fine!", sentences[2])
    }

    @Test
    fun `abbreviations are not treated as sentence boundaries`() {
        val text = "Dr. Smith works here. He is great."
        val sentences = SentenceTokeniser.tokenise(text)
        assertEquals(2, sentences.size)
    }

    @Test
    fun `empty input returns empty list`() {
        assertEquals(emptyList<String>(), SentenceTokeniser.tokenise(""))
        assertEquals(emptyList<String>(), SentenceTokeniser.tokenise("   "))
    }

    @Test
    fun `single sentence without terminal punctuation is returned`() {
        val text = "This sentence has no full stop"
        val sentences = SentenceTokeniser.tokenise(text)
        assertEquals(1, sentences.size)
        assertEquals(text, sentences[0])
    }
}
