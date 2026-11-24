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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontVariation
import org.jetbrains.compose.resources.Font
import org.spotube.spotube.modules.settings.Theme
import org.spotube.spotube.modules.settings.UserSettings
import org.spotube.millennium.composables.MillenniumTooltipHost
import org.spotube.millennium.theme.MillenniumColors
import org.spotube.millennium.theme.MillenniumPalette
import org.spotube.millennium.theme.resolve
import spotube.composeapp.generated.resources.Res
import spotube.composeapp.generated.resources.instrument_sans_variable
import spotube.composeapp.generated.resources.jetbrains_mono_variable
import spotube.composeapp.generated.resources.unbounded_variable
import org.spotube.millennium.theme.MillenniumTheme
import org.spotube.millennium.theme.MillenniumThemeConfig

/**
 * Semantic color roles used across the app, resolved from the active Millennium palette.
 *
 * This is not a second palette: every role is derived from [MillenniumColors], so the
 * whole app follows the selected Millennium accent family and light/dark appearance.
 */
@Immutable
data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val outline: Color,
    val outlineVariant: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val scrim: Color
)

/** Maps Millennium's semantic palette onto the app's role names. */
fun appColorsFrom(m: MillenniumColors): AppColors = AppColors(
    primary = m.accent,
    onPrimary = m.onAccent,
    primaryContainer = m.raised,
    onPrimaryContainer = m.text,
    secondary = m.accent,
    onSecondary = m.onAccent,
    secondaryContainer = m.raised,
    onSecondaryContainer = m.text,
    tertiaryContainer = m.raised,
    onTertiaryContainer = m.text,
    background = m.background,
    onBackground = m.text,
    surface = m.surface,
    onSurface = m.text,
    surfaceVariant = m.inset,
    onSurfaceVariant = m.muted,
    surfaceContainer = m.surface,
    surfaceContainerHigh = m.raised,
    surfaceContainerHighest = m.raised,
    inverseSurface = m.text,
    inverseOnSurface = m.background,
    outline = m.border,
    outlineVariant = m.border,
    error = m.danger,
    onError = m.background,
    errorContainer = m.danger.copy(alpha = 0.16f),
    onErrorContainer = m.danger,
    scrim = m.scrim
)

/** Named text styles used by the app, independent of Material typography. */
@Immutable
data class AppTypography(
    val displaySmall: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
    /** Timestamps, durations, track numbers (mono, tabular figures). */
    val time: TextStyle = labelMedium,
    /** Tiny uppercase status labels (LIVE, 256 KBPS). */
    val signal: TextStyle = labelSmall,
    /** The lowercase spotube wordmark. */
    val wordmark: TextStyle = headlineLarge,
)

private fun style(
    family: FontFamily,
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    tracking: Float = 0f,
): TextStyle = TextStyle(
    fontFamily = family,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = tracking.sp,
)

/** Spotube Flux type scale: Unbounded for display, Instrument Sans for UI, JetBrains Mono for signal. */
fun fluxTypography(display: FontFamily, sans: FontFamily, mono: FontFamily) = AppTypography(
    displaySmall = style(display, 44, 46, FontWeight.Bold, -1.3f),
    headlineLarge = style(display, 32, 36, FontWeight.Bold, -0.8f),
    headlineMedium = style(display, 26, 30, FontWeight.SemiBold, -0.5f),
    headlineSmall = style(display, 20, 26, FontWeight.SemiBold, -0.2f),
    titleLarge = style(sans, 20, 26, FontWeight.SemiBold),
    titleMedium = style(sans, 16, 22, FontWeight.SemiBold),
    titleSmall = style(sans, 14, 20, FontWeight.SemiBold),
    bodyLarge = style(sans, 16, 24),
    bodyMedium = style(sans, 14, 20),
    bodySmall = style(sans, 12, 16),
    labelLarge = style(sans, 14, 20, FontWeight.SemiBold),
    labelMedium = style(sans, 12, 16, FontWeight.SemiBold),
    labelSmall = style(sans, 11, 14, FontWeight.Bold, 1.2f),
    time = style(mono, 12, 16, FontWeight.Medium).copy(fontFeatureSettings = "tnum"),
    signal = style(mono, 10, 12, FontWeight.Medium, 1.6f),
    wordmark = style(display, 28, 32, FontWeight.ExtraBold, -1.4f),
)

