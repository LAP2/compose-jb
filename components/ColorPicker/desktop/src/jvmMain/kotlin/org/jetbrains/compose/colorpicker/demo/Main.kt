package org.jetbrains.compose.colorpicker.demo

import androidx.compose.desktop.DesktopTheme
import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush.Companion.horizontalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.colorpicker.ColorPicker
import org.jetbrains.compose.colorpicker.ColorPickerState
import org.jetbrains.compose.colorpicker.rememberColorPickerState

@Composable
private fun GradientLine(
    colors: List<Color>
) = Box(
    Modifier
        .fillMaxWidth()
        .height(30.dp)
        .drawWithCache {
            onDrawBehind {
                drawRect(
                    horizontalGradient(colors,endX = size.width)
                )
            }
        }
)

private class ColorsListState(
    var selectedColor: MutableState<Int>,
    var colors: MutableList<Color>,
    var onClick: (selectedColor: Color) -> Unit
)

@Composable
private fun SelectableItem(
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) = Box(
    Modifier
        .size(30.dp)
        .background(color)
        .selectable(
            selected,
            onClick = onClick
        )
) {
    if (selected) Box(
        Modifier
            .fillMaxSize()
            .background(SolidColor(Blue),alpha = 0.25f)
    )
}

@Composable
private fun ColorsList(
    state: ColorsListState
) = Row(
    Modifier
        .fillMaxWidth()
        .padding(vertical = 5.dp),
    spacedBy(3.dp),
    CenterVertically
) {
    with(state) {
        for(colorIndex in colors.indices) {
            SelectableItem(
                colorIndex == selectedColor.value,
                colors[colorIndex]
            ) {
                selectedColor.value = colorIndex
                onClick(colors[colorIndex])
            }
        }
    }
}

fun main() = Window(
    "Color Picker demo"
) {
    MaterialTheme {
        DesktopTheme {
            val state: ColorPickerState = rememberColorPickerState()
            val colorListState = remember {
                ColorsListState(
                    mutableStateOf(-1),
                    mutableListOf(Red, Green, Blue)
                ) {
                    println("Color selected")
//                    state.color = it
                }
            }
//            if (colorListState.selectedColor.value >= 0 && state.color != colorListState.colors[colorListState.selectedColor.value]) {
//                colorListState.colors[colorListState.selectedColor.value] = state.color
//            }
            Column {
                ColorsList(colorListState)
                GradientLine(colorListState.colors)
                Box(Modifier.fillMaxSize()) {
                    ColorPicker(
                        Modifier.fillMaxSize(),
                        state
                    )
                }
            }
        }
    }
}