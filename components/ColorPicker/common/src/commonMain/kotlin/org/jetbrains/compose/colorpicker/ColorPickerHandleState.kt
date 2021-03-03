package org.jetbrains.compose.colorpicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import org.jetbrains.compose.movable.TwoDirectionsMovable

interface ColorPickable {
    var color: Color
}

interface ColorPickerState : ColorPickable {
    var brightness: Float
}

@Composable
expect fun rememberColorPickerState(
    color: Color = Transparent,
    brightness: Float = 1f
): ColorPickerState