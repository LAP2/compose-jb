package org.jetbrains.compose.colorpicker

import androidx.compose.foundation.MutatePriority
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import org.jetbrains.compose.movable.TwoDirectionsMovable
import org.jetbrains.compose.movable.TwoDirectionsMoveScope
import org.jetbrains.compose.movable.movableState

interface MovementDeltaFilter {

}

class ColorPickerHandleState : TwoDirectionsMovable {

    private val twoDirectionsMovableState = movableState(this::onMove)

    private fun onMove(
        xDelta: Float,
        yDelta: Float
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun move(
        movePriority: MutatePriority,
        block: suspend TwoDirectionsMoveScope.() -> Unit) = twoDirectionsMovableState.move(movePriority, block)

    override fun dispatchRawMovement(
        xDelta: Float,
        yDelta: Float
    ) = twoDirectionsMovableState.dispatchRawMovement(xDelta, yDelta)

    override val isMoveInProgress: Boolean = twoDirectionsMovableState.isMoveInProgress

}

fun Modifier.layoutHandle(
    state: ColorPickerHandleState
) = layout { measurable, constraints ->



    layout(constraints.maxWidth, constraints.maxHeight) {

    }


}