package org.jetbrains.compose.state

import org.jetbrains.compose.datatable.model.Column
import org.jetbrains.compose.datatable.model.Row
import kotlin.jvm.JvmInline

sealed interface SizeValue

@JvmInline
value class Measured(
    val size: Int
): SizeValue

@JvmInline
value class NeedMeasure(
    val setter: (value: Int) -> Unit
): SizeValue

//====================================================================================================================//

interface RowHeight {
    operator fun get(index: Row): SizeValue
}

@JvmInline
internal value class FixedRowHeight(
    private val height: Measured
) : RowHeight {
    override fun get(index: Row): SizeValue = height
}

@JvmInline
internal value class MapBasedRowHeight(
    private val heights: MutableMap<Row, Int>
) : RowHeight {
    override fun get(index: Row): SizeValue = heights[index]?.let {
            height -> Measured(height)
    } ?: NeedMeasure { height ->
        heights[index] = height
    }
}

interface ColumnWidth {
    operator fun get(index: Column): SizeValue
}

@JvmInline
internal value class MapBasedColumnWidth(
    private val widths: MutableMap<Column, Int>
) : ColumnWidth {
    override fun get(index: Column): SizeValue = widths[index]?.let {
            width -> Measured(width)
    } ?: NeedMeasure { width ->
        widths[index] = width
    }
}

internal class IndexWrapperColumnWidth(
    private val columnIndex: MutableMap<Column, Column>,
    private val delegate: ColumnWidth
) : ColumnWidth {
    override fun get(index: Column): SizeValue = delegate[columnIndex[index] ?: index]
}
