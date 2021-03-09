package org.jetbrains.compose.movable

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset

suspend fun TwoDirectionsMovable.smoothMoveTo(
    from: Offset,
    to: Offset,
    animationSpec: AnimationSpec<Offset> = tween()
): Offset {
    val animSpec = animationSpec.vectorize(Offset.VectorConverter)
    val conv = Offset.VectorConverter
    val fromVector = conv.convertToVector(from)
    val toVector = conv.convertToVector(to)
    var previousValue = Offset.Zero

    move {
        val startTimeNanos = withFrameNanos { it }
        do {
            val finished = withFrameNanos { frameTimeNanos ->
                val newValue = conv.convertFromVector(
                    animSpec.getValueFromNanos(
                        playTimeNanos = frameTimeNanos - startTimeNanos,
                        initialValue = fromVector,
                        targetValue = toVector,
                        // TODO: figure out if/how we should incorporate existing velocity
                        initialVelocity = fromVector
                    )
                )
                val delta = newValue - previousValue
                val consumed = moveBy(delta)

                if (consumed != delta) {
                    previousValue += consumed
                    true
                } else {
                    previousValue = newValue
                    previousValue == to
                }
            }
        } while (!finished)
    }
    return previousValue
}