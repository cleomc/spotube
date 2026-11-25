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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import org.spotube.millennium.composables.MillenniumBottomNavigationBarTheme
import org.spotube.millennium.composables.MillenniumButtonTheme
import org.spotube.millennium.composables.MillenniumCardTheme
import org.spotube.millennium.composables.MillenniumCheckboxTheme
import org.spotube.millennium.composables.MillenniumDialogTheme
import org.spotube.millennium.composables.MillenniumDropdownMenuTheme
import org.spotube.millennium.composables.MillenniumIconButtonTheme
import org.spotube.millennium.composables.MillenniumListTileTheme
import org.spotube.millennium.composables.MillenniumModalBottomSheetTheme
import org.spotube.millennium.composables.MillenniumProgressTheme
import org.spotube.millennium.composables.MillenniumSegmentedButtonTheme
import org.spotube.millennium.composables.MillenniumSidebarTheme
import org.spotube.millennium.composables.MillenniumSliderTheme
import org.spotube.millennium.composables.MillenniumTabGroupTheme
import org.spotube.millennium.composables.MillenniumTextFieldTheme
import org.spotube.millennium.composables.MillenniumTextTheme
import org.spotube.millennium.composables.MillenniumToggleSwitchTheme
import org.spotube.millennium.composables.MillenniumTopAppBarTheme
import org.spotube.millennium.composables.MillenniumTriStateCheckboxTheme
import org.spotube.millennium.composables.defaultMillenniumBottomNavigationBarTheme
import org.spotube.millennium.composables.defaultMillenniumButtonTheme
import org.spotube.millennium.composables.defaultMillenniumCardTheme
import org.spotube.millennium.composables.defaultMillenniumCheckboxTheme
import org.spotube.millennium.composables.defaultMillenniumDialogTheme
import org.spotube.millennium.composables.defaultMillenniumDropdownMenuTheme
import org.spotube.millennium.composables.defaultMillenniumIconButtonTheme
import org.spotube.millennium.composables.defaultMillenniumListTileTheme
import org.spotube.millennium.composables.defaultMillenniumModalBottomSheetTheme
import org.spotube.millennium.composables.defaultMillenniumProgressTheme
import org.spotube.millennium.composables.defaultMillenniumSegmentedButtonTheme
import org.spotube.millennium.composables.defaultMillenniumSidebarTheme
import org.spotube.millennium.composables.defaultMillenniumSliderTheme
import org.spotube.millennium.composables.defaultMillenniumTabGroupTheme
import org.spotube.millennium.composables.defaultMillenniumTextFieldTheme
import org.spotube.millennium.composables.defaultMillenniumTextTheme
import org.spotube.millennium.composables.defaultMillenniumToggleSwitchTheme
import org.spotube.millennium.composables.defaultMillenniumTopAppBarTheme
import org.spotube.millennium.composables.defaultMillenniumTriStateCheckboxTheme
import org.spotube.millennium.theme.MillenniumColors
import org.spotube.millennium.theme.MillenniumControlState
import org.spotube.millennium.theme.MillenniumControlStateProperty
import org.spotube.millennium.theme.MillenniumThemeComponents
import org.spotube.millennium.theme.defaultMillenniumThemeComponents

/*
 * Millennium component themes restyled for Spotube Flux: no gels, bevels, orbs or inner glows;
 * pills for every button, 12/18/28dp corners elsewhere, and the flux gradient as the only loud
 * fill (primary actions, selection, checked state, played progress).
 */

private val Clear = Color.Transparent
private val ClearBrush: Brush = SolidColor(Color.Transparent)
private val Pill = CircleShape
private val Small = RoundedCornerShape(12.dp)
private val Medium = RoundedCornerShape(18.dp)
private val Large = RoundedCornerShape(28.dp)

/** Maps every state of a control property, telling the mapper which state it is. */
private inline fun <T> MillenniumControlStateProperty<T>.mapStates(
    f: (MillenniumControlState, T) -> T,
): MillenniumControlStateProperty<T> = MillenniumControlStateProperty(
    default = f(MillenniumControlState.Default, default),
    hovered = f(MillenniumControlState.Hovered, hovered),
    focused = f(MillenniumControlState.Focused, focused),
    pressed = f(MillenniumControlState.Pressed, pressed),
    disabled = f(MillenniumControlState.Disabled, disabled),
    selected = f(MillenniumControlState.Selected, selected),
    error = f(MillenniumControlState.Error, error),
)

/**
 * Builds the full component set for one appearance from the resolved [colors] (brushes need
 * concrete colours, so this is rebuilt when the palette or appearance changes).
 */
