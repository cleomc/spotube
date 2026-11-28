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
import org.spotube.millennium.composables.MillenniumSeparatorTheme
import org.spotube.millennium.composables.defaultMillenniumSeparatorTheme
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
 * pills for buttons, 10/14/22dp corners elsewhere. Primary actions are ink (a text fill with a
 * background label); the accent only appears as thin light (checked controls, played lines,
 * focus borders, selection ticks).
 */

private val Clear = Color.Transparent
private val ClearBrush: Brush = SolidColor(Color.Transparent)
private val Pill = CircleShape
private val Small = RoundedCornerShape(10.dp)
private val Medium = RoundedCornerShape(14.dp)
private val Large = RoundedCornerShape(22.dp)

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
    val ink: Brush = SolidColor(colors.text)
    val accentFill: Brush = SolidColor(colors.accentTop)
    val hair = colors.border.copy(alpha = 0.6f)
    val wash = { alpha: Float -> SolidColor(colors.text.copy(alpha = alpha)) }
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
            background: (MillenniumControlState) -> Brush?,
            border: (MillenniumControlState) -> Color?,
            foreground: Color,
        ) = mapStates { state, p ->
            p.copy(
                background = background(state),
                foreground = foreground,
                textStyle = label,
                shape = Pill,
                padding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                contentSpacing = 8.dp,
                borderColor = if (state == MillenniumControlState.Focused) colors.accent else border(state),
                shadows = null,
                shadowElevation = 0.dp,
                innerGlowColor = Clear,
                bottomRimColor = Clear,
                alpha = if (state == MillenniumControlState.Disabled) 0.38f else 1f,
                motion = p.motion.copy(hoverScale = 1f, pressedScale = 0.97f),
            )
        }
        val hoverish = setOf(MillenniumControlState.Hovered, MillenniumControlState.Pressed)
        MillenniumButtonTheme(
            primary = t.primary.restyle({ ink }, { Clear }, colors.background),
            secondary = t.secondary.restyle(
                { if (it in hoverish) wash(0.04f) else null },
                { if (it in hoverish) colors.text else colors.border },
                colors.text,
            ),
            quiet = t.quiet.restyle({ if (it in hoverish) wash(0.06f) else null }, { null }, colors.text),
            danger = t.danger.restyle(
                { if (it in hoverish) SolidColor(colors.danger.copy(alpha = 0.08f)) else null },
                { colors.danger.copy(alpha = 0.6f) },
                colors.danger,
            ),
        )
    }

    val iconButtons = defaultMillenniumIconButtonTheme().let { t ->
        fun MillenniumControlStateProperty<org.spotube.millennium.composables.MillenniumIconButtonThemeProperties>.restyle(
            background: (MillenniumControlState) -> Brush?,
            border: Color?,
            foreground: Color?,
        ) = mapStates { state, p ->
            p.copy(
                background = background(state),
                foreground = foreground ?: p.foreground,
                shape = Pill,
                borderColor = if (state == MillenniumControlState.Focused) colors.accent else border,
                shadows = null,
                shadowElevation = 0.dp,
                innerGlowColor = Clear,
                bottomRimColor = Clear,
                orb = false,
                alpha = if (state == MillenniumControlState.Disabled) 0.38f else 1f,
                motion = p.motion.copy(hoverScale = 1.04f, pressedScale = 0.92f),
            )
        }
        val hoverish = setOf(MillenniumControlState.Hovered, MillenniumControlState.Pressed)
        MillenniumIconButtonTheme(
            primary = t.primary.restyle({ ink }, Clear, colors.background),
            secondary = t.secondary.restyle({ if (it in hoverish) wash(0.06f) else null }, colors.border, null),
            quiet = t.quiet.restyle({ if (it in hoverish) wash(0.07f) else null }, null, null),
            danger = t.danger.restyle(
                { if (it in hoverish) SolidColor(colors.danger.copy(alpha = 0.08f)) else null },
                colors.danger.copy(alpha = 0.6f),
                colors.danger,
            ),
        )
    }

    val toggle = defaultMillenniumToggleSwitchTheme().let { t ->
        MillenniumToggleSwitchTheme(states = t.states.mapStates { _, p ->
            p.copy(
                trackBackground = insetFill,
                selectedTrackBackground = accentFill,
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
                selectedFaceBackground = accentFill,
                selectedFaceForeground = colors.onAccent,
                faceShape = RoundedCornerShape(6.dp),
                faceGlowColor = Clear,
                labelStyle = type.bodyMedium,
            )
        })
    }
    val triCheckbox = defaultMillenniumTriStateCheckboxTheme().let { t ->
        MillenniumTriStateCheckboxTheme(states = t.states.mapStates { _, p ->
            p.copy(
                faceBackground = insetFill,
                selectedFaceBackground = accentFill,
                selectedFaceForeground = colors.onAccent,
                faceShape = RoundedCornerShape(6.dp),
                faceGlowColor = Clear,
                labelStyle = type.bodyMedium,
            )
        })
    }

    val slider = defaultMillenniumSliderTheme().let { t ->
        MillenniumSliderTheme(states = t.states.mapStates { _, p ->
            p.copy(
                trackBackground = SolidColor(colors.border.copy(alpha = 0.7f)),
                activeTrackBackground = flux,
                thumbBackground = ink,
                trackShape = Pill,
                thumbShape = Pill,
                trackBorderColor = Clear,
                thumbBorderColor = Clear,
                thumbGlowColor = Clear,
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
                listShape = Pill,
                listBorderColor = hair,
                tabShape = Pill,
                selectedTabBackground = wash(0.09f),
                selectedTabForeground = colors.text,
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
                background = if (state == MillenniumControlState.Selected) ink else null,
                foreground = if (state == MillenniumControlState.Selected) colors.background else null,
                textStyle = type.labelLarge,
                innerGlowColor = Clear,
            )
        })
    }

    val textField = defaultMillenniumTextFieldTheme().let { t ->
        MillenniumTextFieldTheme(states = t.states.mapStates { state, p ->
            p.copy(
                background = SolidColor(
                    if (state == MillenniumControlState.Focused) colors.raised else colors.inset.copy(alpha = 0.7f)
                ),
                shape = Small,
                textStyle = type.bodyMedium,
                labelStyle = type.labelSmall,
                placeholderStyle = type.bodyMedium,
                errorStyle = type.bodySmall,
                placeholderColor = colors.muted,
                cursorColor = colors.accent,
                borderColor = when (state) {
                    MillenniumControlState.Focused -> colors.accent
                    MillenniumControlState.Error -> colors.danger
                    MillenniumControlState.Hovered -> colors.text.copy(alpha = 0.6f)
                    else -> colors.border
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
                background = SolidColor(colors.surface.copy(alpha = 0.55f)),
                shape = Medium,
                borderColor = colors.border.copy(alpha = 0.45f),
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
                    MillenniumControlState.Pressed -> wash(0.04f)
                    else -> null
                },
                selectedBackground = wash(0.05f),
                selectedTitleForeground = colors.accent,
                selectedSubtitleForeground = colors.muted,
                shape = Small,
                borderColor = Clear,
                innerGlowColor = Clear,
            )
        })
    }

    val separator = defaultMillenniumSeparatorTheme().let { t ->
        MillenniumSeparatorTheme(states = t.states.mapStates { _, p ->
            p.copy(color = colors.border.copy(alpha = 0.5f), highlightColor = Clear)
        })
    }

    val dialog = defaultMillenniumDialogTheme().let { t ->
        MillenniumDialogTheme(states = t.states.mapStates { _, p ->
            p.copy(
                panelBackground = SolidColor(colors.surface),
                shape = Large,
                titleStyle = type.titleLarge,
                descriptionStyle = type.bodyMedium,
                panelBorderColor = hair,
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
                panelBorderColor = hair,
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
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                contentPadding = padding,
                contentSpacing = 8.dp,
                handleColor = colors.border,
                borderColor = hair,
                innerGlowColor = Clear,
            )
        })
    }

    val topBar = defaultMillenniumTopAppBarTheme().let { t ->
        MillenniumTopAppBarTheme(
            properties = t.properties.copy(
                titleStyle = type.headlineMedium,
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
                    borderColor = hair,
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
                    selectedIndicatorColor = colors.accentTop,
                )
            },
            badge = t.badge.mapStates { _, p ->
                p.copy(background = accentFill, foreground = colors.onAccent, shape = Pill, borderColor = Clear, innerGlowColor = Clear)
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
                    borderColor = hair,
                    innerGlowColor = Clear,
                    shadowElevation = 0.dp,
                )
            },
            item = t.item.mapStates { state, p ->
                p.copy(
                    background = when (state) {
                        MillenniumControlState.Selected -> wash(0.09f)
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
        separatorTheme = separator,
        dialogTheme = dialog,
        dropdownMenuTheme = dropdown,
        modalBottomSheetTheme = sheet,
        topAppBarTheme = topBar,
        sidebarTheme = sidebar,
        bottomNavigationBarTheme = bottomNav,
    )
}

