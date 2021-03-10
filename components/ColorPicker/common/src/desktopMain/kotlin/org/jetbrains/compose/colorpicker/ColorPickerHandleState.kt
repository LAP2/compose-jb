package org.jetbrains.compose.colorpicker

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.MutatePriority
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.movable.TwoDirectionsMovable
import org.jetbrains.compose.movable.TwoDirectionsMoveScope
import org.jetbrains.compose.movable.movableState
import java.awt.Color.HSBtoRGB
import java.awt.Color.RGBtoHSB
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.roundToInt

internal interface HandlePosition {
    val offset: Offset
}

internal interface MovableHandlerState : HandlePosition, TwoDirectionsMovable

internal class ColorToOffsetBiDirectionalConverter(
    private val colorCircleRadius: Float,
    private val colorCircleCenter: Offset,
    private val brightness: Float
) {
    @Suppress("NOTHING_TO_INLINE")
    inline fun Offset.toColor(): Color {
        return Color(
            HSBtoRGB(
                Math.toDegrees(acos(x).toDouble()).toFloat(),
                with(this.pow(2)) { sqrt(x + y) / colorCircleRadius},
                brightness
            )
        )
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun Color.toOffset(): Offset {
        val hsb = RGBtoHSB(
            red.roundToInt(),
            green.roundToInt(),
            blue.roundToInt(),null
        )
        val angle = hsb[0]
        val radius = hsb[1]
        return Offset(
            (radius * cos(Math.toRadians(angle.toDouble()))).toFloat(),
            (radius * sin(Math.toRadians(angle.toDouble()))).toFloat()
        ) + colorCircleCenter
    }
}

internal class ColorPickerHandleState : HandlePosition {

    val animatableOffset = Animatable(Offset.Zero, Offset.VectorConverter)

    override val offset: Offset
        get() = animatableOffset.value

    //    private val twoDirectionsMovableState = movableState(this::onMove)

//    private fun onMove(xDelta: Float, yDelta: Float): Offset {
//        offset += Offset(xDelta, yDelta)
//        return offset
//    }

//    override suspend fun move(
//        movePriority: MutatePriority,
//        block: suspend TwoDirectionsMoveScope.() -> Unit
//    ) = twoDirectionsMovableState.move(movePriority, block)
//
//    override fun dispatchRawMovement(
//        xDelta: Float,
//        yDelta: Float
//    ) = twoDirectionsMovableState.dispatchRawMovement(xDelta, yDelta)
//
//    override val isMoveInProgress: Boolean = twoDirectionsMovableState.isMoveInProgress

}