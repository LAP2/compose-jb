package org.jetbrains.compose.datatable

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.jetbrains.compose.datatable.model.CellPosition

interface DataGridCellInfo {
    val position: CellPosition
    val cellOffset: IntOffset
    val size: IntSize
    val key: Any
}