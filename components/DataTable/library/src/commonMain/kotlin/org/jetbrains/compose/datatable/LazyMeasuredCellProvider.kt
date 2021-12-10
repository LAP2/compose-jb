package org.jetbrains.compose.datatable

import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Constraints.Companion.Infinity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntOffset.Companion.Zero
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastForEach
import org.jetbrains.compose.datatable.model.CellPosition
import org.jetbrains.compose.datatable.model.Column
import org.jetbrains.compose.datatable.model.Row
import org.jetbrains.compose.state.ColumnWidth
import org.jetbrains.compose.state.Measured
import org.jetbrains.compose.state.NeedMeasure
import org.jetbrains.compose.state.RowHeight

internal class LazyMeasuredCell(
    override val position: CellPosition,
    override val key: Any,
    private val placeable: Placeable
) : DataGridCellInfo {

    override val size: IntSize = IntSize(placeable.width, placeable.height)

    override var cellOffset: IntOffset = Zero

    fun place(
        scope: Placeable.PlacementScope
    ): Unit = with(scope) {
        placeable.placeRelativeWithLayer(cellOffset)
    }
}

internal data class LazyMeasuredRow(
    val rowIndex: Row,
    val row: List<LazyMeasuredCell>,
    val rowHeight: Int
) {
    fun place(
        scope: Placeable.PlacementScope
    ): Unit = row.fastForEach { it.place(scope) }
}
/*
* TODO at this moment this class is unused but there could be an opportunity to use
*  it during scroll offset apply by just change internal column offsets
* */
internal data class LazyMeasuredColumn(
    val columnIndex: Column,
    val column: List<LazyMeasuredCell>,
    val columnWidth: Int
) {
    fun place(
        scope: Placeable.PlacementScope
    ): Unit = column.fastForEach { it.place(scope) }
}

internal fun interface MeasuredCellFactory {
    fun createPlaceable(
        position: CellPosition,
        key: Any,
        placeable: Placeable
    ) : LazyMeasuredCell
}

private const val CELL_CONTENT_ROOT_COUNT = 1

internal class LazyMeasuredCellProvider(
    constraints: Constraints,
    private val columnWidth: ColumnWidth,
    private val rowHeight: RowHeight,
    private val scope: SubcomposeMeasureScope,
    private val cells: DataGridCellProvider,
    private val cellContentFactory: DataGridCellContentFactory,
    private val measuredCellFactory: MeasuredCellFactory
) {

    val viewportConstraints = constraints.copy()

    val childConstraints = Constraints()

    private val CellPosition.height: Int
        get() =
            when (val height = rowHeight[row]) {
                is Measured -> height.size
                is NeedMeasure -> {
                    // TODO setup callback
                    Infinity
                }
            }

    private val CellPosition.width: Int
        get() =
            when(val width = columnWidth[column]) {
                is Measured -> width.size
                is NeedMeasure -> {
                    // TODO setup callback
                    Infinity
                }
            }

    private fun cellConstraints(position: CellPosition): Constraints =
        with(position) {
            val width = width
            val height = height

            Constraints().let {
                if (width < Infinity) {
                    it.copy(minWidth = width, maxWidth = width)
                } else {
                    it
                }
            }.let {
                if (height < Infinity) {
                    it.copy(minHeight = height, maxHeight = height)
                } else it
            }
        }


    fun getAndMeasure(
        position: CellPosition,
    ): LazyMeasuredCell {
        val key = cells.getKey(position)
        val content = cellContentFactory.getCellContent(position, key)
        val measurables = scope.subcompose(key,content)
        require(CELL_CONTENT_ROOT_COUNT >= measurables.size) {"Cell content should have one cell root"}
        return measuredCellFactory
            .createPlaceable(
                position,
                key,
                measurables[CELL_CONTENT_ROOT_COUNT - 1]
                    .measure(cellConstraints(position))
            )
    }

    operator fun get(position: CellPosition): LazyMeasuredCell = getAndMeasure(position)

    operator fun get(row: Row, column: Column): LazyMeasuredCell = get(CellPosition(row, column))
}
