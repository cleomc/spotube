/*
 * Copyright (C) 2026 Kingkor Roy Tirtho and Spotube Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.spotube.spotube.resources.iconsax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Name of the echo group inside every Flux icon (see [fluxIcon]). */
const val FLUX_ICON_ECHO = "echo"

/** Name of the ink group inside every Flux icon (see [fluxIcon]). */
const val FLUX_ICON_INK = "ink"

/** Offset of the echo stroke, in viewport units (24 = icon size). */
const val FLUX_ICON_ECHO_OFFSET = 0.9f

/**
 * Builds a Flux echo icon: 24 × 24, 1.6 round strokes, drawn twice. The [FLUX_ICON_ECHO] group is
 * the same geometry shifted 0.9 right and down at 38 % alpha, like a print slightly out of
 * register; the [FLUX_ICON_INK] group is the icon itself. Every colour is a white placeholder that
 * the icon's tint replaces, so a plain `MillenniumIcon` shows a tone-on-tone echo, while
 * `FluxIcon` draws the two groups separately to colour the echo.
 */
fun fluxIcon(name: String, strokes: List<String>, fills: List<String> = emptyList()): ImageVector {
    val builder = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )
    fun ImageVector.Builder.layer(alpha: Float) {
        strokes.forEach { d ->
            addPath(
                pathData = addPathNodes(d),
                stroke = SolidColor(Color.White),
                strokeAlpha = alpha,
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        fills.forEach { d ->
            addPath(
                pathData = addPathNodes(d),
                fill = SolidColor(Color.White),
                fillAlpha = alpha,
                stroke = SolidColor(Color.White),
                strokeAlpha = alpha,
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }
    builder.addGroup(
        name = FLUX_ICON_ECHO,
        translationX = FLUX_ICON_ECHO_OFFSET,
        translationY = FLUX_ICON_ECHO_OFFSET,
    )
    builder.layer(alpha = 0.38f)
    builder.clearGroup()
    builder.addGroup(name = FLUX_ICON_INK)
    builder.layer(alpha = 1f)
    builder.clearGroup()
    return builder.build()
}
