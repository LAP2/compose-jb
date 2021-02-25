package org.jetbrains.compose.colorpicker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadVectorXmlResource
import androidx.compose.ui.res.vectorXmlResource
import androidx.compose.ui.unit.dp
import org.xml.sax.InputSource
import kotlin.math.floor

internal data class AngleColor(
    val angle: Float,
    val color: Color
)

internal fun convertHSBToRGB(
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

const val angleStep: Float = 1f / 360
const val DEFAULT_SATURATION = 1f
const val DEFAULT_BRIGHTNESS = 1f

private object StaticConstants {
    @JvmStatic
    val colors = generateSequence(0f) { it + angleStep }.take(360).mapIndexed { angle, hue ->
        AngleColor(angle.toFloat(), convertHSBToRGB(hue, DEFAULT_SATURATION, DEFAULT_BRIGHTNESS))
    }.toList()
}

@Composable
internal actual fun ColorCircle(
    modifier: Modifier
) = Box(
    modifier = modifier.fillMaxSize()
) {
    val vi = rememberVectorPainter(
        ImageVector.Builder(
            defaultWidth = 50.dp,
            defaultHeight = 50.dp,
            viewportWidth = 50f,
            viewportHeight = 50f,
        ).apply {
            path(
                stroke = SolidColor(Color.Red),
                strokeLineMiter = 1f,
                strokeLineWidth = 1f,
            ) {
                moveTo(0f,0f)
                lineTo(50f, 50f)
            }
        }.build()
    )
    Icon(
        vi,
        "Circle",
        modifier.matchParentSize(),
        tint = Color.Unspecified
    )
}
//    Box(
//        modifier = modifier
//            .fillMaxSize()
//            .drawWithCache {
//                val center = size.center
//                val arcSizePart = size.minDimension
//                val arcLeftCorner = Offset(center.x - (arcSizePart / 2), center.y - (arcSizePart / 2))
//                val arcSize = Size(arcSizePart - arcLeftCorner.x, arcSizePart - arcLeftCorner.y)
//                onDrawBehind {
//                    with(StaticConstants.colors) {
//                        for (index in indices) {
//                            val angleColor = get(index)
//                            drawArc(
//                                brush = Brush.radialGradient(
//                                    colors = listOf(Color.Transparent, angleColor.color),
//                                    center = Offset(this@onDrawBehind.size.center.x, this@onDrawBehind.size.center.y),
//                                    radius = arcSize.minDimension / 2,
//                                    tileMode = TileMode.Clamp
//                                ),
//                                startAngle = angleColor.angle,
//                                sweepAngle = 1.5f,
//                                useCenter = true,
//                                size = arcSize,
//                                topLeft = arcLeftCorner
//                            )
//                        }
//                    }
//                }
//            }
//    )