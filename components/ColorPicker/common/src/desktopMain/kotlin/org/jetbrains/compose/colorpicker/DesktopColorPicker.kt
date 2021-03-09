package org.jetbrains.compose.colorpicker

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
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
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.anyPositionChangeConsumed
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.movable.TwoDirectionsMovable
import org.jetbrains.compose.movable.dispatchRawMovement
import org.jetbrains.compose.movable.smoothMoveTo
import java.awt.Color.HSBtoRGB
import kotlin.coroutines.coroutineContext
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val angleStep: Float = 1f / 360
private const val DEFAULT_SATURATION = 1f
private const val DEFAULT_BRIGHTNESS = 1f

private object StaticConstants {
    @JvmStatic
    val colors = Array(360) { angle ->
        Color(
            HSBtoRGB(
                angleStep * angle,
                DEFAULT_SATURATION,
                DEFAULT_BRIGHTNESS
            )
        )
    }
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
                            colors = listOf(Color.Transparent, angleColor),
                            center = Offset(this@onDrawBehind.size.center.x, this@onDrawBehind.size.center.y),
                            radius = arcSize.minDimension / 2,
                            tileMode = TileMode.Clamp
                        ),
                        startAngle = index.toFloat(),
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

internal typealias OffsetCoerceIn = (Offset) -> Offset

internal fun Modifier.twoDirectionsDragProcess(
    movable: TwoDirectionsMovable,
    coerceInMovementBorder: OffsetCoerceIn = { it }
): Modifier {
    return this
        .pointerInput(movable) {
            detectDragGestures { change, dragAmount ->  }
            forEachGesture {
                val scope = CoroutineScope(coroutineContext)
                awaitPointerEventScope {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var drag: PointerInputChange?
                    do {
                        drag = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                            change.consumeAllChanges()
                            val currentPos = coerceInMovementBorder(change.position - down.position)
                            scope.launch {
                                movable.move {
                                    moveBy(currentPos.x, currentPos.y)
                                }
                            }
                        }
                    } while (drag != null && !drag.anyPositionChangeConsumed())
                    if (drag != null) {
                        drag(drag.id) {
                            it.consumeAllChanges()
                            val currentPos = coerceInMovementBorder(it.position - down.position)
                            scope.launch {
                                movable.move {
                                    moveBy(currentPos.x, currentPos.y)
                                }
                            }
                        }
                    }
                }
            }
        }
}

@Composable
internal fun ColorPickerHandle(
    handleState: TwoDirectionsMovable,
    handleMovementBorder: OffsetCoerceIn
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
        .twoDirectionsDragProcess(
            handleState,
            handleMovementBorder
        )
)

@Composable
actual fun ColorCircle(
    modifier: Modifier,
    colorPickerState: ColorPickerState
) = BoxWithConstraints(
    modifier
) {

    val coerceInCircle = remember(maxWidth, maxHeight) {
            val cic = CoerceInCircle(
                Offset(
                    maxWidth.value / 2,
                    maxHeight.value / 2
                ),
                min(maxWidth,maxHeight).value
            )
        println("Center and r: $cic")
        cic
    }

    val converter = remember(coerceInCircle, colorPickerState.brightness) {
        ColorToOffsetBiDirectionalConverter(
            coerceInCircle.circleRadius,
            coerceInCircle.circleCenter,
            colorPickerState.brightness
        )
    }

    val handleState = remember(colorPickerState) {
        ColorPickerHandleState()
    }

    var cachedColor by remember { mutableStateOf(colorPickerState.color) }

    LaunchedEffect(colorPickerState) {
        handleState.dispatchRawMovement(with(converter) { colorPickerState.color.toOffset() - handleState.offset })
    }

    LaunchedEffect(colorPickerState.color) {
        println("Color effect")
        if (cachedColor != colorPickerState.color) {
            cachedColor = colorPickerState.color
            handleState.smoothMoveTo(handleState.offset, with(converter) { colorPickerState.color.toOffset() })
        }
    }

    LaunchedEffect(handleState.offset) {
        println("Offset effect")
        val newColor = with(converter) { handleState.offset.toColor() }
        cachedColor = newColor
        colorPickerState.color = newColor
    }

    ColorCircleWithHandle(
        modifier,
        handleState,
        coerceInCircle
    )
}

private data class CoerceInCircle(
    val circleCenter: Offset,
    val circleRadius: Float
) : OffsetCoerceIn {

    override fun invoke(incomeOffset: Offset): Offset {
        val centerRelativePoint = incomeOffset - circleCenter
        return if (with((centerRelativePoint).pow(2)){ sqrt(x + y) } > circleRadius) {
            val t = acos(incomeOffset.x)
            Offset(circleRadius * cos(t), circleRadius * sin(t)) + circleCenter
        } else {
            incomeOffset
        }
    }

}

@Composable
private fun ColorCircleWithHandle(
    modifier: Modifier,
    handleState: MovableHandlerState,
    coerceInCircle: OffsetCoerceIn
) {
    Layout(
        {
            ColorPickerHandle(handleState,coerceInCircle)
        },
        modifier
            .fillMaxSize()
            .drawColorCircle()
            .pointerInput(handleState) {
                coroutineScope {
                    detectTapGestures {
                        println("taped on $it")
                        launch { handleState.smoothMoveTo(handleState.offset,it) }
                    }
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
}