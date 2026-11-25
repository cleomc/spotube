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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas as GraphicsCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

private const val TAU = (2 * PI).toFloat()

/**
 * One ambient clock for the whole app, in seconds (0 to [AMBIENT_LOOP_SECONDS]), so every screen's
 * aurora sits at the same phase and screen transitions do not jump. Read it only in draw scopes.
 */
private val LocalFluxAmbient = staticCompositionLocalOf<State<Float>> { mutableFloatStateOf(0f) }

/** Playback energy, 0 (paused) to 1 (playing), eased over the `flow` duration. Draw scopes only. */
private val LocalFluxEnergy = staticCompositionLocalOf<State<Float>> { mutableFloatStateOf(0f) }

/**
 * The content layer glass panels blur (bottom bar, floating player). Null where nothing is
 * registered as a haze source: glass then falls back to a denser fill.
 */
val LocalFluxHaze = staticCompositionLocalOf<HazeState?> { null }

/** Long enough that the loop restart is never seen; every motion uses whole cycles of it. */
private const val AMBIENT_LOOP_SECONDS = 26f * 32f * 38f / 16f * 3f

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

/** Lets the aurora, ribbons, halos and equalizers react to playback. */
@Composable
fun ProvideFluxPlayback(playing: Boolean, content: @Composable () -> Unit) {
    val energy = animateFloatAsState(
        targetValue = if (playing) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "flux-energy",
    )
    CompositionLocalProvider(LocalFluxEnergy provides energy, content = content)
}

// ---------------------------------------------------------------------------------------------
// Colour helpers
// ---------------------------------------------------------------------------------------------

private fun cyclic(colors: List<Color>, x: Float): Color {
    val f = (x - floor(x)) * colors.size
    val i = f.toInt() % colors.size
    return lerp(colors[i], colors[(i + 1) % colors.size], f - floor(f))
}

/** A sweep gradient through [colors] that loops seamlessly; shift [phase] (0..1) to spin it. */
internal fun flowSweep(center: Offset, phase: Float, colors: List<Color>): Brush {
    val n = 12
    val stops = Array(n + 1) { i ->
        val t = i / n.toFloat()
        t to cyclic(colors, t + phase)
    }
    return Brush.sweepGradient(*stops, center = center)
}

private fun FluxColors.flow() = listOf(flowA, flowC, flowB, accentTop)

// ---------------------------------------------------------------------------------------------
// Background: aurora, neon liquid ribbons, stars, retro grid, scanlines, grain
// ---------------------------------------------------------------------------------------------

private class Blob(
    val x: Float, val y: Float, val radius: Float,
    val period: Float, val phase: Float,
)

private val Blobs = listOf(
    Blob(0.15f, 0.10f, 0.72f, 26f, 0.0f),
    Blob(0.90f, 0.34f, 0.64f, 32f, 1.7f),
    Blob(0.40f, 0.92f, 0.70f, 38f, 3.1f),
)

/** 0..1 kick at 120 bpm, sharp attack and soft decay, scaled by playback energy. */
private fun beat(time: Float, energy: Float): Float {
    val phase = (time * 2f) - floor(time * 2f)
    return energy * (1f - phase).pow(3)
}

private fun DrawScope.drawAurora(flux: FluxColors, time: Float, energy: Float) {
    val colors = listOf(flux.flowA, flux.flowB, flux.flowC)
    val strength = (if (flux.dark) 0.62f else 0.38f) * (0.62f + 0.38f * energy)
    val kick = beat(time, energy)
    val dim = max(size.width, size.height)
    Blobs.forEachIndexed { i, blob ->
        val angle = (time / blob.period) * TAU + blob.phase
        val cx = (blob.x + 0.12f * sin(angle)) * size.width
        val cy = (blob.y + 0.10f * sin(angle * 0.8f + 1.3f)) * size.height
        val scale = (1.035f + 0.115f * sin(angle * 1.3f)) * (1f + 0.06f * kick)
        val r = dim * blob.radius * scale
        drawCircle(
            brush = Brush.radialGradient(
                0f to colors[i].copy(alpha = strength),
                0.5f to colors[i].copy(alpha = strength * 0.38f),
                1f to Color.Transparent,
                center = Offset(cx, cy),
                radius = r,
            ),
            radius = r,
            center = Offset(cx, cy),
        )
    }
}