val AppDefaultTypography = fluxTypography(FontFamily.SansSerif, FontFamily.SansSerif, FontFamily.Monospace)

@OptIn(ExperimentalTextApi::class)
@Composable
private fun rememberFluxTypography(): AppTypography {
    val displayWeights = listOf(FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold)
    val sansWeights = listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold)
    val monoWeights = listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.Bold)
    val display = FontFamily(displayWeights.map {
        Font(Res.font.unbounded_variable, weight = it,
            variationSettings = FontVariation.Settings(FontVariation.weight(it.weight)))
    })
    val sans = FontFamily(sansWeights.map {
        Font(Res.font.instrument_sans_variable, weight = it,
            variationSettings = FontVariation.Settings(FontVariation.weight(it.weight)))
    })
    val mono = FontFamily(monoWeights.map {
        Font(Res.font.jetbrains_mono_variable, weight = it,
            variationSettings = FontVariation.Settings(FontVariation.weight(it.weight)))
    })
    return remember(display, sans, mono) { fluxTypography(display, sans, mono) }
}

/** Corner shapes used by the app. */
@Immutable
data class AppShapes(
    val small: Shape,
    val medium: Shape,
    val large: Shape
)

val AppDefaultShapes = AppShapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(28.dp)
)

val LocalAppColors =
    staticCompositionLocalOf { appColorsFrom(FluxColorSchemes.dark) }
val LocalAppTypography = staticCompositionLocalOf { AppDefaultTypography }
val LocalAppShapes = staticCompositionLocalOf { AppDefaultShapes }
val LocalFluxColors = staticCompositionLocalOf {
    fluxColorsFor(MillenniumPalette.Blue, true, FluxColorSchemes.dark)
}

/** Reads the active app theme tokens, all backed by Millennium. */
object AppTheme {
    val colors: AppColors @Composable get() = LocalAppColors.current
    val typography: AppTypography @Composable get() = LocalAppTypography.current
    val shapes: AppShapes @Composable get() = LocalAppShapes.current

    /** Flux-only roles: aurora colours, glass, grain. */
    val flux: FluxColors @Composable get() = LocalFluxColors.current

    /** The flux fill (accentTop to accentBottom at 120°): the only loud fill of the app. */
    val fluxBrush: Brush
        @Composable get() {
            val flux = LocalFluxColors.current
            return remember(flux) { FluxGradientBrush(flux.accentTop, flux.accentBottom) }
        }
}

/**
 * The app's single source of truth for colour.
 *
 * Installs [MillenniumTheme] with the Spotube Flux palettes ([FluxColorSchemes]) and component
 * themes ([fluxThemeComponents]) for the user's selected [UserSettings.palette] and light/dark
 * [UserSettings.theme], then derives the app's [AppColors], [AppTypography] and [AppShapes].
 */
@Composable
fun SpotubeTheme(
    settings: UserSettings,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (settings.theme) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.SYSTEM -> isSystemDarkTheme()
    }
    val palette: MillenniumPalette = settings.palette
    val typography = rememberFluxTypography()

    val lightTheme = remember(palette, typography) {
        fluxThemeComponents(FluxColorSchemes.resolve(palette, dark = false), typography)
    }
    val darkTheme = remember(palette, typography) {
        fluxThemeComponents(FluxColorSchemes.resolve(palette, dark = true), typography)
    }

    MillenniumTheme(
        dark = isDarkTheme,
        config = MillenniumThemeConfig(
            colorSchemes = FluxColorSchemes,
            palette = palette,
            dark = darkTheme,
            light = lightTheme,
        )
    ) {
        val millenniumColors: MillenniumColors = MillenniumTheme.colors
        val colors = appColorsFrom(millenniumColors)
        val flux = remember(palette, isDarkTheme, millenniumColors) {
            fluxColorsFor(palette, isDarkTheme, millenniumColors)
        }
        MillenniumTooltipHost {
            CompositionLocalProvider(
                LocalAppColors provides colors,
                LocalAppTypography provides typography,
                LocalAppShapes provides AppDefaultShapes,
                LocalFluxColors provides flux,
            ) {
                ProvideFluxAmbient(content)
            }
        }
    }
}

/** Whether the active Millennium appearance is light. */
@Composable
fun isLightAppearance(): Boolean = MillenniumTheme.colors.background.luminance() > 0.5f
