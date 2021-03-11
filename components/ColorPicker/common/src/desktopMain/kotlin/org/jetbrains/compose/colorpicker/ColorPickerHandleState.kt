package org.jetbrains.compose.colorpicker

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
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
    suspend fun moveBy(delta: Offset)
    suspend fun moveTo(target: Offset)
    suspend fun setOffset(newOffset: Offset)
}

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
        val angle = hsb[0] * 360
        val radius = hsb[1] * colorCircleRadius
        return Offset(
            (radius * cos(Math.toRadians(angle.toDouble()))).toFloat(),
            (radius * sin(Math.toRadians(angle.toDouble()))).toFloat()
        ) + colorCircleCenter
    }
}

internal class ColorPickerHandleState(
    private val converter: ColorToOffsetBiDirectionalConverter,
    private val colorCircleState: ColorCircleState,
    private val animationSpec: AnimationSpec<Offset> = tween()
) : HandlePosition {

    private val animatableOffset = Animatable(colorCircleState.color.toOffset(), Offset.VectorConverter)

    override val offset: Offset
        get() = animatableOffset.value

    override suspend fun setOffset(newOffset: Offset) {
        animatableOffset.snapTo(newOffset)
        producedColor = offset.toColor()
        colorCircleState.color = producedColor
    }

    override suspend fun moveBy(delta: Offset) {
        moveTo(offset + delta)
    }

    override suspend fun moveTo(target: Offset) {
        animatableOffset.animateTo(
            target,
            animationSpec = animationSpec
        ) {
            producedColor = this.value.toColor()
            colorCircleState.color = producedColor
        }
    }

    suspend fun moveToColor(target: Color) {
        moveTo(target.toOffset())
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Color.toOffset(): Offset = with(converter){this@toOffset.toOffset()}

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Offset.toColor(): Color = with(converter){this@toColor.toColor()}

    var producedColor: Color = colorCircleState.color
        private set

}