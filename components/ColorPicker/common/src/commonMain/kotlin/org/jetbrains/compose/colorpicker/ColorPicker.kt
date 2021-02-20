package org.jetbrains.compose.colorpicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun ColorCircle(
    modifier: Modifier = Modifier
)

@Composable
fun ColorPicker(modifier: Modifier) {
    ColorCircle(modifier)
}