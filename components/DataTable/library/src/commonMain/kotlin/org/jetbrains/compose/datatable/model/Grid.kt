@file:Suppress("NOTHING_TO_INLINE")

package org.jetbrains.compose.datatable.model

import androidx.compose.ui.util.packInts
import androidx.compose.ui.util.unpackInt1
import androidx.compose.ui.util.unpackInt2
import kotlin.jvm.JvmInline

fun GridSize(
    rowsCount: Int,
    columnsCount: Int
): GridSize {
    require(rowsCount >= 0) {"could not set negative rows count value current value: $rowsCount"}
    require(columnsCount >= 0) {"could not set negative columns count value current value: $columnsCount"}
    return GridSize(packInts(rowsCount, columnsCount))
}

/**
 * Represents size of data grid, contains packed value of rows count and columns count
 * * First component is [rowsCount]
 * * Second component is [columnsCount]
 */
@JvmInline
value class GridSize internal constructor(internal val packedValue: Long) {

    val rowsCount: Int
        get() = unpackInt1(packedValue)

    val columnsCount: Int
        get() = unpackInt2(packedValue)

    inline operator fun component1(): Int = rowsCount
    inline operator fun component2(): Int = columnsCount

    companion object {
        val Empty = GridSize(0L)
    }
}

fun CellPosition(
    row: Row,
    column: Column
): CellPosition {
    require(row.rowIndex >= 0) {"could not set negative row value current value: ${row.rowIndex}"}
    require(column.columnIndex >= 0) {"could not set negative column value current value: ${column.columnIndex}"}
    return CellPosition(packInts(row.rowIndex, column.columnIndex))
}


/**
 * Represent data grid cell position, contains packed value of Row and Column
 * * First component is [Row]
 * * Second component is [Column]
 */
@JvmInline
value class CellPosition internal constructor(internal val packedValue: Long) {
    val row: Row
        get() = Row(unpackInt1(packedValue))
    val column: Column
        get() = Column(unpackInt2(packedValue))

    inline operator fun component1(): Row = row
    inline operator fun component2(): Column = column

    companion object {
        val ZeroCellPosition = CellPosition(0L)
    }

}

/**
 * Represent data grid row index
 * This class is value based
 * @param rowIndex zero based index of data grid row
 */
@JvmInline
value class Row(val rowIndex: Int) {
    operator fun inc(): Row = Row(rowIndex + 1)
    operator fun dec(): Row = Row(rowIndex - 1)
    operator fun compareTo(other: Int): Int = rowIndex - other
    operator fun minus(value: Int): Row = Row(rowIndex - value)
    operator fun plus(value: Int): Row = Row(rowIndex + value)
}

/**
 * Represent data grid column index
 * This class is value based
 * @param columnIndex zero based index of data grid column
 */
@JvmInline
value class Column(val columnIndex: Int) {
    operator fun inc(): Column = Column(columnIndex + 1)
    operator fun dec(): Column = Column(columnIndex - 1)
    operator fun compareTo(other: Int): Int = columnIndex - other
    operator fun minus(value: Int): Column = Column(columnIndex - value)
    operator fun plus(value: Int): Column = Column(columnIndex + value)
}