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

package org.spotube.spotube.core.ui.theming

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import org.spotube.millennium.theme.MillenniumColorSchemes
import org.spotube.millennium.theme.MillenniumColors
import org.spotube.millennium.theme.MillenniumPalette
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/*
 * Spotube Flux palettes. Two appearances (Nuit = dark, Papier = light), each in three accent
 * families that replace Millennium's Blue / Orange / Lime: Flux, Braise and Prisme.
 */

/*
 * Nuit is tuned for OLED screens: the ground and wells are true black (pixels off), surfaces sit
 * just above it, and `border` stays at 3:1 on black so hairlines remain visible.
 */
private fun nuit(accent: Long, top: Long, bottom: Long) = MillenniumColors(
    background = Color(0xFF000000), surface = Color(0xFF0A090E), raised = Color(0xFF16141D),
    inset = Color(0xFF000000), text = Color(0xFFF4F1FF), muted = Color(0xFFA9A4C2),
    border = Color(0xFF5E5A80), highlight = Color.White, accent = Color(accent),
    accentTop = Color(top), accentBottom = Color(bottom), onAccent = Color.White,
    success = Color(0xFF6FE3B0), danger = Color(0xFFFF7A7A), scrim = Color(0xCC000000),
)

private fun papier(accent: Long, top: Long, bottom: Long) = MillenniumColors(
    background = Color(0xFFEBE3B9), surface = Color(0xFFF2ECCE), raised = Color(0xFFF9F5E2),
    inset = Color(0xFFE0D6A6), text = Color(0xFF0B1170), muted = Color(0xFF454A85),
    border = Color(0xFF7A779C), highlight = Color.White, accent = Color(accent),
    accentTop = Color(top), accentBottom = Color(bottom), onAccent = Color.White,
    success = Color(0xFF1E7347), danger = Color(0xFFB3261E), scrim = Color(0x660B1170),
)

/** Millennium palette slots mapped to Flux (Blue), Braise (Orange) and Prisme (Lime). */
val FluxColorSchemes = MillenniumColorSchemes(
    light = papier(0xFF1A3BC9, 0xFF1E4ED8, 0xFFC13A15),
    dark = nuit(0xFF8FA8FF, 0xFF2253E6, 0xFFC43C16),
    orangeLight = papier(0xFFA92D17, 0xFFB8301A, 0xFF2A1CAB),
    orangeDark = nuit(0xFFFF9470, 0xFFC4341D, 0xFF2C1FB4),
    limeLight = papier(0xFF5A2DC2, 0xFF4430D6, 0xFF93309F),
    limeDark = nuit(0xFFC7A8FF, 0xFF4B37E0, 0xFF9C35AD),
)

/**
 * Flux-only colour roles that Millennium has no slot for: the decorative aurora colours, the
 * fluted glass fill and the film grain ink.
 */
@Immutable
data class FluxColors(
    val dark: Boolean,
    val accentTop: Color,
    val accentBottom: Color,
    val flowA: Color,
    val flowB: Color,
    val flowC: Color,
    val glass: Color,
    val rib: Color,
    val grainBlend: Color,
)

fun fluxColorsFor(palette: MillenniumPalette, dark: Boolean, colors: MillenniumColors): FluxColors {
    val (a, b, c) = when (palette) {
        MillenniumPalette.Blue -> Triple(0xFF136BE4, 0xFFF05A28, 0xFF6A3CF0)
        MillenniumPalette.Lime -> Triple(0xFF577CF8, 0xFFD88DE5, 0xFFA487F7)
        MillenniumPalette.Orange -> Triple(0xFFC14B3D, 0xFFFF7A2A, 0xFF311CB3)
    }
    return FluxColors(
        dark = dark,
        accentTop = colors.accentTop,
        accentBottom = colors.accentBottom,
        flowA = Color(a),
        flowB = Color(b),
        flowC = Color(c),
        glass = if (dark) Color(0xB3050409) else Color(0xB8F2ECCE),
        rib = if (dark) Color(0x12FFFFFF) else Color(0x73FFFFFF),
        grainBlend = if (dark) Color.White else Color(0xFF2A2410),
    )
}

/**
 * The flux fill: a linear gradient from [start] to [end] at [angleDegrees] (CSS convention,
 * 0° = upwards, 90° = to the right), sized to whatever it paints.
 */
@Immutable
class FluxGradientBrush(
    private val start: Color,
    private val end: Color,
    private val angleDegrees: Float = 120f,
) : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        val rad = (angleDegrees - 90f) * PI.toFloat() / 180f
        val dx = cos(rad)
        val dy = sin(rad)
        // CSS gradient line length: covers the box corners for any angle.
        val half = (abs(size.width * dx) + abs(size.height * dy)) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        return LinearGradientShader(
            from = Offset(center.x - dx * half, center.y - dy * half),
            to = Offset(center.x + dx * half, center.y + dy * half),
            colors = listOf(start, end),
        )
    }

    override fun equals(other: Any?): Boolean =
        other is FluxGradientBrush && other.start == start && other.end == end &&
            other.angleDegrees == angleDegrees

    override fun hashCode(): Int = (start.hashCode() * 31 + end.hashCode()) * 31 + angleDegrees.hashCode()
}

fun fluxBrush(colors: MillenniumColors): FluxGradientBrush =
    FluxGradientBrush(colors.accentTop, colors.accentBottom)
