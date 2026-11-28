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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
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
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.graphics.ColorFilter
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import org.spotube.spotube.resources.iconsax.FLUX_ICON_ECHO
import org.spotube.spotube.resources.iconsax.FLUX_ICON_INK
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

/** Playback energy, 0 (paused) to 1 (playing), eased over 1.2 s. Draw scopes only. */
private val LocalFluxEnergy = staticCompositionLocalOf<State<Float>> { mutableFloatStateOf(0f) }

/**
 * The content layer glass panels blur (bottom bar, floating player). Null where nothing is
 * registered as a haze source: glass then falls back to a denser fill.
 */
val LocalFluxHaze = staticCompositionLocalOf<HazeState?> { null }

/** Long enough that the loop restart is never seen; the aurora periods (34, 42, 50 s) divide it. */
private const val AMBIENT_LOOP_SECONDS = 34f * 42f * 50f / 4f

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

/** Lets the aurora, scopes, glows and icon echoes react to playback. */
@Composable
fun ProvideFluxPlayback(playing: Boolean, content: @Composable () -> Unit) {
    val energy = animateFloatAsState(
        targetValue = if (playing) 1f else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "flux-energy",
    )
    CompositionLocalProvider(LocalFluxEnergy provides energy, content = content)
}

/** 0..1 pulse at 120 bpm (sharp attack, soft decay), scaled by playback energy. */
private fun beat(time: Float, energy: Float): Float {
    val phase = (time * 2f) - floor(time * 2f)
    return energy * (1f - phase).pow(3)
}

/** The accent as thin light: accentTop to accentBottom, left to right. */
private fun FluxColors.accentLine(width: Float): Brush =
    Brush.horizontalGradient(listOf(accentTop, accentBottom), startX = 0f, endX = width)

// ---------------------------------------------------------------------------------------------
// Background: aurora and grain
// ---------------------------------------------------------------------------------------------

private class Light(
    val x: Float, val y: Float, val radius: Float,
    val period: Float, val phase: Float,
)

private val Lights = listOf(
    Light(0.18f, 0.08f, 0.66f, 34f, 0.0f),
    Light(0.88f, 0.40f, 0.60f, 42f, 1.7f),
    Light(0.36f, 0.94f, 0.64f, 50f, 3.1f),
)

/**
 * Three very soft lights drifting on 34 to 50 s loops. Nuit is tuned for OLED: the lights stay
 * low (22 % at rest, 34 % playing) and fall to true black well before the edges.
 */
private fun DrawScope.drawAurora(flux: FluxColors, time: Float, energy: Float) {
    val colors = listOf(flux.flowA, flux.flowB, flux.flowC)
    val strength = if (flux.dark) 0.22f + 0.12f * energy else 0.30f + 0.10f * energy
    val kick = beat(time, energy)
    val dim = max(size.width, size.height)
    Lights.forEachIndexed { i, light ->
        // Ease in-out back and forth, like the CSS alternate loop.
        val u = 0.5f - 0.5f * kotlin.math.cos((time / light.period) * PI.toFloat() + light.phase)
        val cx = (light.x + 0.10f * (u - 0.5f) * 2f) * size.width
        val cy = (light.y + 0.08f * sin(u * PI.toFloat() + light.phase)) * size.height
        val scale = (0.96f + 0.08f * u) * (if (i == 0) 1f + 0.035f * kick else 1f)
        val r = dim * light.radius * scale
        drawCircle(
            brush = Brush.radialGradient(
                0f to colors[i].copy(alpha = strength),
                0.45f to colors[i].copy(alpha = strength * 0.32f),
                1f to Color.Transparent,
                center = Offset(cx, cy),
                radius = r,
            ),
            radius = r,
            center = Offset(cx, cy),
            blendMode = if (flux.dark) BlendMode.SrcOver else BlendMode.Multiply,
        )
    }
}

private const val GRAIN_SIZE = 192

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
    // Six steps a second, each a different tile offset.
    val step = (time * 6f).toInt()
    val dx = ((step * 37) % GRAIN_SIZE).toFloat()
    val dy = ((step * 61) % GRAIN_SIZE).toFloat()
    translate(-dx, -dy) {
        drawRect(
            brush = brush,
            topLeft = Offset.Zero,
            size = Size(size.width + dx, size.height + dy),
            // Kept faint on Nuit so OLED blacks stay black.
            alpha = if (flux.dark) 0.035f else 0.09f,
            blendMode = if (flux.dark) BlendMode.Screen else BlendMode.Multiply,
        )
    }
}