/** Two glowing neon-liquid ribbons that flow across the window (the "neon liquid" moodboard). */
private fun DrawScope.drawRibbons(flux: FluxColors, time: Float, energy: Float) {
    val brush = Brush.linearGradient(
        colors = listOf(flux.flowA, flux.flowC, flux.flowB, flux.flowC, flux.flowA),
        start = Offset.Zero,
        end = Offset(size.width, size.height),
    )
    val base = if (flux.dark) 1f else 0.55f
    val steps = 56
    repeat(2) { r ->
        val path = Path()
        val baseY = size.height * (0.24f + 0.44f * r)
        val amp = size.height * (0.05f + 0.015f * energy)
        val t = time * (0.35f + 0.1f * r)
        for (i in 0..steps) {
            val u = i / steps.toFloat()
            val x = size.width * (-0.05f + 1.1f * u)
            val y = baseY +
                amp * sin(u * 5.2f + t + r * 2.1f) +
                amp * 0.55f * sin(u * 11.3f - t * 1.6f + r) +
                amp * 0.25f * sin(u * 23f + t * 2.4f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        val a = base * (0.55f + 0.45f * energy) * (if (r == 0) 1f else 0.7f)
        drawPath(path, brush, alpha = 0.05f * a, style = Stroke(34.dp.toPx(), cap = StrokeCap.Round))
        drawPath(path, brush, alpha = 0.10f * a, style = Stroke(14.dp.toPx(), cap = StrokeCap.Round))
        drawPath(path, brush, alpha = 0.22f * a, style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
        drawPath(
            path, Color.White, alpha = (if (flux.dark) 0.18f else 0.30f) * a,
            style = Stroke(1.2.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

private class Star(val x: Float, val y: Float, val r: Float, val freq: Float, val phase: Float)

private val Stars: List<Star> = Random(11).let { rnd ->
    List(70) {
        Star(
            x = rnd.nextFloat(),
            y = rnd.nextFloat() * 0.72f,
            r = 0.5f + rnd.nextFloat().pow(3) * 1.6f,
            freq = 0.4f + rnd.nextFloat() * 1.4f,
            phase = rnd.nextFloat() * TAU,
        )
    }
}

/** Twinkling motes; the brightest throw a four-point sparkle. */
private fun DrawScope.drawStars(flux: FluxColors, time: Float) {
    val ink = if (flux.dark) Color.White else flux.flowC
    val k = if (flux.dark) 1f else 0.5f
    val unit = 1.dp.toPx()
    Stars.forEach { s ->
        val tw = (0.5f + 0.5f * sin(time * s.freq + s.phase)).pow(4)
        val a = (0.12f + 0.78f * tw) * k
        val c = Offset(s.x * size.width, s.y * size.height)
        drawCircle(ink, radius = s.r * unit, center = c, alpha = a)
        if (s.r > 1.4f && tw > 0.35f) {
            val len = s.r * unit * 5f * tw
            val sw = 0.8f * unit
            drawLine(ink, c - Offset(len, 0f), c + Offset(len, 0f), sw, StrokeCap.Round, alpha = a * 0.7f)
            drawLine(ink, c - Offset(0f, len), c + Offset(0f, len), sw, StrokeCap.Round, alpha = a * 0.7f)
            drawCircle(flux.flowA, radius = s.r * unit * 4f, center = c, alpha = a * 0.12f)
        }
    }
}

/** A synthwave perspective grid rolling toward the viewer at the bottom of the window. */
private fun DrawScope.drawRetroGrid(flux: FluxColors, time: Float, energy: Float) {
    val horizon = size.height * 0.68f
    val depth = size.height - horizon
    val ink = flux.flowC
    val base = (if (flux.dark) 0.16f else 0.12f) * (0.7f + 0.3f * energy)
    val stroke = 1.dp.toPx()
    val cx = size.width / 2f
    for (i in -14..14) {
        val xb = cx + i * size.width * 0.16f
        val xt = cx + i * size.width * 0.012f
        drawLine(
            brush = Brush.verticalGradient(
                0f to Color.Transparent, 1f to ink.copy(alpha = base),
                startY = horizon, endY = size.height,
            ),
            start = Offset(xt, horizon), end = Offset(xb, size.height), strokeWidth = stroke,
        )
    }
    val scroll = time * 0.22f
    for (j in 0 until 12) {
        val t = (j + scroll - floor(scroll)) / 12f
        val y = horizon + depth * t.pow(2.2f)
        drawLine(ink, Offset(0f, y), Offset(size.width, y), stroke, alpha = base * t)
    }
}

private const val GRAIN_SIZE = 160

/** A tile of film grain: random dots of [ink] at a few alpha levels on a transparent ground. */
private fun grainTile(ink: Color): ImageBitmap {
    val bitmap = ImageBitmap(GRAIN_SIZE, GRAIN_SIZE)
    val canvas = GraphicsCanvas(bitmap)
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
            size = Size(size.width + dx, size.height + dy),
            alpha = if (flux.dark) 0.07f else 0.11f,
            blendMode = if (flux.dark) BlendMode.Screen else BlendMode.Multiply,
        )
    }
}

/**
 * The Flux window ground: background colour, the drifting aurora (pulsing on the beat while
 * playing), neon-liquid ribbons, twinkling stars, a retro perspective grid, CRT scanlines and film
 * grain. Use it wherever a screen used to paint a flat `background` colour.
 */
@Composable
fun Modifier.fluxBackground(grain: Boolean = true, decor: Boolean = true): Modifier {
    val flux = AppTheme.flux
    val background = AppTheme.colors.background
    val time = LocalFluxAmbient.current
    val energy = LocalFluxEnergy.current
    val grainBrush = remember(flux.grainBlend) {
        val tile = grainTile(flux.grainBlend)
        ShaderBrush(ImageShader(tile, TileMode.Repeated, TileMode.Repeated))
    }
    return this.drawBehind {
        drawRect(background)
        val t = time.value
        val e = energy.value
        drawAurora(flux, t, e)
        if (decor) {
            drawRetroGrid(flux, t, e)
            drawRibbons(flux, t, e)
            drawStars(flux, t)
            val pitch = 3.dp.toPx()
            drawRect(
                brush = ShaderBrush(
                    LinearGradientShader(
                        from = Offset.Zero, to = Offset(0f, pitch),
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = if (flux.dark) 0.16f else 0.04f)),
                        tileMode = TileMode.Repeated,
                    )
                )
            )
        }
        if (grain) drawGrain(flux, grainBrush, t)
    }
}

// ---------------------------------------------------------------------------------------------
// Glass, rims and neon borders
// ---------------------------------------------------------------------------------------------

/**
 * A spinning holographic border (the flow colours chasing each other around [shape]) with a soft
 * neon bloom. Apply it outside any `clip` so the bloom is not cut off.
 */
@Composable
fun Modifier.fluxNeonBorder(
    shape: Shape,
    width: Dp = 1.5.dp,
    intensity: Float = 1f,
    periodSeconds: Float = 6f,
    behind: Boolean = false,
): Modifier {
    val flux = AppTheme.flux
    val time = LocalFluxAmbient.current
    val colors = remember(flux) { flux.flow() }
    if (intensity <= 0f) return this
    fun DrawScope.ring() {
        val phase = time.value / periodSeconds
        val outline = shape.createOutline(size, layoutDirection, this)
        val brush = flowSweep(center, phase - floor(phase), colors)
        val w = width.toPx()
        drawOutline(outline, brush, alpha = 0.10f * intensity, style = Stroke(w * 7f))
        drawOutline(outline, brush, alpha = 0.22f * intensity, style = Stroke(w * 3f))
        drawOutline(outline, brush, alpha = intensity, style = Stroke(w))
    }
    return if (behind) drawBehind { ring() } else drawWithContent { drawContent(); ring() }
}

/** Spinning conic halo of the flow colours around playing artwork (cards, player). */
@Composable
fun Modifier.fluxHalo(active: Boolean, shape: Shape, width: Dp = 2.5.dp): Modifier {
    val a by animateFloatAsState(if (active) 1f else 0f, tween(600), label = "flux-halo")
    return fluxNeonBorder(shape, width, intensity = a, periodSeconds = 4f, behind = true)
}

/** Glass rim: a specular top edge fading down to a faint warm bottom edge. */
@Composable
fun Modifier.fluxRim(shape: Shape, strength: Float = 1f): Modifier {
    val flux = AppTheme.flux
    return drawWithContent {
        drawContent()
        val outline = shape.createOutline(size, layoutDirection, this)
        drawOutline(
            outline,
            Brush.verticalGradient(
                0f to Color.White.copy(alpha = (if (flux.dark) 0.30f else 0.75f) * strength),
                0.45f to Color.White.copy(alpha = (if (flux.dark) 0.06f else 0.25f) * strength),
                1f to flux.flowB.copy(alpha = 0.22f * strength),
            ),
            style = Stroke(2.dp.toPx()), // half of it is clipped away by the panel
        )
    }
}

/**
 * Fluted glass panel: blurs what is behind it (when a [LocalFluxHaze] source exists), tints it with
 * `glass`, adds 6dp flutes, a diagonal sheen and a specular rim, clipped to [shape]. [neon] adds the
 * spinning holographic border. The component on top must leave its own background transparent.
 */
@Composable
fun Modifier.fluxGlass(shape: Shape, neon: Float = 0f): Modifier {
    val flux = AppTheme.flux
    val haze = LocalFluxHaze.current
    val groove = AppTheme.colors.background.copy(alpha = 0.22f)
    val fill = if (haze != null) flux.glass.copy(alpha = flux.glass.alpha * 0.55f)
    else flux.glass.copy(alpha = max(flux.glass.alpha, if (flux.dark) 0.84f else 0.9f))
    val frosted = if (haze != null) Modifier.hazeEffect(state = haze) {
        blurEffect {
            blurRadius = 28.dp
            colorEffects = listOf(HazeColorEffect.tint(flux.glass))
        }
    } else Modifier
    return this
        .fluxNeonBorder(shape, intensity = neon)
        .clip(shape)
        .then(frosted)
        .drawBehind {
            drawRect(fill)
            val pitch = 6.dp.toPx()
            drawRect(
                brush = ShaderBrush(
                    LinearGradientShader(
                        from = Offset.Zero,
                        to = Offset(pitch, 0f),
                        colors = listOf(flux.rib, Color.Transparent, groove, Color.Transparent),
                        colorStops = listOf(0f, 0.2f, 0.55f, 0.9f),
                        tileMode = TileMode.Repeated,
                    )
                )
            )
            drawRect(brush = Brush.verticalGradient(0f to Color.Transparent, 1f to fill))
            drawRect(
                brush = Brush.linearGradient(
                    0f to Color.White.copy(alpha = if (flux.dark) 0.10f else 0.35f),
                    0.4f to Color.Transparent,
                    start = Offset.Zero,
                    end = Offset(size.width * 0.6f, size.height * 1.4f),
                )
            )
        }
        .fluxRim(shape)
}

/** Glass card ground for media tiles and content cards, lighter than [fluxGlass] (no flutes). */
@Composable
fun Modifier.fluxCard(shape: Shape, highlight: Float = 0f): Modifier {
    val flux = AppTheme.flux
    val colors = AppTheme.colors
    return this
        .clip(shape)
        .drawBehind {
            drawRect(
                Brush.verticalGradient(
                    0f to colors.surfaceContainerHigh.copy(alpha = 0.62f + 0.2f * highlight),
                    1f to colors.surfaceContainer.copy(alpha = 0.42f + 0.2f * highlight),
                )
            )
            drawRect(
                brush = Brush.linearGradient(
                    0f to Color.White.copy(alpha = if (flux.dark) 0.07f else 0.30f),
                    0.35f to Color.Transparent,
                    start = Offset.Zero,
                    end = Offset(size.width * 0.5f, size.height * 1.5f),
                )
            )
        }
        .fluxRim(shape, strength = 0.7f + 0.3f * highlight)
}

/** Playing row marker: a 3dp glowing flux bar on the left edge (the list tile theme adds the wash). */
@Composable
fun Modifier.fluxPlayingBar(active: Boolean): Modifier {
    val flux = AppTheme.flux
    val brush = AppTheme.fluxBrush
    val a by animateFloatAsState(if (active) 1f else 0f, tween(280), label = "flux-wash")
    return drawBehind {
        if (a <= 0.01f) return@drawBehind
        val w = 3.dp.toPx()
        val inset = size.height * 0.2f
        drawRoundRect(
            brush = brush, topLeft = Offset(0f, inset), size = Size(w, size.height - 2 * inset),
            cornerRadius = CornerRadius(w, w), alpha = a,
        )
        drawRoundRect(
            color = flux.accentTop, topLeft = Offset(0f, inset), size = Size(w * 4f, size.height - 2 * inset),
            cornerRadius = CornerRadius(w * 2, w * 2), alpha = 0.25f * a,
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Motion helpers
// ---------------------------------------------------------------------------------------------

/**
 * Squishy press feedback: scales down in 90 ms on touch and springs back with overshoot on
 * release. Observes the pointer without consuming it, so the wrapped control still gets the click.
 */
@Composable
fun Modifier.fluxPress(pressedScale: Float = 0.95f): Modifier {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = if (pressed) tween(90) else spring(dampingRatio = 0.45f, stiffness = 380f),
        label = "flux-press",
    )
    return this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                pressed = true
                val slop = viewConfiguration.touchSlop
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break
                    if ((change.position - down.position).getDistance() > slop) break
                }
                pressed = false
            }
        }
}

/**
 * Screen-enter motion: fades in and rises 12dp over 600 ms, staggered 30 ms per [index] (capped at
 * 8). Runs once per item, also inside lazy lists (the flag is saved with the item).
 */
@Composable
fun Modifier.fluxEnter(index: Int = 0): Modifier {
    var entered by rememberSaveable { mutableStateOf(false) }
    val progress = remember { Animatable(if (entered) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!entered) {
            delay(30L * index.coerceIn(0, 8))
            progress.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
            entered = true
        }
    }
    return graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 12.dp.toPx()
        val s = 0.97f + 0.03f * progress.value
        scaleX = s; scaleY = s
    }
}

// ---------------------------------------------------------------------------------------------
// Small animated pieces
// ---------------------------------------------------------------------------------------------

/** Four flux bars dancing while [playing]; they settle low and still when paused. */
@Composable
fun FluxEqualizer(playing: Boolean, modifier: Modifier = Modifier, brush: Brush? = null) {
    val time = LocalFluxAmbient.current
    val fill = brush ?: AppTheme.fluxBrush
    val energy by animateFloatAsState(if (playing) 1f else 0f, tween(280), label = "flux-eq")
    Canvas(modifier.size(16.dp, 14.dp)) {
        val periods = floatArrayOf(0.7f, 1.1f, 0.85f, 0.95f)
        val bw = 3.dp.toPx()
        val gap = (size.width - 4 * bw) / 3f
        for (i in 0 until 4) {
            val p = 0.5f + 0.5f * sin(time.value * TAU / periods[i] + i * 1.3f)
            val h = size.height * (0.22f + 0.78f * p * energy)
            drawRoundRect(
                brush = fill,
                topLeft = Offset(i * (bw + gap), size.height - h),
                size = Size(bw, h),
                cornerRadius = CornerRadius(bw / 2, bw / 2),
            )
        }
    }
}

// Play triangle and pause bars as two quads each, with matching corners so they morph.
private val PlayLeft = floatArrayOf(0.14f, 0.02f, 0.56f, 0.27f, 0.56f, 0.73f, 0.14f, 0.98f)
private val PlayRight = floatArrayOf(0.56f, 0.27f, 0.98f, 0.50f, 0.98f, 0.50f, 0.56f, 0.73f)
private val PauseLeft = floatArrayOf(0.14f, 0.04f, 0.42f, 0.04f, 0.42f, 0.96f, 0.14f, 0.96f)
private val PauseRight = floatArrayOf(0.58f, 0.04f, 0.86f, 0.04f, 0.86f, 0.96f, 0.58f, 0.96f)

/**
 * Play/pause as one shape morphing between a triangle and two bars (`smooth` with overshoot),
 * turning a quarter as it goes.
 */
@Composable
fun FluxPlayPauseIcon(
    playing: Boolean,
    contentDescription: String?,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val t by animateFloatAsState(
        targetValue = if (playing) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "flux-play-morph",
    )
    Canvas(
        modifier
            .size(22.dp)
            .then(
                if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription }
                else Modifier
            )
    ) {
        val side = size.minDimension
        val ox = (size.width - side) / 2f
        val oy = (size.height - side) / 2f
        fun quad(a: FloatArray, b: FloatArray): Path = Path().apply {
            for (k in 0 until 4) {
                val x = ox + side * (a[2 * k] + (b[2 * k] - a[2 * k]) * t)
                val y = oy + side * (a[2 * k + 1] + (b[2 * k + 1] - a[2 * k + 1]) * t)
                if (k == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        val round = Stroke(width = side * 0.09f, join = StrokeJoin.Round)
        listOf(quad(PlayLeft, PauseLeft), quad(PlayRight, PauseRight)).forEach { p ->
            drawPath(p, color)
            drawPath(p, color, style = round)
        }
    }
}

/** The lowercase spotube wordmark, filled with the flux gradient. */
@Composable
fun fluxWordmarkStyle(base: TextStyle = AppTheme.typography.wordmark): TextStyle =
    base.copy(brush = AppTheme.fluxBrush)
