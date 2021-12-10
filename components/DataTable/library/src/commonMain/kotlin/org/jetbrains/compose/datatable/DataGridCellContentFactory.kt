package org.jetbrains.compose.datatable

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import org.jetbrains.compose.datatable.model.CellPosition
import org.jetbrains.compose.state.DataGridState

typealias CellContent = @Composable () -> Unit

@Composable
internal fun rememberCellContentFactory(
    cellsState: State<DataGridCellProvider>,
    state: DataGridState
): DataGridCellContentFactory {
    val holder = rememberSaveableStateHolder()
    val factory = remember(cellsState) {
        DataGridCellContentFactory(
            holder,
            cellsState
        )
    }
    factory.updateKeyPositionMappingForVisibleCells(state) // TODO maybe should be in other place seems to be a side effect
    return factory
}

class DataGridCellContentFactory(
    private val holder: SaveableStateHolder,
    private var cellsState: State<DataGridCellProvider>
) {
    private val cellLambdaCache = mutableMapOf<Any,CachedCellContent>()

    fun getCellContent(
        position: CellPosition,
        key: Any
    ): CellContent {
        val cachedContent = cellLambdaCache[key]
        return if (cachedContent != null && cachedContent.position == position) {
            cachedContent.content
        } else {
            val newContent = CachedCellContent(position, cellScope, key)
            cellLambdaCache[key] = newContent
            newContent.content
        }
    }

    fun updateKeyPositionMappingForVisibleCells(
        state: DataGridState
    ) {
        val cells = cellsState.value
        if (cells.isNotEmpty()) {
            state.updateScrollsPositionIfTheFirstCellsWasMoved(cells)
            val firstVisible = state.firstVisibleCellPositionNonObservable
            val count = state.visibleCellsCount
        }
    }

    private inner class CachedCellContent(
        initialPosition: CellPosition,
        private val scope: DataGridCellScope,
        val key: Any
    ) {
        var position by mutableStateOf(initialPosition)

        val content: CellContent = @Composable {
            val cells = cellsState.value
            if (position in cells) {
                val key = cells.getKey(position)
                if (key == this.key) {
                    val content = cells.getContent(position,scope)
                    holder.SaveableStateProvider(key, content)
                }
            }
        }

    }

    private var cellScope = InitialCellScopeImpl

    fun updateCellScope(
        density: Density,
        constraints: Constraints
    ) {
        if (cellScope.density != density || cellScope.constraints != constraints) {
            cellScope = DataGridCellScopeImpl(density, constraints)
            cellLambdaCache.clear()
        }
    }

}

private val InitialCellScopeImpl = DataGridCellScopeImpl(Density(0f, 0f), Constraints())

private data class DataGridCellScopeImpl(
    val density: Density,
    val constraints: Constraints
) : DataGridCellScope {

}