@Composable
private fun rememberGrainBrush(ink: Color): ShaderBrush = remember(ink) {
    ShaderBrush(ImageShader(grainTile(ink), TileMode.Repeated, TileMode.Repeated))
}

/**
 * The Flux window ground: the background colour, the aurora (rising and breathing while playing)
 * and film grain. Use it wherever a screen used to paint a flat `background` colour. [decor] keeps
 * the aurora; turn it off for plain panels.
 */
@Composable
fun Modifier.fluxBackground(grain: Boolean = true, decor: Boolean = true): Modifier {
    val flux = AppTheme.flux
    val background = AppTheme.colors.background
    val time = LocalFluxAmbient.current
    val energy = LocalFluxEnergy.current
    val grainBrush = rememberGrainBrush(flux.grainBlend)
    return this.drawBehind {
        drawRect(background)
        val t = time.value
        if (decor) drawAurora(flux, t, energy.value)
        if (grain) drawGrain(flux, grainBrush, t)
    }
}

// ---------------------------------------------------------------------------------------------
// Glass, hairlines and light
// ---------------------------------------------------------------------------------------------

/**
 * A thin accent line around [shape], the accent gradient slowly turning. [intensity] fades it in
 * and out (0 draws nothing). Flux keeps the accent to thin light, so there is no bloom.
 */
@Composable
fun Modifier.fluxNeonBorder(
    shape: Shape,
    width: Dp = 1.dp,
    intensity: Float = 1f,
    periodSeconds: Float = 12f,
    behind: Boolean = false,
): Modifier {
    val flux = AppTheme.flux
    val time = LocalFluxAmbient.current
    if (intensity <= 0f) return this
    fun DrawScope.ring() {
        val phase = time.value / periodSeconds
        val a = (phase - floor(phase)) * TAU
        val dx = kotlin.math.cos(a) * size.width / 2f
        val dy = sin(a) * size.height / 2f
        val brush = Brush.linearGradient(
            listOf(flux.accentTop, flux.accentBottom),
            start = center - Offset(dx, dy),
            end = center + Offset(dx, dy),
        )
        drawOutline(
            shape.createOutline(size, layoutDirection, this), brush,
            alpha = intensity, style = Stroke(width.toPx()),
        )
    }
    return if (behind) drawBehind { ring() } else drawWithContent { drawContent(); ring() }
}

/**
 * `glow-art`: the light a cover casts under itself, in the flow colours, fading in over 600 ms
 * while [active]. Apply it before the cover's `clip`.
 */
@Composable
fun Modifier.fluxHalo(active: Boolean, shape: Shape, width: Dp = 2.5.dp): Modifier {
    val flux = AppTheme.flux
    val energy = LocalFluxEnergy.current
    val time = LocalFluxAmbient.current
    val a by animateFloatAsState(if (active) 1f else 0f, tween(600), label = "flux-glow")
    return drawBehind {
        if (a <= 0.01f) return@drawBehind
        val breathe = 1f + 0.04f * beat(time.value, energy.value)
        val w = size.width
        val h = size.height
        val strength = (if (flux.dark) 0.42f else 0.30f) * a
        fun glow(color: Color, cx: Float, cy: Float, r: Float) = drawCircle(
            brush = Brush.radialGradient(
                0f to color.copy(alpha = strength), 1f to Color.Transparent,
                center = Offset(cx, cy), radius = r,
            ),
            radius = r, center = Offset(cx, cy),
        )
        glow(flux.flowA, w * 0.32f, h * 0.82f, w * 0.62f * breathe)
        glow(flux.flowB, w * 0.70f, h * 0.88f, w * 0.52f * breathe)
    }
}

/** Glass edge: a 1dp hairline in `border` at 60 % with the `lift` highlight along the top. */
@Composable
fun Modifier.fluxRim(shape: Shape, strength: Float = 1f): Modifier {
    val flux = AppTheme.flux
    val hair = AppTheme.colors.outline.copy(alpha = 0.6f * strength)
    return drawWithContent {
        drawContent()
        val outline = shape.createOutline(size, layoutDirection, this)
        // Half of the stroke lies outside the clip, so 2dp shows as a 1dp hairline.
        drawOutline(outline, hair, style = Stroke(2.dp.toPx()))
        drawOutline(
            outline,
            Brush.verticalGradient(
                0f to Color.White.copy(alpha = (if (flux.dark) 0.10f else 0.6f) * strength),
                0.25f to Color.Transparent,
            ),
            style = Stroke(2.dp.toPx()),
        )
    }
}

