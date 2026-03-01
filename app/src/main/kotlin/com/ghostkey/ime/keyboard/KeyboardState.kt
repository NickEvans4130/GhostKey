package com.ghostkey.ime.keyboard

enum class ShiftState { OFF, SINGLE, CAPS_LOCK }
enum class KeyboardMode { ALPHA, NUMERIC, SYMBOL }

data class KeyboardState(
    val shiftState: ShiftState = ShiftState.OFF,
    val mode: KeyboardMode = KeyboardMode.ALPHA
) {
    fun withShiftToggled(): KeyboardState = copy(
        shiftState = when (shiftState) {
            ShiftState.OFF -> ShiftState.SINGLE
            ShiftState.SINGLE -> ShiftState.CAPS_LOCK
            ShiftState.CAPS_LOCK -> ShiftState.OFF
        }
    )

    fun afterCharacterTyped(): KeyboardState = if (shiftState == ShiftState.SINGLE) {
        copy(shiftState = ShiftState.OFF)
    } else this
}
