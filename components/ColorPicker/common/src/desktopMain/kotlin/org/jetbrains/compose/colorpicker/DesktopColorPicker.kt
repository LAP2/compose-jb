package org.jetbrains.compose.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withRunningRecomposer
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.anyPositionChangeConsumed
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.movable.dispatchRawMovement
import kotlin.coroutines.coroutineContext
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private data class AngleColor(
    val angle: Float,
    val color: Color
)

private const val angleStep: Float = 1f / 360
private const val DEFAULT_SATURATION = 1f
private const val DEFAULT_BRIGHTNESS = 1f

private object StaticConstants {
    @JvmStatic
    val colors = generateSequence(0f) { it + angleStep }
        .take(360)
        .mapIndexed { angle, hue ->
            AngleColor(angle.toFloat(), convertHSBToRGB(hue, DEFAULT_SATURATION, DEFAULT_BRIGHTNESS))
        }.toList()
}

private fun Modifier.drawColorCircle(): Modifier {
    return clip(
        GenericShape { size, _ ->
            addArc(
                oval = Rect(size.center, size.minDimension / 2),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 360F
            )
        }
    ).drawWithCache {
        val center = size.center
        val arcSizePart = size.minDimension
        val arcLeftCorner = Offset(center.x - (arcSizePart / 2), center.y - (arcSizePart / 2))
        val arcSize = Size(arcSizePart - arcLeftCorner.x, arcSizePart - arcLeftCorner.y)
        onDrawBehind {
            with(StaticConstants.colors) {
                for (index in indices) {
                    val angleColor = get(index)
                    drawArc(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, angleColor.color),
                            center = Offset(this@onDrawBehind.size.center.x, this@onDrawBehind.size.center.y),
                            radius = arcSize.minDimension / 2,
                            tileMode = TileMode.Clamp
                        ),
                        startAngle = angleColor.angle,
                        sweepAngle = 1.5f,
                        useCenter = true,
                        size = arcSize,
                        topLeft = arcLeftCorner
                    )
                }
            }
        }
    }
}

@Composable
internal fun ColorPickerHandle(
    handleState: ColorPickerHandleState
) = Box(
    modifier = Modifier
        .size(10.dp)
        .border(
            2.dp,
            Color.Black,
            GenericShape { size, _ ->
                addArc(
                    oval = Rect(
                        center = size.center,
                        radius = 4f
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 360f
                )
            }
        )
        .pointerInput(handleState) {
            forEachGesture {
                val scope = CoroutineScope(coroutineContext)
                awaitPointerEventScope {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var drag: PointerInputChange?
                    do {
                        drag = awaitTouchSlopOrCancellation(down.id) {change, _ ->
                            change.consumeAllChanges()
                            val currentPos = change.position - down.position
                            scope.launch {
                                handleState.move {
                                    moveBy(currentPos.x, currentPos.y)
                                }
                            }
                        }
                    } while (drag != null && !drag.anyPositionChangeConsumed())
                    if (drag != null) {
                        drag(drag.id) {
                            it.consumeAllChanges()
                            val currentPos = it.position - down.position
                            scope.launch {
                                handleState.move {
                                    moveBy(currentPos.x, currentPos.y)
                                }
                            }
                        }
                    }
                }
            }
        }
)

@Composable
private fun ColorCircle(
    modifier: Modifier = Modifier
) = Box(
    modifier
        .drawColorCircle()
)

@Composable
fun CPTst(
    modifier: Modifier = Modifier
) = BoxWithConstraints(
    modifier
) {

}

@Composable
actual fun ColorPicker(
    modifier: Modifier,
    handleState: ColorPickerHandleState
) = Layout(
    {
        ColorPickerHandle(handleState)
    },
    modifier
        .fillMaxSize()
        .drawColorCircle()
        .pointerInput(handleState) {
            detectTapGestures {
                handleState.offset = it
            }
        }
) { measurables: List<Measurable>, constraints: Constraints ->

    val colorPickerHandlePlaceable = measurables[0].measure(
        Constraints(
            maxWidth = 10,
            maxHeight = 10
        )
    )

    val xCenterDelta = colorPickerHandlePlaceable.width / 2
    val yCenterDelta = colorPickerHandlePlaceable.height / 2

    layout(constraints.maxWidth, constraints.maxHeight) {
        colorPickerHandlePlaceable.place(
            handleState.offset.x.roundToInt() - xCenterDelta,
            handleState.offset.y.roundToInt() - yCenterDelta
        )
    }
}