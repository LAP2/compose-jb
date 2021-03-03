package org.jetbrains.compose.colorpicker.demo

import androidx.compose.desktop.DesktopTheme
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.jetbrains.compose.colorpicker.ColorPicker
import org.jetbrains.compose.colorpicker.ColorPickerHandleState

fun main() = Window(
    "Color Picker demo"
) {
    MaterialTheme {
        DesktopTheme {
//            val state = remember { ColorPickerHandleState() }
//            ColorPicker(
//                Modifier.fillMaxSize(),
//                state
//            )
        }
    }
}