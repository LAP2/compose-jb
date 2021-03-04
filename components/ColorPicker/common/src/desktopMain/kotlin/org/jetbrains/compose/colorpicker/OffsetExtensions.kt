package org.jetbrains.compose.colorpicker

import androidx.compose.ui.geometry.Offset
import kotlin.math.pow

@Suppress("NOTHING_TO_INLINE")
internal inline fun Offset.pow(power: Int): Offset = copy(x.pow(power),y.pow(power))