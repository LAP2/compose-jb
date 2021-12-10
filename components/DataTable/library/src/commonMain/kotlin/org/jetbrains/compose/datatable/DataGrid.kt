package org.jetbrains.compose.datatable

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.jetbrains.compose.state.DataGridState
import org.jetbrains.compose.state.MapBasedColumnWidth
import org.jetbrains.compose.state.MapBasedRowHeight

@Composable
fun DataGridTest(
    cellsState: State<DataGridCellProvider>,
    modifier: Modifier,
    state: DataGridState,
    contentPadding: PaddingValues,
    reverseLayout: Boolean,
    flingBehavior: FlingBehavior
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val cellContentFactory = rememberCellContentFactory(cellsState, state)

    val subcomposeLayoutState = remember { SubcomposeLayoutState(2) }

    val rhs = remember { MapBasedRowHeight(mutableMapOf()) }
    val cws = remember { MapBasedColumnWidth(mutableMapOf()) }

    SubcomposeLayout(
        subcomposeLayoutState,
        modifier
            .scrollable(
                orientation = Vertical,
                reverseDirection = !reverseLayout,
                interactionSource = state.internalInteractionSource,
                flingBehavior = flingBehavior,
                state = state.verticalScrollableState
            )
            .scrollable(
                orientation = Horizontal,
                reverseDirection = if (isRtl) reverseLayout else !reverseLayout,
                interactionSource = state.internalInteractionSource,
                flingBehavior = flingBehavior,
                state = state.horizontalScrollableState
            )
    ) { constraints ->

        val cells = cellsState.value
        state.updateScrollsPositionIfTheFirstCellsWasMoved(cells)

        state.density = Density(density, fontScale)

        cellContentFactory.updateCellScope(this, constraints)

        // TODO spacing between grid cells

        val lazyMeasuredCells = LazyMeasuredCellProvider(
            constraints = constraints,
            columnWidth = cws,
            rowHeight = rhs,
            scope = this,
            cells = cells,
            cellContentFactory = cellContentFactory,
        ) { position, key, placeable ->
            LazyMeasuredCell(
                position,
                key,
                placeable
            )
        }

        val measuredGrid = measureDataGrid(
            gridSize = cells.gridSize,
            constraints = constraints,
            cells = lazyMeasuredCells,
            contentPadding = DataGridPaddings(
                top = contentPadding.calculateTopPadding().roundToPx(),
                right = contentPadding.calculateRightPadding(layoutDirection).roundToPx(),
                bottom = contentPadding.calculateBottomPadding().roundToPx(),
                left = contentPadding.calculateLeftPadding(layoutDirection).roundToPx()
            ),
            firstVisibleCell = CurrentFirstCell(
                position = state.firstVisibleCellPositionNonObservable,
                offset = state.firstVisibleCellScrollOffsetNonObservable
            ),
            scrollToBeConsumed = state.scrollToBeConsumed
        )

        state.applyMeasuredGrid(measuredGrid)

        state.onPostMeasureListener?.apply {
            onPostMeasure(lazyMeasuredCells.childConstraints, measuredGrid)
        }

        layout(
            width = measuredGrid.layoutSize.width,
            height = measuredGrid.layoutSize.height,
            placementBlock = measuredGrid.placementBock
        )
    }
}