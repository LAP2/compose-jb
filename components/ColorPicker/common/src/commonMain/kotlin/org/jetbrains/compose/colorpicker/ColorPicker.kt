package org.jetbrains.compose.colorpicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun ColorCircle(
    modifier: Modifier = Modifier,
    colorPickerState: ColorPickerState
)

@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    colorPickerState: ColorPickerState
) {
    ColorCircle(modifier,colorPickerState)
}