/**
 * Fine fluted glass: blurs what is behind it (when a [LocalFluxHaze] source exists) and tints it
 * `glass`, then 1dp flutes every 4dp fading downward, a hairline and the top highlight, clipped to
 * [shape]. [neon] adds a thin accent line around it. The component on top must leave its own
 * background transparent.
 */
@Composable
fun Modifier.fluxGlass(shape: Shape, neon: Float = 0f): Modifier {
    val flux = AppTheme.flux
    val haze = LocalFluxHaze.current
    val fill = if (haze != null) flux.glass.copy(alpha = flux.glass.alpha * 0.5f)
    else flux.glass.copy(alpha = max(flux.glass.alpha, if (flux.dark) 0.9f else 0.92f))
    val frosted = if (haze != null) Modifier.hazeEffect(state = haze) {
        blurEffect {
            blurRadius = 28.dp
            colorEffects = listOf(HazeColorEffect.tint(flux.glass))
        }
    } else Modifier
    return this
        .fluxNeonBorder(shape, intensity = neon * 0.6f)
        .clip(shape)
        .then(frosted)
        .drawBehind {
            drawRect(fill)
            val pitch = 4.dp.toPx()
            val rib = 1.dp.toPx() / pitch
            drawRect(
                brush = ShaderBrush(
                    LinearGradientShader(
                        from = Offset.Zero,
                        to = Offset(pitch, 0f),
                        colors = listOf(
                            flux.rib.copy(alpha = flux.rib.alpha * 0.7f), flux.rib.copy(alpha = flux.rib.alpha * 0.7f),
                            Color.Transparent, Color.Transparent,
                        ),
                        colorStops = listOf(0f, rib, rib, 1f),
                        tileMode = TileMode.Repeated,
                    )
                )
            )
            // Flutes fade toward the bottom.
            drawRect(brush = Brush.verticalGradient(0f to Color.Transparent, 1f to fill))
        }
        .fluxRim(shape)
}

/**
 * Card ground for content cards and tiles. Flux cards have no surface: this only adds a faint
 * `raised` wash ([highlight] 0..1, for hover or the playing item) and a hairline.
 */
@Composable
fun Modifier.fluxCard(shape: Shape, highlight: Float = 0f): Modifier {
    val colors = AppTheme.colors
    return this
        .clip(shape)
        .drawBehind {
            drawRect(colors.onSurface.copy(alpha = 0.03f + 0.04f * highlight))
            drawOutline(
                shape.createOutline(size, layoutDirection, this),
                colors.outline.copy(alpha = 0.35f + 0.2f * highlight),
                style = Stroke(2.dp.toPx()),
            )
        }
}

/** A 1dp `hair` rule along the bottom edge: row separators and section ends. */
@Composable
fun Modifier.fluxHairline(): Modifier {
    val hair = AppTheme.colors.outline.copy(alpha = 0.6f)
    return drawBehind {
        val w = 1.dp.toPx()
        drawRect(hair, topLeft = Offset(0f, size.height - w), size = Size(size.width, w))
    }
}

/** Playing row marker: a soft `text` 5 % wash with a 2dp accent tick on the left edge. */
@Composable
fun Modifier.fluxPlayingBar(active: Boolean): Modifier {
    val flux = AppTheme.flux
    val wash = AppTheme.colors.onSurface
    val a by animateFloatAsState(if (active) 1f else 0f, tween(280), label = "flux-playing-row")
    return drawBehind {
        if (a <= 0.01f) return@drawBehind
        drawRect(wash.copy(alpha = 0.05f * a))
        val w = 2.dp.toPx()
        val inset = size.height * 0.28f
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(flux.accentTop, flux.accentBottom), inset, size.height - inset),
            topLeft = Offset(0f, inset), size = Size(w, size.height - 2 * inset),
            cornerRadius = CornerRadius(w, w), alpha = a,
        )
    }
}

/**
 * Viewfinder corners: four 10dp marks 6dp outside the content in `text` at 70 %, settling from
 * 104 % to 100 % as they appear. Apply before any `clip`.
 */
@Composable
fun Modifier.fluxViewfinder(visible: Boolean): Modifier {
    val ink = AppTheme.colors.onSurface
    val a by animateFloatAsState(if (visible) 1f else 0f, tween(280), label = "flux-viewfinder")
    return drawWithContent {
        drawContent()
        if (a <= 0.01f) return@drawWithContent
        val gap = 6.dp.toPx() * (1f + 0.6f * (1f - a))
        val len = 10.dp.toPx()
        val sw = 1.2.dp.toPx()
        val c = ink.copy(alpha = 0.7f * a)
        val l = -gap
        val t = -gap
        val r = size.width + gap
        val b = size.height + gap
        listOf(
            Triple(Offset(l, t), 1f, 1f), Triple(Offset(r, t), -1f, 1f),
            Triple(Offset(l, b), 1f, -1f), Triple(Offset(r, b), -1f, -1f),
        ).forEach { (p, sx, sy) ->
            drawLine(c, p, p + Offset(len * sx, 0f), sw, StrokeCap.Round)
            drawLine(c, p, p + Offset(0f, len * sy), sw, StrokeCap.Round)
        }
    }
}

