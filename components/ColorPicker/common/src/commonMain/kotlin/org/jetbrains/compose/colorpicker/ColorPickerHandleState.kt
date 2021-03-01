package org.jetbrains.compose.colorpicker

import androidx.compose.foundation.MutatePriority
import androidx.compose.runtime.mutableStateOf
import org.jetbrains.compose.movable.TwoDirectionsMovable
import org.jetbrains.compose.movable.TwoDirectionsMoveScope
import org.jetbrains.compose.movable.movableState

class ColorPickerHandleState: TwoDirectionsMovable {

    private val _x = mutableStateOf(0f)

    var x: Float
        get() = _x.value
        set(newX) {
            _x.value = newX
        }

    private val _y = mutableStateOf(0f)

    var y: Float
        get() = _y.value
        set(newY) {
            _y.value = newY
        }


    private val twoDirectionsMovableState = movableState(this::onMove)

    private fun onMove(xDelta: Float, yDelta: Float) {
        x+=xDelta
        y+=yDelta
    }

    override suspend fun move(
        movePriority: MutatePriority,
        block: suspend TwoDirectionsMoveScope.() -> Unit
    ) = twoDirectionsMovableState.move(movePriority, block)

    override fun dispatchRawMovement(
        xDelta: Float,
        yDelta: Float
    ) = twoDirectionsMovableState.dispatchRawMovement(xDelta, yDelta)

    override val isMoveInProgress: Boolean = twoDirectionsMovableState.isMoveInProgress

}