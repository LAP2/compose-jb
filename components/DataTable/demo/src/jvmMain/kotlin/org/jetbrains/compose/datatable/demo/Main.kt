package org.jetbrains.compose.datatable.demo

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.datatable.*
import org.jetbrains.compose.datatable.model.CellPosition
import org.jetbrains.compose.datatable.model.GridSize
import org.jetbrains.compose.state.DataGridState
import org.jetbrains.compose.state.StateTestFun

fun main() = application {
    val state = remember { DataGridState() }
    val dcp = derivedStateOf { TestCellsProvider() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Data Table demo"
    ) {
        MaterialTheme {
//            DesktopTheme {
//                DataGridTest(
//                    cellsState = dcp,
//                    modifier = Modifier
//                        .fillMaxSize(),
//                    state = state,
//                    contentPadding = PaddingValues(),
//                    reverseLayout = false,
//                    flingBehavior = ScrollableDefaults.flingBehavior()
//                )
//            }
            DataGridTest(
                dcp,
                Modifier,
                state,
                PaddingValues(),
                false,
                ScrollableDefaults.flingBehavior()
            )
//            StateTestFun()
        }
    }
}

private data class DefaultLazyKey(private val cellPosition: CellPosition)

private class GroupContent(
    val key: ((position: CellPosition) -> Any)?,
    val content: Nothing
)

private class TestCellsProvider(
    override val gridSize: GridSize = GridSize(1000,1000)
) : DataGridCellProvider {


    override fun getKey(
        position: CellPosition
    ): Any {
        return DefaultLazyKey(position)
    }

    override fun getContent(
        position: CellPosition,
        scope: DataGridCellScope
    ): CellContent {
        return {     Box(
            modifier = Modifier
                .size(
                    width = 200.dp,
                    height = 50.dp
                )
                .background(Color.Red)
        ) }
    }
}

@Preview
@Composable
fun CC() {
    Box(
        modifier = Modifier
            .size(
                width = 200.dp,
                height = 50.dp
            )
            .background(Color.Red)
    )
}