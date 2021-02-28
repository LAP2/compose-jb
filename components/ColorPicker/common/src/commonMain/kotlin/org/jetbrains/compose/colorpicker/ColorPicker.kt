package org.jetbrains.compose.colorpicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun ColorPicker(
    modifier: Modifier,
    handleState: ColorPickerHandleState
)