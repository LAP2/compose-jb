package org.jetbrains.compose.state

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Remeasurement
import androidx.compose.ui.layout.RemeasurementModifier
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.datatable.*
import org.jetbrains.compose.datatable.DataGridMeasureResult
import org.jetbrains.compose.datatable.model.CellPosition
import kotlin.math.abs

class DataGridState {

    private val scrollsPosition = DataGridScrollsPosition()

    val firstVisibleCellPosition: CellPosition get() = scrollsPosition.observablePosition

    val firstVisibleCellVerticalScrollOffset: Int get() = scrollsPosition.observableVerticalScrollOffset

    val firstVisibleCellHorizontalScrollOffset: Int get() = scrollsPosition.observableHorizontalScrollOffset

    // TODO add layout info

    val interactionSource: InteractionSource get() = internalInteractionSource

    internal val internalInteractionSource: MutableInteractionSource = MutableInteractionSource()

    internal var verticalScrollToBeConsumed = 0f
        private set

    internal var horizontalScrollToBeConsumed = 0f
        private set

    internal val scrollToBeConsumed: Offset
        internal get() {
            return Offset(horizontalScrollToBeConsumed, verticalScrollToBeConsumed)
        }

    internal val firstVisibleCellPositionNonObservable: CellPosition get() = scrollsPosition.position

    internal val firstVisibleCellVerticalScrollOffsetNonObservable: Int get() = scrollsPosition.scrollOffset.verticalOffset

    internal val firstVisibleCellHorizontalScrollOffsetNonObservable: Int get() = scrollsPosition.scrollOffset.horizontalOffset

    internal val firstVisibleCellScrollOffsetNonObservable: ScrollOffset get() = scrollsPosition.scrollOffset

    internal var visibleCellsCount = 0

    internal var density: Density = Density(1f, 1f)

    internal var onScrolledListener: DataGridOnScrolledListener? = null
    internal var onPostMeasureListener: DataGridOnPostMeasureListener? = null

    internal val verticalScrollableState = ScrollableState { -onScroll(-it, true) }
    internal val horizontalScrollableState = ScrollableState { -onScroll(-it, false) }

    private var canScrollBackward: Boolean = true
    private var canScrollForward: Boolean = true

    internal lateinit var remeasurement: Remeasurement

    internal val remeasurementModifier = object : RemeasurementModifier {
        override fun onRemeasurementAvailable(remeasurement: Remeasurement) {
            this@DataGridState.remeasurement = remeasurement
        }
    }

    internal fun onScroll(distance: Float, isVertical: Boolean): Float {
        println("Distance: $distance")
        if (distance < 0 && !canScrollForward || distance > 0 && !canScrollBackward) {
            return 0f
        }
        check(abs(if (isVertical) verticalScrollToBeConsumed else horizontalScrollToBeConsumed) <= 0.5f) {
            "entered drag with non-zero pending scroll: ${if (isVertical) verticalScrollToBeConsumed else horizontalScrollToBeConsumed}"
        }
        if (isVertical) verticalScrollToBeConsumed += distance else horizontalScrollToBeConsumed += distance
        println("We are here")
        println("Vertical scroll to be consumed: $verticalScrollToBeConsumed")
        println("Horizontal scroll to be consumed: $horizontalScrollToBeConsumed")
        println("Scroll to be consumed: $scrollToBeConsumed")
        // scrollToBeConsumed will be consumed synchronously during the forceRemeasure invocation
        // inside measuring we do scrollToBeConsumed.roundToInt() so there will be no scroll if
        // we have less than 0.5 pixels
        if (abs(if (isVertical) verticalScrollToBeConsumed else horizontalScrollToBeConsumed) > 0.5f) {
            val preScrollToBeConsumed = if (isVertical) verticalScrollToBeConsumed else horizontalScrollToBeConsumed
            remeasurement.forceRemeasure()
            if (isVertical) {
                onScrolledListener?.onScrolled(
                    0f,
                    preScrollToBeConsumed - verticalScrollToBeConsumed
                )
            } else {
                onScrolledListener?.onScrolled(
                    preScrollToBeConsumed - horizontalScrollToBeConsumed,
                    0f
                )
            }
        }

        // here scrollToBeConsumed is already consumed during the forceRemeasure invocation
        if (abs(if (isVertical) verticalScrollToBeConsumed else horizontalScrollToBeConsumed) <= 0.5f) {
            // We consumed all of it - we'll hold onto the fractional scroll for later, so report
            // that we consumed the whole thing
            return distance
        } else {
            val scrollConsumed = distance - if (isVertical) verticalScrollToBeConsumed else horizontalScrollToBeConsumed
            // We did not consume all of it - return the rest to be consumed elsewhere (e.g.,
            // nested scrolling)
            if (isVertical) verticalScrollToBeConsumed = 0f else horizontalScrollToBeConsumed = 0f // We're not consuming the rest, give it back
            return scrollConsumed
        }
    }

    internal fun applyMeasuredGrid(
        gridMeasureResult: DataGridMeasureResult
    ) {

    }

    internal fun updateScrollsPositionIfTheFirstCellsWasMoved(cells: DataGridCellProvider) {
        scrollsPosition.updateScrollsPositionIfTheFirstCellsWasMoved(cells)
    }

}

internal interface DataGridOnScrolledListener {
    fun onScrolled(hDelta: Float, vDelta: Float)
}

internal interface DataGridOnPostMeasureListener {
    fun SubcomposeMeasureScope.onPostMeasure(
        childConstraints: Constraints,
        result: DataGridMeasureResult
    )
}

@Composable
fun StateTestFun(

) {
    val state = remember { DataGridState() }
    Box(
        Modifier
            .scrollable(state.verticalScrollableState,Orientation.Vertical)
            .fillMaxSize()
            .then(state.remeasurementModifier)
    ) {
        Box(
            Modifier.size(500.dp)
                .background(Color.Red)
        )
    }
}