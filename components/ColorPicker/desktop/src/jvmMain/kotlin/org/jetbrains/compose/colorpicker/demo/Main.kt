package org.jetbrains.compose.colorpicker.demo

import androidx.compose.desktop.DesktopTheme
import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush.Companion.horizontalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
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
    var selectedColorIndex: MutableState<Int>,
    var colors: MutableList<Color>,
    var onClick: (selectedColor: Color) -> Unit
) {
    val selectedColor: Color?
        get() = if (selectedColorIndex.value >= 0) colors[selectedColorIndex.value] else null
}

@Composable
private fun ColorBox(
    color: Color
) = Box(
    Modifier
        .size(30.dp)
        .background(color)
)

@Composable
private fun SelectionWrapper(
    slot: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .border(
                3.dp,
                MaterialTheme.colors.background
            ),
        content = {slot()}
    )
}

@Composable
private fun SelectableListItem(
    onSelection: () -> Unit,
    selected: Boolean,
    itemContent: @Composable () -> Unit
) {
    Box(
        Modifier
            .selectable(
                selected,
            ) {
                onSelection()
            }
    ) {
        if (selected) {
            SelectionWrapper(itemContent)
        } else {
            itemContent()
        }
    }
}

@Composable
private fun ColorsList(
    state: ColorsListState
) {
    val listState = rememberLazyListState()
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        state = listState,
        horizontalArrangement = spacedBy(3.dp),
        verticalAlignment = CenterVertically
    ) {
        itemsIndexed(state.colors) { index, color ->
            SelectableListItem(
                {
                    state.onClick(color)
                    state.selectedColorIndex.value = index
                },
                state.selectedColorIndex.value == index
            ) {
                ColorBox(color)
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
                    state.color = it
                }
            }
            if (
                colorListState.selectedColor != null
                && colorListState.selectedColor != state.color
            ) {
                colorListState.colors[colorListState.selectedColorIndex.value] = state.color
            }
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