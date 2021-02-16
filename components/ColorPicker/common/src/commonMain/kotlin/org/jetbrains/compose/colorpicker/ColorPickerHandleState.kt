package org.jetbrains.compose.colorpicker

import androidx.compose.foundation.MutatePriority
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