package org.jetbrains.compose.colorpicker

import androidx.compose.foundation.MutatePriority
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import org.jetbrains.compose.movable.TwoDirectionsMovable
import org.jetbrains.compose.movable.TwoDirectionsMoveScope
import org.jetbrains.compose.movable.movableState
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class ColorPickerHandleBorderConfiguration(
    val colorCircleCenter: Offset,
    val colorCircleRadius: Float
)

fun convertHSBToRGB(
    hue: Float,
    saturation: Float,
    brightness: Float
): Color {
    var r = 0
    var g = 0
    var b = 0
    if (saturation == 0f) {
        b = (brightness * 255.0f + 0.5f).toInt()
        g = b
        r = g
    } else {
        val h = (hue - floor(hue.toDouble()).toFloat()) * 6.0f
        val f = h - floor(h.toDouble()).toFloat()
        val p = brightness * (1.0f - saturation)
        val q = brightness * (1.0f - saturation * f)
        val t = brightness * (1.0f - saturation * (1.0f - f))
        when (h.toInt()) {
            0 -> {
                r = (brightness * 255.0f + 0.5f).toInt()
                g = (t * 255.0f + 0.5f).toInt()
                b = (p * 255.0f + 0.5f).toInt()
            }
            1 -> {
                r = (q * 255.0f + 0.5f).toInt()
                g = (brightness * 255.0f + 0.5f).toInt()
                b = (p * 255.0f + 0.5f).toInt()
            }
            2 -> {
                r = (p * 255.0f + 0.5f).toInt()
                g = (brightness * 255.0f + 0.5f).toInt()
                b = (t * 255.0f + 0.5f).toInt()
            }
            3 -> {
                r = (p * 255.0f + 0.5f).toInt()
                g = (q * 255.0f + 0.5f).toInt()
                b = (brightness * 255.0f + 0.5f).toInt()
            }
            4 -> {
                r = (t * 255.0f + 0.5f).toInt()
                g = (p * 255.0f + 0.5f).toInt()
                b = (brightness * 255.0f + 0.5f).toInt()
            }
            5 -> {
                r = (brightness * 255.0f + 0.5f).toInt()
                g = (p * 255.0f + 0.5f).toInt()
                b = (q * 255.0f + 0.5f).toInt()
            }
        }
    }
    return Color(r, g, b)
}

interface HandleOffset {
    var offset: Offset
}

interface ColorPickable {
    var color: Color
}

class ColorPickerHandleState(
    private val borderConfiguration: ColorPickerHandleBorderConfiguration
): TwoDirectionsMovable, HandleOffset, ColorPickable {

    @Suppress("NOTHING_TO_INLINE")
    private inline fun calculateColor(): Color {
        val hue = Math.toDegrees(acos(offset.x).toDouble()).toFloat()
        val saturation = with(offset.pow(2)) { sqrt(x + y) / borderConfiguration.colorCircleRadius}
        val brightness = 1f // TODO add brightness parameter
        return convertHSBToRGB(hue, saturation, brightness)
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
            (radius*cos(Math.toRadians(angle.toDouble()))).toFloat(),
            (radius*sin(Math.toRadians(angle.toDouble()))).toFloat()
        ) + borderConfiguration.colorCircleCenter
    }

    private val _color = mutableStateOf(Transparent)
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
        return if (with((centerRelativePoint).pow(2)){ sqrt(x+y) } > radius) {
            val t = acos(x)
            Offset(radius * cos(t),radius * sin(t)) + center
        } else {
            this
        }
    }

    private fun onMove(xDelta: Float, yDelta: Float) {
        with(borderConfiguration) {
            offset = (offset + Offset(xDelta,yDelta)).coerceInCircle(colorCircleCenter, colorCircleRadius)
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