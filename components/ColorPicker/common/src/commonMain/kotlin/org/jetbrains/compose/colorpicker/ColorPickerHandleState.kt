package org.jetbrains.compose.colorpicker

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.movable.TwoDirectionsMovable
import org.jetbrains.compose.movable.TwoDirectionsMoveScope
import org.jetbrains.compose.movable.movableState
import kotlin.math.roundToInt

interface MovementDeltaFilter

class ColorPickerHandleState : TwoDirectionsMovable {

    private val _x = mutableStateOf(0f)

    var x: Float
        get() = _x.value
        set(newX) {
            _x.value = newX
        }

    private val _y = mutableStateOf(0f)

    var y: Float
        get() = _y.value
        set(newY) {
            _y.value = newY
        }


    private val twoDirectionsMovableState = movableState(this::onMove)

    private fun onMove(
        xDelta: Float,
        yDelta: Float
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun move(
        movePriority: MutatePriority,
        block: suspend TwoDirectionsMoveScope.() -> Unit) = twoDirectionsMovableState.move(movePriority, block)

    override fun dispatchRawMovement(
        xDelta: Float,
        yDelta: Float
    ) = twoDirectionsMovableState.dispatchRawMovement(xDelta, yDelta)

    override val isMoveInProgress: Boolean = twoDirectionsMovableState.isMoveInProgress

}
@Composable
internal fun ColorCircle() {

}

@Composable
internal fun ColorPickerHandle(

) = Box(
    modifier = Modifier
        .size(14.dp)
        .drawWithCache {
            onDrawBehind {

            }
        }
) {

}

@Composable
fun ColorPickerWithHandle(
    modifier: Modifier = Modifier,
    colorPickerHandleState: ColorPickerHandleState
) = Layout(
    {
        ColorCircle()
        ColorPickerHandle()
    },
    modifier
) { measurables: List<Measurable>, constraints: Constraints ->

    val circlePlaceable = measurables[0].measure(constraints)
    val handle = measurables[1].measure(constraints)

    layout(constraints.maxWidth, constraints.maxHeight) {
        circlePlaceable.place(0,0)
        handle.place(colorPickerHandleState.x.roundToInt(), colorPickerHandleState.y.roundToInt())
    }

}