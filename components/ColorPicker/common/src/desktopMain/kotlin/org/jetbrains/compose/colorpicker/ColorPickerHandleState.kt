package org.jetbrains.compose.colorpicker

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.awt.Color.HSBtoRGB
import java.awt.Color.RGBtoHSB
import kotlin.math.absoluteValue
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.roundToInt

internal interface HandlePosition {
    val offset: Offset
    suspend fun smoothMoveBy(delta: Offset)
    suspend fun smoothMoveTo(target: Offset)
    suspend fun moveTo(target: Offset)
}

internal class ColorToOffsetBiDirectionalConverter(
    private val colorCircleRadius: Float,
    private val colorCircleCenter: Offset,
    private val brightness: Float
) {
    @Suppress("NOTHING_TO_INLINE")
    inline fun Offset.toColor(): Color {
        val currentRelativePoint = this - colorCircleCenter
        val currentRadius = with(currentRelativePoint.pow(2)) { sqrt(x + y) }
        val hue = with(currentRelativePoint) {
            if (y < 0) {
                val dav = Math.toDegrees(asin(y / currentRadius).toDouble()).absoluteValue
                if (x < 0) { 180 + dav } else { 360 - dav }
            } else {
                Math.toDegrees(acos(x / currentRadius).toDouble())
            } / 360
        }.toFloat()
        val color =  Color(
            HSBtoRGB(
                hue,
                currentRadius / colorCircleRadius,
                brightness
            )
        )
        println(color)
        return color
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun Color.toOffset(): Offset {
        println(this)
        val hsb = RGBtoHSB(
            (red*255).roundToInt(),
            (green*255).roundToInt(),
            (blue*255).roundToInt(),null
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

    override suspend fun moveTo(target: Offset) {
        animatableOffset.snapTo(target)
        producedColor = offset.toColor()
        colorCircleState.color = producedColor
    }

    override suspend fun smoothMoveBy(delta: Offset) {
        smoothMoveTo(offset + delta)
    }

    override suspend fun smoothMoveTo(target: Offset) {
        animatableOffset.animateTo(
            target,
            animationSpec = animationSpec
        ) {
            producedColor = this.value.toColor()
            colorCircleState.color = producedColor
        }
    }

    suspend fun moveToColor(target: Color) {
        println("moved")
        animatableOffset.animateTo(
            target.toOffset(),
            animationSpec = animationSpec
        )
        println("color installed")
        producedColor = target
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Color.toOffset(): Offset = with(converter){this@toOffset.toOffset()}

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Offset.toColor(): Color = with(converter){this@toColor.toColor()}

    var producedColor: Color = colorCircleState.color
        private set

}