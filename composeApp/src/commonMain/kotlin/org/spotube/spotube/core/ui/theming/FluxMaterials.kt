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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * One ambient clock for the whole app, in seconds (0 to [AMBIENT_LOOP_SECONDS]), so every screen's
 * aurora sits at the same phase and screen transitions do not jump. Read it only in draw scopes.
 */
private val LocalFluxAmbient = staticCompositionLocalOf<State<Float>> { mutableFloatStateOf(0f) }

/** Least common multiple of the 26/32/38 s aurora loops would be huge; one long loop is enough. */
private const val AMBIENT_LOOP_SECONDS = 26f * 32f * 38f / 16f

@Composable
fun ProvideFluxAmbient(content: @Composable () -> Unit) {
    val transition = rememberInfiniteTransition(label = "flux-ambient")
    val time = transition.animateFloat(
        initialValue = 0f,
        targetValue = AMBIENT_LOOP_SECONDS,
        animationSpec = infiniteRepeatable(
            tween((AMBIENT_LOOP_SECONDS * 1000).toInt(), easing = LinearEasing),
            RepeatMode.Restart,
        ),
        label = "flux-ambient-time",
    )
    CompositionLocalProvider(LocalFluxAmbient provides time, content = content)
}

private class Blob(
    val x: Float, val y: Float, val radius: Float,
    val period: Float, val phase: Float,
)

private val Blobs = listOf(
    Blob(0.18f, 0.12f, 0.70f, 26f, 0.0f),
    Blob(0.88f, 0.30f, 0.62f, 32f, 1.7f),
    Blob(0.45f, 0.95f, 0.66f, 38f, 3.1f),
)

private fun DrawScope.drawAurora(flux: FluxColors, time: Float) {
    val colors = listOf(flux.flowA, flux.flowB, flux.flowC)
    val strength = if (flux.dark) 0.42f else 0.30f
    val dim = max(size.width, size.height)
    Blobs.forEachIndexed { i, blob ->
        val angle = (time / blob.period) * 2f * PI.toFloat() + blob.phase
        val cx = (blob.x + 0.10f * sin(angle)) * size.width
        val cy = (blob.y + 0.10f * sin(angle * 0.8f + 1.3f)) * size.height
        val scale = 1.035f + 0.115f * sin(angle * 1.3f)
        val r = dim * blob.radius * scale
        drawCircle(
            brush = Brush.radialGradient(
                0f to colors[i].copy(alpha = strength),
                0.55f to colors[i].copy(alpha = strength * 0.35f),
                1f to Color.Transparent,
                center = Offset(cx, cy),
                radius = r,
            ),
            radius = r,
            center = Offset(cx, cy),
        )
    }
}

private const val GRAIN_SIZE = 160

/** A tile of film grain: random dots of [ink] at a few alpha levels on a transparent ground. */
private fun grainTile(ink: Color): ImageBitmap {
    val bitmap = ImageBitmap(GRAIN_SIZE, GRAIN_SIZE)
    val canvas = Canvas(bitmap)
    val random = Random(7)
    val paint = Paint().apply { strokeCap = StrokeCap.Square; strokeWidth = 1f }
    listOf(0.25f, 0.5f, 0.8f, 1f).forEach { alpha ->
        paint.color = ink.copy(alpha = alpha)
        val points = List(GRAIN_SIZE * GRAIN_SIZE / 7) {
            Offset(random.nextInt(GRAIN_SIZE) + 0.5f, random.nextInt(GRAIN_SIZE) + 0.5f)
        }
        canvas.drawPoints(PointMode.Points, points, paint)
    }
    return bitmap
}

private fun DrawScope.drawGrain(flux: FluxColors, brush: ShaderBrush, time: Float) {
    // Film-like jitter: six steps a second, each a different tile offset.
    val step = (time * 6f).toInt()
    val dx = ((step * 37) % GRAIN_SIZE).toFloat()
    val dy = ((step * 61) % GRAIN_SIZE).toFloat()
    translate(-dx, -dy) {
        drawRect(
            brush = brush,
            topLeft = Offset.Zero,
            size = androidx.compose.ui.geometry.Size(size.width + dx, size.height + dy),
            alpha = if (flux.dark) 0.07f else 0.11f,
            blendMode = if (flux.dark) BlendMode.Screen else BlendMode.Multiply,
        )
    }
}

/**
 * The Flux window ground: [FluxColors]-tinted background, the drifting aurora and film grain.
 * Use it wherever a screen used to paint a flat `background` colour.
 */
@Composable
fun Modifier.fluxBackground(grain: Boolean = true): Modifier {
    val flux = AppTheme.flux
    val background = AppTheme.colors.background
    val time = LocalFluxAmbient.current
    val grainBrush = remember(flux.grainBlend) {
        val tile = grainTile(flux.grainBlend)
        ShaderBrush(ImageShader(tile, TileMode.Repeated, TileMode.Repeated))
    }
    return this.drawBehind {
        drawRect(background)
        val t = time.value
        drawAurora(flux, t)
        if (grain) drawGrain(flux, grainBrush, t)
    }
}

/**
 * Fluted glass panel ground: the translucent `glass` fill, vertical flutes every 6dp (a light
 * rib and a darker groove) fading toward the bottom, and a `highlight` rim on top, clipped to
 * [shape]. The component on top must leave its own background transparent.
 */
@Composable
fun Modifier.fluxGlass(shape: Shape): Modifier {
    val flux = AppTheme.flux
    val groove = AppTheme.colors.background.copy(alpha = 0.22f)
    return this
        .clip(shape)
        .drawBehind {
            drawRect(flux.glass)
            val pitch = 6.dp.toPx()
            val ribs = ShaderBrush(
                LinearGradientShader(
                    from = Offset.Zero,
                    to = Offset(pitch, 0f),
                    colors = listOf(flux.rib, Color.Transparent, groove, Color.Transparent),
                    colorStops = listOf(0f, 0.2f, 0.55f, 0.9f),
                    tileMode = TileMode.Repeated,
                )
            )
            drawRect(brush = ribs)
            // Fade the flutes out toward the bottom by washing it back with the glass fill.
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Transparent,
                    1f to flux.glass,
                )
            )
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.White.copy(alpha = if (flux.dark) 0.10f else 0.30f),
                    0.04f to Color.Transparent,
                )
            )
        }
}

/** The lowercase spotube wordmark, filled with the flux gradient. */
@Composable
fun fluxWordmarkStyle(base: TextStyle = AppTheme.typography.wordmark): TextStyle =
    base.copy(brush = AppTheme.fluxBrush)
