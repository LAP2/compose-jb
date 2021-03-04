package org.jetbrains.compose.colorpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent

interface ColorCircleState {
    var color: Color
}

interface ColorPickerBrightnessState {
    var brightness: Float
}

interface ColorPickerState : ColorCircleState, ColorPickerBrightnessState

private class ColorPickerStateImpl(
    initialColor: Color,
    initialBrightness: Float
) : ColorPickerState {
    override var color: Color by mutableStateOf(initialColor)
    override var brightness: Float by mutableStateOf(initialBrightness)
}

@Composable
fun rememberColorPickerState(
    color: Color = Transparent,
    brightness: Float = 1f
): ColorPickerState = ColorPickerStateImpl(color,brightness)