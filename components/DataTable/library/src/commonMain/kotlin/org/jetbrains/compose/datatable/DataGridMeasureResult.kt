package org.jetbrains.compose.datatable

import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

internal class DataGridMeasureResult(
    val firstVisibleCell: LazyMeasuredCell?,
    val firstVisibleCellOffset: IntOffset,
    override val visibleCellsInfo: List<DataGridCellInfo>,
    val layoutSize: IntSize,
    val placementBock: Placeable.PlacementScope.()->Unit
) : DataGridLayoutInfo {

}