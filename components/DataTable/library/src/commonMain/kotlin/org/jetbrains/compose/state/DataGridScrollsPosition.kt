@file:Suppress("NOTHING_TO_INLINE")
package org.jetbrains.compose.state

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.util.packInts
import androidx.compose.ui.util.unpackInt1
import androidx.compose.ui.util.unpackInt2
import org.jetbrains.compose.datatable.DataGridCellProvider
import org.jetbrains.compose.datatable.contains
import org.jetbrains.compose.datatable.model.CellPosition
import org.jetbrains.compose.datatable.model.Column
import org.jetbrains.compose.datatable.model.Row
import kotlin.jvm.JvmInline

internal fun ScrollOffset(
    horizontalOffset: Int,
    verticalOffset: Int
): ScrollOffset {
    return ScrollOffset(packInts(horizontalOffset,verticalOffset))
}

/**
 * first int horizontal offset
 * second int vertical offset
 */
@JvmInline
internal value class ScrollOffset internal constructor(
    internal val packedValue: Long
) {
    val horizontalOffset: Int
        get() = unpackInt1(packedValue)

    val verticalOffset: Int
        get() = unpackInt2(packedValue)

    inline operator fun component1(): Int = horizontalOffset
    inline operator fun component2(): Int = verticalOffset

    companion object {
        val StartOffset = ScrollOffset(0L)
    }
}

internal class DataGridScrollsPosition(
    initialRow: Int = 0,
    initialColumn: Int = 0,
    initialHorizontalScrollOffset: Int = 0,
    initialVerticalScrollOffset: Int = 0
) {

    var position: CellPosition = CellPosition(Row(initialRow), Column(initialColumn))
        private set

    var scrollOffset: ScrollOffset = ScrollOffset(
        initialHorizontalScrollOffset,
        initialVerticalScrollOffset
    )
        private set

    private val positionState = mutableStateOf(position)
    val observablePosition get() = positionState.value

    private val horizontalScrollOffsetState = mutableStateOf(scrollOffset.horizontalOffset)
    val observableHorizontalScrollOffset get() = horizontalScrollOffsetState.value

    private val verticalScrollOffsetState = mutableStateOf(scrollOffset.verticalOffset)
    val observableVerticalScrollOffset get() = verticalScrollOffsetState.value

    private var lastKnownCellKey: Any? = null

    private fun CellPosition.requireNonNegativeCellPosition() = require(
        row.rowIndex >= 0f && column.columnIndex >= 0f
    ) {
        "Position should not contains negative numbers (Row:$row;Column:$column)"
    }

    private fun ScrollOffset.requireNonNegativeScrollOffset() = require(
        horizontalOffset >= 0f && verticalOffset >= 0f
    ) {
        "Scroll offsets should not be negative $this"
    }

    fun updateScrollsPositionIfTheFirstCellsWasMoved(cells: DataGridCellProvider) = update(
        findDataGridCellByKey(lastKnownCellKey, position, cells),
        scrollOffset
    )

    private fun update(
        position: CellPosition,
        scrollOffset: ScrollOffset
    ) {
        position.requireNonNegativeCellPosition()
        scrollOffset.requireNonNegativeScrollOffset()
        tryUpdatePosition(position)
        tryUpdateScrollOffset(scrollOffset)
    }

    private fun tryUpdatePosition(newPosition: CellPosition) {
        if (newPosition != position) {
            position = newPosition
            positionState.value = newPosition
        }
    }

    private fun tryUpdateScrollOffset(newOffset: ScrollOffset) {
        if (newOffset != scrollOffset) {
            scrollOffset = newOffset
            horizontalScrollOffsetState.value = newOffset.horizontalOffset
            verticalScrollOffsetState.value = newOffset.verticalOffset
        }
    }

    private companion object {

        private fun findDataGridCellByKey(
            key: Any?,
            lastKnown: CellPosition,
            cells: DataGridCellProvider
        ): CellPosition {
            if (key != null && lastKnown in cells && key != cells.getKey(lastKnown)) {
                with(cells) {
                    var before = prev(lastKnown)
                    var after = next(lastKnown)
                    while (before.row >= 0 || after in cells) {
                        if (before.row >= 0) {
                            if (key == cells.getKey(before)) return before
                            before = prev(before)
                        }
                        if (after in cells) {
                            if (key == cells.getKey(after)) return after
                            after = next(after)
                        }
                    }
                }
            }
            return lastKnown
        }

        private fun DataGridCellProvider.next(
            current: CellPosition
        ): CellPosition = with(current) {
            if (column.columnIndex == 0) {
                CellPosition(row-1, Column(gridSize.columnsCount-1))
            } else {
                CellPosition(row = row, column = column-1)
            }
        }

        private fun DataGridCellProvider.prev(
            current: CellPosition
        ): CellPosition = with(current) {
            if (column.columnIndex == gridSize.columnsCount-1) {
                CellPosition(row + 1, Column(0))
            } else {
                CellPosition(row = row, column = column+1)
            }
        }
    }
}