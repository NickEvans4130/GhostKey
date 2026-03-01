package com.ghostkey.util

import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputConnection

private val mainHandler = Handler(Looper.getMainLooper())

fun InputConnection.getTextBeforeCursorSafe(maxLength: Int): CharSequence? =
    runCatching { getTextBeforeCursor(maxLength, 0) }.getOrNull()

fun InputConnection.getTextAfterCursorSafe(maxLength: Int): CharSequence? =
    runCatching { getTextAfterCursor(maxLength, 0) }.getOrNull()

fun InputConnection.replaceTextOnMain(
    deleteBeforeCount: Int,
    newText: String,
    onMain: () -> Unit = {}
) {
    mainHandler.post {
        beginBatchEdit()
        deleteSurroundingText(deleteBeforeCount, 0)
        commitText(newText, 1)
        endBatchEdit()
        onMain()
    }
}
