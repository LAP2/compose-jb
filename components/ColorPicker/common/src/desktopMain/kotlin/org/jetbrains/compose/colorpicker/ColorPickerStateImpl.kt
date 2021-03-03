package org.jetbrains.compose.colorpicker

import androidx.compose.foundation.MutatePriority
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.movable.TwoDirectionsMovable
import org.jetbrains.compose.movable.TwoDirectionsMoveScope
import org.jetbrains.compose.movable.movableState
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import java.awt.Color.HSBtoRGB

private data class ColorPickerHandleBorderConfiguration(
    val colorCircleCenter: Offset,
    val colorCircleRadius: Float
)

internal interface HandlePosition {
    var offset: Offset
}

private class ColorPickerStateImpl(
    private val borderConfiguration: ColorPickerHandleBorderConfiguration
): ColorPickerState, HandlePosition, TwoDirectionsMovable {

    @Suppress("NOTHING_TO_INLINE")
    private inline fun calculateColor(): Color {
        return Color(
            HSBtoRGB(
                Math.toDegrees(acos(offset.x).toDouble()).toFloat(),
                with(offset.pow(2)) { sqrt(x + y) / borderConfiguration.colorCircleRadius},
                brightness
            )
        )
    }

    private val _offset = mutableStateOf(borderConfiguration.colorCircleCenter)

    override var offset: Offset
        get() = _offset.value
        set(newOffset) {
            _offset.value = newOffset
            _color.value = calculateColor()
        }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun calculateOffset(): Offset {
        val hsb = java.awt.Color.RGBtoHSB(
            color.red.roundToInt(),
            color.green.roundToInt(),
            color.blue.roundToInt(),null
        )
        val angle = hsb[0]
        val radius = hsb[1]
        return Offset(
            (radius * cos(Math.toRadians(angle.toDouble()))).toFloat(),
            (radius * sin(Math.toRadians(angle.toDouble()))).toFloat()
        ) + borderConfiguration.colorCircleCenter
    }

    private val _brightness = mutableStateOf(1f)

    override var brightness: Float
        get() = _brightness.value
        set(newBrightness) {
            _brightness.value = newBrightness
            _color.value = calculateColor()
        }


    private val _color = mutableStateOf(Color.Transparent)
    override var color: Color
        get() = _color.value
        set(newColor) {
            _color.value = newColor
            _offset.value = calculateOffset()
        }

    private val twoDirectionsMovableState = movableState(this::onMove)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Offset.pow(power: Int): Offset = copy(x.pow(power),y.pow(power))

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Offset.coerceInCircle(
        center: Offset,
        radius: Float
    ): Offset {
        val centerRelativePoint = this - center
        return if (with((centerRelativePoint).pow(2)){ sqrt(x + y) } > radius) {
            val t = acos(x)
            Offset(radius * cos(t), radius * sin(t)) + center
        } else {
            this
        }
    }

    private fun onMove(xDelta: Float, yDelta: Float) {
        with(borderConfiguration) {
            offset = (offset + Offset(xDelta, yDelta)).coerceInCircle(colorCircleCenter, colorCircleRadius)
        }
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

@Composable
actual fun rememberColorPickerState(
    color: Color,
    brightness: Float
): ColorPickerState {
    return remember { ColorPickerStateImpl(ColorPickerHandleBorderConfiguration(Offset(0f,0f),20f)) }
}