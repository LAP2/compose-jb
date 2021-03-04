package org.jetbrains.compose.colorpicker

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
    var offset: Offset
}

internal class ColorPickerHandleState(
    private val setColor: (Color) -> Unit,
    private val setHandlePosition: (Offset)-> Unit,
    private val colorCircleRadius: Float,
    private val colorCircleCenter: Offset,
    var brightness: Float
) : HandlePosition, TwoDirectionsMovable {

    override var offset: Offset by mutableStateOf(Offset.Unspecified)

    private val twoDirectionsMovableState = movableState(this::onMove)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Offset.calculateColor(): Color {
        return Color(
            HSBtoRGB(
                Math.toDegrees(acos(x).toDouble()).toFloat(),
                with(this.pow(2)) { sqrt(x + y) / colorCircleRadius},
                brightness
            )
        )
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Color.calculateOffset(): Offset {
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

    fun toColor(
        handlePosition: Offset,
    ) {
        setColor(handlePosition.calculateColor())
    }

    fun toHandlePosition(
        color: Color,
    ) {
        setHandlePosition(color.calculateOffset())
    }

    private fun onMove(xDelta: Float, yDelta: Float) {
        offset += Offset(xDelta, yDelta)
        toColor(offset)
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