fun fluxThemeComponents(
    colors: MillenniumColors,
    type: AppTypography,
): MillenniumThemeComponents {
    val flux = fluxBrush(colors)
    val glow = listOf(colors.accentTop.copy(alpha = 0.55f), colors.accentBottom.copy(alpha = 0.45f))
    val raisedFill: Brush = SolidColor(colors.raised.copy(alpha = 0.70f))
    val insetFill: Brush = SolidColor(colors.inset)
    val label = type.labelLarge
    val base = defaultMillenniumThemeComponents()

    val text = defaultMillenniumTextTheme().let { t ->
        MillenniumTextTheme(states = t.states.mapStates { _, p ->
            p.copy(
                body = type.bodyMedium,
                label = type.labelLarge.copy(fontWeight = type.labelMedium.fontWeight),
                button = type.labelLarge,
                title = type.titleLarge,
                headline = type.headlineLarge,
                caption = type.bodySmall,
                avatar = type.labelLarge,
            )
        })
    }

    val buttons = defaultMillenniumButtonTheme().let { t ->
        fun MillenniumControlStateProperty<org.spotube.millennium.composables.MillenniumButtonThemeProperties>.restyle(
            background: Brush?,
            border: Color?,
            shadows: List<Color>?,
            elevation: Boolean,
        ) = mapStates { state, p ->
            p.copy(
                background = background,
                textStyle = label,
                shape = Pill,
                padding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
                contentSpacing = 8.dp,
                borderColor = if (state == MillenniumControlState.Focused) colors.accent else border,
                shadows = shadows,
                shadowElevation = if (!elevation || state == MillenniumControlState.Disabled ||
                    state == MillenniumControlState.Pressed
                ) 0.dp else if (state == MillenniumControlState.Hovered) 14.dp else 10.dp,
                innerGlowColor = Clear,
                bottomRimColor = Clear,
                alpha = if (state == MillenniumControlState.Disabled) 0.4f else 1f,
                motion = p.motion.copy(hoverScale = 1.02f, pressedScale = 0.95f),
            )
        }
        MillenniumButtonTheme(
            primary = t.primary.restyle(flux, Clear, glow, elevation = true),
            secondary = t.secondary.restyle(raisedFill, colors.border, null, elevation = false),
            quiet = t.quiet.restyle(null, null, null, elevation = false),
            danger = t.danger.restyle(SolidColor(colors.danger), Clear, null, elevation = false)
                .mapStates { _, p -> p.copy(foreground = colors.background) },
        )
    }

    val iconButtons = defaultMillenniumIconButtonTheme().let { t ->
        fun MillenniumControlStateProperty<org.spotube.millennium.composables.MillenniumIconButtonThemeProperties>.restyle(
            background: Brush?,
            border: Color?,
            shadows: List<Color>?,
        ) = mapStates { state, p ->
            p.copy(
                background = background,
                shape = Pill,
                borderColor = if (state == MillenniumControlState.Focused) colors.accent else border,
                shadows = shadows,
                shadowElevation = if (shadows != null && state != MillenniumControlState.Pressed &&
                    state != MillenniumControlState.Disabled
                ) 10.dp else 0.dp,
                innerGlowColor = Clear,
                bottomRimColor = Clear,
                orb = false,
                alpha = if (state == MillenniumControlState.Disabled) 0.4f else 1f,
                motion = p.motion.copy(hoverScale = 1.08f, pressedScale = 0.90f),
            )
        }
        MillenniumIconButtonTheme(
            primary = t.primary.restyle(flux, Clear, glow),
            secondary = t.secondary.restyle(raisedFill, colors.border, null),
            quiet = t.quiet.restyle(null, null, null),
            danger = t.danger.restyle(SolidColor(colors.danger), Clear, null)
                .mapStates { _, p -> p.copy(foreground = colors.background) },
        )
    }

    val toggle = defaultMillenniumToggleSwitchTheme().let { t ->
        MillenniumToggleSwitchTheme(states = t.states.mapStates { _, p ->
            p.copy(
                trackBackground = insetFill,
                selectedTrackBackground = flux,
                trackShape = Pill,
                trackGlowColor = Clear,
                thumbBackground = SolidColor(Color.White),
                thumbShape = Pill,
                thumbGlowColor = Clear,
                thumbBorderColor = Clear,
                labelStyle = type.bodyMedium,
            )
        })
    }

    val checkbox = defaultMillenniumCheckboxTheme().let { t ->
        MillenniumCheckboxTheme(states = t.states.mapStates { _, p ->
            p.copy(
                faceBackground = insetFill,
                selectedFaceBackground = flux,
                selectedFaceForeground = colors.onAccent,
                faceShape = RoundedCornerShape(8.dp),
                faceGlowColor = Clear,
                labelStyle = type.bodyMedium,
            )
        })
    }
    val triCheckbox = defaultMillenniumTriStateCheckboxTheme().let { t ->
        MillenniumTriStateCheckboxTheme(states = t.states.mapStates { _, p ->
            p.copy(
                faceBackground = insetFill,
                selectedFaceBackground = flux,
                selectedFaceForeground = colors.onAccent,
                faceShape = RoundedCornerShape(8.dp),
                faceGlowColor = Clear,
                labelStyle = type.bodyMedium,
            )
        })
    }

    val slider = defaultMillenniumSliderTheme().let { t ->
        MillenniumSliderTheme(states = t.states.mapStates { _, p ->
            p.copy(
                trackBackground = insetFill,
                activeTrackBackground = flux,
                thumbBackground = SolidColor(Color.White),
                trackShape = Pill,
                thumbShape = Pill,
                trackBorderColor = Clear,
                thumbBorderColor = Clear,
                thumbGlowColor = colors.accentTop.copy(alpha = 0.6f),
                thumbBottomRimColor = Clear,
                trackInsetShadeColor = Clear,
            )
        })
    }

    val progress = defaultMillenniumProgressTheme().let { t ->
        MillenniumProgressTheme(states = t.states.mapStates { _, p ->
            p.copy(
                trackBackground = insetFill,
                progressBackground = flux,
                shape = Pill,
                borderColor = Clear,
                innerGlowColor = Clear,
            )
        })
    }

    val tabs = defaultMillenniumTabGroupTheme().let { t ->
        MillenniumTabGroupTheme(states = t.states.mapStates { _, p ->
            p.copy(
                listBackground = insetFill,
                listShape = Medium,
                listBorderColor = Clear,
                tabShape = Small,
                selectedTabBackground = flux,
                selectedTabForeground = colors.onAccent,
                unselectedTabForeground = colors.muted,
                tabTextStyle = type.labelLarge,
                tabGlowColor = Clear,
                tabBorderWidth = 0.dp,
            )
        })
    }

    val segmented = defaultMillenniumSegmentedButtonTheme().let { t ->
        MillenniumSegmentedButtonTheme(states = t.states.mapStates { state, p ->
            p.copy(
                background = if (state == MillenniumControlState.Selected) flux else null,
                foreground = if (state == MillenniumControlState.Selected) colors.onAccent else null,
                textStyle = type.labelLarge,
                innerGlowColor = Clear,
            )
        })
    }

    val textField = defaultMillenniumTextFieldTheme().let { t ->
        MillenniumTextFieldTheme(states = t.states.mapStates { state, p ->
            p.copy(
                background = insetFill,
                shape = Small,
                textStyle = type.bodyMedium,
                labelStyle = type.labelMedium,
                placeholderStyle = type.bodyMedium,
                errorStyle = type.bodySmall,
                placeholderColor = colors.muted,
                cursorColor = colors.accent,
                borderColor = when (state) {
                    MillenniumControlState.Focused -> colors.accentTop
                    MillenniumControlState.Error -> colors.danger
                    else -> colors.border.copy(alpha = 0.55f)
                },
                insetShadeColor = Clear,
                innerGlowColor = Clear,
                bottomRimColor = Clear,
            )
        })
    }

    val card = defaultMillenniumCardTheme().let { t ->
        MillenniumCardTheme(states = t.states.mapStates { _, p ->
            p.copy(
                background = SolidColor(colors.surface),
                shape = Medium,
                borderColor = colors.border.copy(alpha = 0.35f),
                innerGlowColor = Clear,
                bottomRimColor = Clear,
                contentTextStyle = type.bodyMedium,
            )
        })
    }

    val listTile = defaultMillenniumListTileTheme().let { t ->
        MillenniumListTileTheme(states = t.states.mapStates { state, p ->
            p.copy(
                titleStyle = type.titleSmall,
                subtitleStyle = type.bodySmall,
                subtitleForeground = colors.muted,
                background = when (state) {
                    MillenniumControlState.Hovered, MillenniumControlState.Focused,
                    MillenniumControlState.Pressed -> SolidColor(colors.raised)
                    else -> null
                },
                selectedBackground = flux,
                selectedTitleForeground = colors.onAccent,
                selectedSubtitleForeground = colors.onAccent.copy(alpha = 0.8f),
                shape = Small,
                borderColor = Clear,
                innerGlowColor = Clear,
            )
        })
    }

    val dialog = defaultMillenniumDialogTheme().let { t ->
        MillenniumDialogTheme(states = t.states.mapStates { _, p ->
            p.copy(
                panelBackground = SolidColor(colors.surface),
                shape = Large,
                titleStyle = type.titleLarge,
                descriptionStyle = type.bodyMedium,
                panelBorderColor = colors.border.copy(alpha = 0.55f),
                panelGlowColor = Clear,
                titleBarBackground = ClearBrush,
                titleBarBorderColor = Clear,
                footerBackground = ClearBrush,
                footerBorderColor = Clear,
            )
        })
    }

    val dropdown = defaultMillenniumDropdownMenuTheme().let { t ->
        MillenniumDropdownMenuTheme(states = t.states.mapStates { _, p ->
            p.copy(
                panelBackground = SolidColor(colors.surface),
                panelShape = Medium,
                panelBorderColor = colors.border.copy(alpha = 0.55f),
                panelGlowColor = Clear,
                itemShape = Small,
                itemTextStyle = type.bodyMedium,
                groupLabelStyle = type.labelSmall,
            )
        })
    }

    val sheet = defaultMillenniumModalBottomSheetTheme().let { t ->
        val padding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 8.dp)
        MillenniumModalBottomSheetTheme(states = t.states.mapStates { _, p ->
            p.copy(
                background = SolidColor(colors.surface),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                contentPadding = padding,
                contentSpacing = 8.dp,
                handleColor = colors.border,
                borderColor = colors.border.copy(alpha = 0.55f),
                innerGlowColor = Clear,
            )
        })
    }

    val topBar = defaultMillenniumTopAppBarTheme().let { t ->
        MillenniumTopAppBarTheme(
            properties = t.properties.copy(
                titleStyle = type.headlineSmall,
                subtitleStyle = type.bodySmall,
                subtitleForeground = colors.muted,
                background = ClearBrush,
                separatorColor = Clear,
            )
        )
    }

    val sidebar = defaultMillenniumSidebarTheme().let { t ->
        MillenniumSidebarTheme(
            root = t.root.mapStates { _, p ->
                p.copy(
                    background = ClearBrush,
                    shape = Large,
                    shadowElevation = 0.dp,
                    borderColor = colors.border.copy(alpha = 0.55f),
                    innerGlowColor = Clear,
                )
            },
            header = t.header.mapStates { _, p -> p.copy(background = ClearBrush, dividerColor = Clear) },
            content = t.content,
            footer = t.footer.mapStates { _, p -> p.copy(background = ClearBrush, dividerColor = Clear) },
            group = t.group.mapStates { _, p ->
                p.copy(
                    labelStyle = type.labelSmall,
                    labelForeground = colors.muted,
                    labelShape = Small,
                    labelBorderColor = Clear,
                    labelBackground = ClearBrush,
                    labelInnerGlowColor = Clear,
                )
            },
            item = t.item.mapStates { state, p ->
                p.copy(
                    labelStyle = type.labelLarge,
                    shape = Medium,
                    borderColor = if (state == MillenniumControlState.Focused) colors.accent else Clear,
                    innerGlowColor = Clear,
                    selectedIndicatorColor = Clear,
                )
            },
            badge = t.badge.mapStates { _, p ->
                p.copy(background = flux, foreground = colors.onAccent, shape = Pill, borderColor = Clear, innerGlowColor = Clear)
            },
            trigger = t.trigger.mapStates { _, p ->
                p.copy(shape = Pill, borderColor = Clear, innerGlowColor = Clear, shadowElevation = 0.dp)
            },
        )
    }

    val bottomNav = defaultMillenniumBottomNavigationBarTheme().let { t ->
        MillenniumBottomNavigationBarTheme(
            states = t.states.mapStates { _, p ->
                p.copy(
                    background = ClearBrush,
                    shape = Pill,
                    borderColor = colors.border.copy(alpha = 0.55f),
                    innerGlowColor = Clear,
                    shadowElevation = 12.dp,
                )
            },
            item = t.item.mapStates { state, p ->
                p.copy(
                    background = when (state) {
                        MillenniumControlState.Selected -> flux
                        else -> null
                    },
                    shape = Pill,
                    labelStyle = type.labelMedium,
                    borderColor = if (state == MillenniumControlState.Focused) colors.accent else Clear,
                    innerGlowColor = Clear,
                )
            },
        )
    }

    return base.copy(
        textTheme = text,
        buttonTheme = buttons,
        iconButtonTheme = iconButtons,
        toggleSwitchTheme = toggle,
        checkboxTheme = checkbox,
        triStateCheckboxTheme = triCheckbox,
        sliderTheme = slider,
        progressTheme = progress,
        tabGroupTheme = tabs,
        segmentedButtonTheme = segmented,
        textFieldTheme = textField,
        cardTheme = card,
        listTileTheme = listTile,
        dialogTheme = dialog,
        dropdownMenuTheme = dropdown,
        modalBottomSheetTheme = sheet,
        topAppBarTheme = topBar,
        sidebarTheme = sidebar,
        bottomNavigationBarTheme = bottomNav,
    )
}

