package org.jetbrains.compose.datatable

import org.jetbrains.compose.datatable.model.CellPosition
import org.jetbrains.compose.datatable.model.GridSize

interface DataGridCellProvider {

    val gridSize: GridSize

    fun getKey(position: CellPosition): Any

    fun getContent(
        position: CellPosition,
        scope: DataGridCellScope
    ): CellContent
}

internal operator fun DataGridCellProvider.contains(cell: CellPosition): Boolean = with(cell) {
    row >= 0 && column >= 0 &&
            row < gridSize.rowsCount && column < gridSize.columnsCount
}

internal fun DataGridCellProvider.isEmpty(): Boolean = gridSize.rowsCount > 0 && gridSize.columnsCount > 0
internal fun DataGridCellProvider.isNotEmpty(): Boolean = !isEmpty()