package org.jetbrains.compose.datatable

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.IntOffset.Companion.Zero
import androidx.compose.ui.util.fastForEach
import org.jetbrains.compose.datatable.model.CellPosition
import org.jetbrains.compose.datatable.model.Column
import org.jetbrains.compose.datatable.model.GridSize
import org.jetbrains.compose.datatable.model.GridSize.Companion.Empty
import org.jetbrains.compose.datatable.model.Row
import org.jetbrains.compose.state.ScrollOffset

internal operator fun ScrollOffset.minus(offset: IntOffset): ScrollOffset = ScrollOffset(
    horizontalOffset - offset.x,
    verticalOffset - offset.y
)

internal operator fun IntOffset.plus(offset: ScrollOffset): IntOffset = IntOffset(
    x + offset.horizontalOffset,
    y + offset.verticalOffset
)

internal data class DataGridPaddings(
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0,
    val left: Int = 0
)

internal inline fun DataGridPaddings.requireNonNegativePadding() = require(
    top >= 0 && left >= 0 && bottom >= 0 && right >= 0
) {
    "padding values should be above zero $this"
}

private fun GridSize.checkDataSourceChanges(
    firstVisibleCell: CurrentFirstCell,
): CurrentFirstCell {
    val (row, column) = firstVisibleCell.position
    val (horizontalOffset, verticalOffset) = firstVisibleCell.offset
    val shouldRowChange = row >= rowsCount
    val shouldColumnChange = column >= columnsCount
    return CurrentFirstCell(
        position = CellPosition(
            row = if (shouldRowChange) Row(rowsCount - 1) else row,
            column = if (shouldColumnChange) Column(columnsCount - 1) else column
        ),
        offset = ScrollOffset(
            horizontalOffset = if (shouldColumnChange) 0 else horizontalOffset,
            verticalOffset = if (shouldRowChange) 0 else verticalOffset
        )
    )
}

private fun CurrentFirstCell.applyScroll(
    scrollToBeConsumed: Offset
): Pair<CurrentFirstCell, IntOffset> {
    val scrollDelta = scrollToBeConsumed.round()
    var (dx, dy) = scrollDelta
    var (appliedH, appliedV) = (offset - scrollDelta)
    when {
        position.row.rowIndex == 0 && appliedV < 0 -> {
            dy += appliedV
            appliedV = 0
        }
        position.column.columnIndex == 0 && appliedH < 0 -> {
            dx += appliedH
            appliedH = 0
        }
    }
    return copy(
        offset = ScrollOffset(
            horizontalOffset = appliedH,
            verticalOffset = appliedV
        )
    ) to IntOffset(dx,dy)
}

internal data class CurrentFirstCell(
    val position: CellPosition,
    val offset: ScrollOffset
)

internal fun measureDataGrid(
    gridSize: GridSize,
    constraints: Constraints,
    cells: LazyMeasuredCellProvider,
    contentPadding: DataGridPaddings,
    firstVisibleCell: CurrentFirstCell,
    scrollToBeConsumed: Offset
): DataGridMeasureResult {

    contentPadding.requireNonNegativePadding()

    if (Empty == gridSize) {
        return DataGridMeasureResult(
            firstVisibleCell = null,
            firstVisibleCellOffset = Zero,
            visibleCellsInfo = emptyList(),
            layoutSize = IntSize(constraints.minWidth,constraints.minHeight),
            placementBock = {}
        )
    }

    val (currentFirstCell, appliedScroll) = gridSize
        .checkDataSourceChanges(firstVisibleCell)
        .applyScroll(scrollToBeConsumed)

    var (scrollXDelta, scrollYDelta) = appliedScroll

    // represents the real amount of scroll we applied as a result of this measure pass.

    // applying the whole requested scroll offset. we will figure out if we can't consume
    // all of it later
    var (row, column) = currentFirstCell.position
    val (rowsCount, columnsCount) = gridSize

    val viewportWidth = constraints.maxWidth
    val initialViewportWidth = -currentFirstCell.offset.horizontalOffset
    var viewportWidthUsed = initialViewportWidth

    val viewportHeight = constraints.maxHeight
    var viewportHeightUsed = -currentFirstCell.offset.verticalOffset

    // initialize visible cells list
    val visibleRows = mutableListOf<LazyMeasuredRow>()

    // TODO composing backward here or we possibly should check borders better

    // Composing cells forward
    /* TODO There should be more complex measurement logic
    *   unfortunately we should provide concept of row and column during data table
    *   cell measurements cause sizes of next or previous values could be dependent
    *   on other cells measured before or after and there should be remeasures for them
    * */
    while (viewportHeightUsed <= viewportHeight && row.rowIndex < rowsCount) {
        /* TODO check minimum offset from both sides
        *   This mean if composable at all
        * */
        val measuredCells = mutableListOf<LazyMeasuredCell>()
        /// start of columns measuring
        while (viewportWidthUsed <= viewportWidth && column.columnIndex < columnsCount) {
            with (cells[row,column]) {
                viewportWidthUsed += size.width
                measuredCells += this
                column++
            }
        }
        viewportWidthUsed = initialViewportWidth
        val rowHeight = measuredCells.first().size.height
        viewportHeightUsed += rowHeight
        visibleRows += LazyMeasuredRow(row, measuredCells, rowHeight)
        row++
    }

    // TODO check if we compose full viewport and try to compose back

    // TODO report amount of pixels consumed during scroll

    // TODO calculate scroll position

    // Calculate cells offset
    val visibleCells = calculateCellsOffset(
        cells = visibleRows,
    )

    // TODO headers and other special items

    val newFirstVisibleCell = visibleCells.firstOrNull()

    return DataGridMeasureResult(
        firstVisibleCell = newFirstVisibleCell,
        firstVisibleCellOffset = newFirstVisibleCell?.cellOffset?:Zero,
        visibleCellsInfo = visibleCells,
        layoutSize = IntSize(constraints.maxWidth,constraints.maxHeight),
        placementBock = {
            visibleCells.fastForEach {
                it.place(this)
            }
        }
    )
}

private fun calculateCellsOffset(
    cells: List<LazyMeasuredRow>,
): List<LazyMeasuredCell> {
    var usedHeight = 0
    val result = mutableListOf<LazyMeasuredCell>()
    cells.fastForEach { measuredRow ->
        var usedWidth = 0
        measuredRow.row.fastForEach {
            it.cellOffset = IntOffset(x = usedWidth, y = usedHeight)
            usedWidth += it.size.width + 2 // TODO add width with spacings
            result += it
        }
        usedHeight += measuredRow.rowHeight + 2
    }
    return result
}