/**
 * The play disc's progress ring: a 1dp `hair` track and a 1.6dp accent arc [inset] outside the
 * content, sweeping with [progress] (0..1, smoothed over 200 ms). Apply before any `clip`.
 */
@Composable
fun Modifier.fluxProgressRing(progress: Float, inset: Dp = 7.dp): Modifier {
    val flux = AppTheme.flux
    val hair = AppTheme.colors.outline.copy(alpha = 0.6f)
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), tween(200, easing = LinearEasing), label = "flux-ring")
    return drawBehind {
        val o = inset.toPx()
        val topLeft = Offset(-o, -o)
        val s = Size(size.width + 2 * o, size.height + 2 * o)
        drawArc(hair, 0f, 360f, false, topLeft, s, style = Stroke(1.dp.toPx()))
        if (p > 0f) {
            drawArc(
                brush = Brush.sweepGradient(
                    0f to flux.accentTop, 1f to flux.accentBottom, center = center,
                ),
                startAngle = -90f, sweepAngle = 360f * p, useCenter = false,
                topLeft = topLeft, size = s,
                style = Stroke(1.6.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Motion helpers
// ---------------------------------------------------------------------------------------------

/**
 * Squishy press feedback: scales down in 90 ms on touch and springs back on release. Observes the
 * pointer without consuming it, so the wrapped control still gets the click.
 */
@Composable
fun Modifier.fluxPress(pressedScale: Float = 0.97f): Modifier {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = if (pressed) tween(90) else spring(dampingRatio = 0.5f, stiffness = 420f),
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
    }
}

// ---------------------------------------------------------------------------------------------
// Small animated pieces
// ---------------------------------------------------------------------------------------------

/**
 * A live oscilloscope trace: a 1.6dp line in the accent gradient plus a ghost trace at 28 %,
 * windowed by sin(πx) so it rests at both ends. Paused, it flattens over about 400 ms. Redrawn in
 * the draw phase only.
 */
@Composable
fun FluxScope(playing: Boolean, modifier: Modifier = Modifier, ghost: Boolean = true) {
    val flux = AppTheme.flux
    val time = LocalFluxAmbient.current
    val amp by animateFloatAsState(if (playing) 1f else 0f, tween(400), label = "flux-scope")
    Canvas(modifier) {
        val t = time.value
        val k = beat(t, 1f)
        val mid = size.height / 2f
        val steps = (size.width / 1.5.dp.toPx()).toInt().coerceAtLeast(12)
        fun trace(shift: Float, gain: Float): Path = Path().apply {
            for (i in 0..steps) {
                val u = i / steps.toFloat()
                val window = sin(u * PI.toFloat())
                val wave = sin(u * 13f + t * 7f + shift) * 0.6f +
                    sin(u * 29f - t * 11f + shift * 2f) * 0.3f +
                    sin(u * 5f + t * 3f) * 0.25f
                val y = mid - wave * window * mid * 0.9f * amp * gain * (0.75f + 0.25f * k)
                val x = u * size.width
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        val brush = flux.accentLine(size.width)
        val style = Stroke(1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        if (ghost) drawPath(trace(1.9f, 0.7f), brush, alpha = 0.28f, style = Stroke(1.dp.toPx()))
        drawPath(trace(0f, 1f), brush, style = style)
    }
}

/** The small scope shown before a playing title and in place of a playing row's number. */
@Composable
fun FluxEqualizer(playing: Boolean, modifier: Modifier = Modifier, brush: Brush? = null) {
    FluxScope(playing = playing, modifier = modifier.size(22.dp, 14.dp), ghost = false)
}

// Play triangle and pause bars as two quads each, with matching corners so they morph.
private val PlayLeft = floatArrayOf(0.18f, 0.06f, 0.56f, 0.29f, 0.56f, 0.71f, 0.18f, 0.94f)
private val PlayRight = floatArrayOf(0.56f, 0.29f, 0.94f, 0.50f, 0.94f, 0.50f, 0.56f, 0.71f)
private val PauseLeft = floatArrayOf(0.20f, 0.08f, 0.40f, 0.08f, 0.40f, 0.92f, 0.20f, 0.92f)
private val PauseRight = floatArrayOf(0.60f, 0.08f, 0.80f, 0.08f, 0.80f, 0.92f, 0.60f, 0.92f)

/** Play/pause as one glyph morphing between a triangle and two bars, with a slight overshoot. */
@Composable
fun FluxPlayPauseIcon(
    playing: Boolean,
    contentDescription: String?,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val t by animateFloatAsState(
        targetValue = if (playing) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 420f),
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
        val round = Stroke(width = side * 0.08f, join = StrokeJoin.Round)
        listOf(quad(PlayLeft, PauseLeft), quad(PlayRight, PauseRight)).forEach { p ->
            drawPath(p, color)
            drawPath(p, color, style = round)
        }
    }
}

/** A copy of this vector holding only the paths of its top-level group [name], or null. */
private fun ImageVector.layer(name: String): ImageVector? {
    val group = root.firstOrNull { it is VectorGroup && it.name == name } as? VectorGroup ?: return null
    val builder = ImageVector.Builder(
        name = "${this.name}-$name",
        defaultWidth = defaultWidth,
        defaultHeight = defaultHeight,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
    )
    group.forEach { node ->
        if (node is VectorPath) {
            builder.addPath(
                pathData = node.pathData,
                pathFillType = node.pathFillType,
                name = node.name,
                fill = node.fill,
                fillAlpha = if (node.fill != null) 1f else node.fillAlpha,
                stroke = node.stroke,
                strokeAlpha = 1f,
                strokeLineWidth = node.strokeLineWidth,
                strokeLineCap = node.strokeLineCap,
                strokeLineJoin = node.strokeLineJoin,
                strokeLineMiter = node.strokeLineMiter,
            )
        }
    }
    return builder.build()
}

/**
 * A Flux echo icon with a coloured echo: the icon in [tint] over the same strokes in [echoTint],
 * offset 0.9dp right and down, like a print slightly out of register. [lively] makes the echo
 * jump to (1.6, -0.6) dp (hover, selection) and, while music plays, pulse on the beat. Icons
 * without the echo groups (brand logos) draw as plain tinted vectors.
 */
@Composable
fun FluxIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    echoTint: Color = AppTheme.flux.flowB.copy(alpha = 0.7f),
    lively: Boolean = false,
    pulse: Boolean = false,
) {
    val ink = remember(imageVector) { imageVector.layer(FLUX_ICON_INK) }
    val echo = remember(imageVector) { imageVector.layer(FLUX_ICON_ECHO) }
    val inkPainter = rememberVectorPainter(ink ?: imageVector)
    val echoPainter = echo?.let { rememberVectorPainter(it) }
    val shift by animateFloatAsState(
        if (lively) 1f else 0f, spring(dampingRatio = 0.55f, stiffness = 420f), label = "flux-echo",
    )
    val time = LocalFluxAmbient.current
    val energy = LocalFluxEnergy.current
    Box(
        modifier
            .size(size)
            .then(
                if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription }
                else Modifier
            )
            .drawBehind {
                val unit = this.size.width / 24f
                if (echoPainter != null) {
                    val k = if (pulse) beat(time.value, energy.value) else 0f
                    val dx = (0.9f + 0.7f * shift + 0.7f * k) * unit
                    val dy = (0.9f - 1.5f * shift + 0.3f * k) * unit
                    translate(dx - 0.9f * unit, dy - 0.9f * unit) {
                        with(echoPainter) { draw(this@drawBehind.size, colorFilter = ColorFilter.tint(echoTint)) }
                    }
                }
                with(inkPainter) { draw(this@drawBehind.size, colorFilter = ColorFilter.tint(tint)) }
            }
    )
}

/** The wordmark's text style: Instrument Serif italic in `text`. */
@Composable
fun fluxWordmarkStyle(base: TextStyle = AppTheme.typography.wordmark): TextStyle =
    base.copy(color = AppTheme.colors.onSurface)

/** The wordmark: [name] in Instrument Serif italic followed by a small `accentBottom` dot. */
@Composable
fun FluxWordmark(name: String, modifier: Modifier = Modifier, style: TextStyle = fluxWordmarkStyle()) {
    val dot = AppTheme.flux.accentBottom
    val fontSize = style.fontSize
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        BasicText(name, style = style)
        Box(
            Modifier
                .padding(start = 2.dp, bottom = (fontSize.value * 0.2f).dp)
                .size((fontSize.value * 0.2f).coerceIn(4f, 10f).dp)
                .drawBehind { drawCircle(dot) }
        )
    }
}
