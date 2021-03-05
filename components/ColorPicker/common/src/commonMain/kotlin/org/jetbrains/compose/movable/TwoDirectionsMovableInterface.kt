package org.jetbrains.compose.movable

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset

interface TwoDirectionsMoveScope {
    fun moveBy(
        xPixels: Float,
        yPixels: Float
    )
}

interface TwoDirectionsMovable {

    suspend fun move(
        movePriority: MutatePriority = MutatePriority.Default,
        block: suspend TwoDirectionsMoveScope.() -> Unit
    )

    fun dispatchRawMovement(
        xDelta: Float,
        yDelta: Float
    )

    val isMoveInProgress: Boolean

}

fun TwoDirectionsMovable.dispatchRawMovement(offset: Offset) = dispatchRawMovement(offset.x, offset.y)

fun TwoDirectionsMoveScope.moveBy(offset: Offset) = moveBy(offset.x,offset.y)

private typealias TwoDirectionsMoveDeltaConsumer = (xDelta: Float, yDelta: Float) -> Unit

fun movableState(
    consumeMoveDelta: TwoDirectionsMoveDeltaConsumer
): TwoDirectionsMovable {
    return DefaultTwoDirectionsMovableState(consumeMoveDelta)
}

@Composable
fun rememberMovableState(
    consumeMoveDelta: TwoDirectionsMoveDeltaConsumer
): TwoDirectionsMovable {
    return DefaultTwoDirectionsMovableState(consumeMoveDelta)
}

suspend fun TwoDirectionsMovable.stopMovement(
    movePriority: MutatePriority = MutatePriority.Default
) {
    move(movePriority){}
}

private class DefaultTwoDirectionsMovableState(
    val onMoveDelta: TwoDirectionsMoveDeltaConsumer
) : TwoDirectionsMovable {

    private val twoDirectionsMoveScope = object : TwoDirectionsMoveScope {
        override fun moveBy(xPixels: Float, yPixels: Float) = onMoveDelta(xPixels, yPixels)
    }

    private val moveMutex = MutatorMutex()

    private val isMovingState = mutableStateOf(false)

    override suspend fun move(
        movePriority: MutatePriority,
        block: suspend TwoDirectionsMoveScope.() -> Unit
    ) {
        moveMutex.mutateWith(twoDirectionsMoveScope, movePriority) {
            isMovingState.value = true
            block()
            isMovingState.value = false
        }
    }

    override fun dispatchRawMovement(xDelta: Float, yDelta: Float) = onMoveDelta(xDelta, yDelta)

    override val isMoveInProgress: Boolean
        get() = isMovingState.value
}