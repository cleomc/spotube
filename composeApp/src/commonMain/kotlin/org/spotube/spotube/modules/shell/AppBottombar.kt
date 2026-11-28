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

package org.spotube.spotube.modules.shell

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composeunstyled.Text
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.spotube.spotube.core.ui.theming.FluxIcon
import org.spotube.spotube.NavigationItem
import org.spotube.spotube.bottombarTabs
import org.spotube.spotube.core.navigation.NavigationState
import org.spotube.spotube.core.navigation.Navigator
import org.spotube.spotube.core.navigation.Routes
import org.spotube.spotube.core.ui.theming.AppTheme
import org.spotube.spotube.core.ui.theming.fluxGlass
import org.spotube.spotube.core.ui.theming.fluxPress
import org.spotube.spotube.modules.downloads.DownloadBadgeIndicator

private val BarHeight = 64.dp
private val BarPadding = 6.dp

/**
 * Floating fluted-glass pill. The selection is a soft `text` 9 % capsule with a 4dp `accentTop`
 * dot at its foot; its leading edge springs to the new tab first and its trailing edge follows,
 * so it stretches while it travels and settles back with a little wobble.
 */
@Composable
fun AppBottombar(
    navigator: Navigator,
    navigationState: NavigationState,
    modifier: Modifier = Modifier,
    libraryBadge: @Composable () -> Unit = { DownloadBadgeIndicator() }
) {
    val tabs = remember { bottombarTabs.filterIsInstance<NavigationItem.Tab<*>>() }
    val selectedIndex = tabs.indexOfFirst { it.route == navigationState.topLevelRoute }
    val flux = AppTheme.flux
    val capsule = AppTheme.colors.onSurface.copy(alpha = 0.09f)

    Box(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .fillMaxWidth()
            .height(BarHeight)
            .fluxGlass(CircleShape)
            .padding(BarPadding)
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val count = tabs.size.coerceAtLeast(1)
            val slot = maxWidth / count
            val slotHeight = maxHeight
            val target = selectedIndex.coerceAtLeast(0).toFloat()
            // Bubble edges in slot units, animated separately for the liquid stretch.
            val leading = remember { Animatable(target) }
            val trailing = remember { Animatable(target) }
            LaunchedEffect(target) {
                coroutineScope {
                    val fast = spring<Float>(dampingRatio = 0.62f, stiffness = 520f)
                    val slow = spring<Float>(dampingRatio = 0.55f, stiffness = 190f)
                    val movingRight = target > leading.value
                    launch { leading.animateTo(target, if (movingRight) fast else slow) }
                    launch { trailing.animateTo(target, if (movingRight) slow else fast) }
                }
            }
            val bubbleAlpha by animateFloatAsState(if (selectedIndex >= 0) 1f else 0f, tween(160))

            Box(
                Modifier.fillMaxSize().drawBehind {
                    if (bubbleAlpha <= 0f) return@drawBehind
                    val slotPx = size.width / count
                    val left = minOf(leading.value, trailing.value) * slotPx
                    val right = (maxOf(leading.value, trailing.value) + 1f) * slotPx
                    val stretch = (right - left) / slotPx - 1f
                    // Squash across the motion axis while stretched (94 % at full stretch).
                    val squash = size.height * 0.06f * stretch.coerceIn(0f, 1f)
                    val top = squash / 2f
                    val h = size.height - squash
                    val r = CornerRadius(h / 2f, h / 2f)
                    drawRoundRect(
                        color = capsule, topLeft = Offset(left, top),
                        size = Size(right - left, h), cornerRadius = r, alpha = bubbleAlpha,
                    )
                    // The accent dot at the capsule's foot, riding its centre.
                    val dot = 2.dp.toPx()
                    drawCircle(
                        color = flux.accentTop, radius = dot,
                        center = Offset((left + right) / 2f, top + h - 5.dp.toPx() - dot),
                        alpha = bubbleAlpha,
                    )
                }
            )

            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Start) {
                tabs.forEachIndexed { index, tab ->
                    BottomTab(
                        tab = tab,
                        selected = index == selectedIndex,
                        modifier = Modifier.size(slot, slotHeight),
                        onClick = { navigator.navigate(tab.route) },
                        badge = if (tab.route == Routes.Library) libraryBadge else null,
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomTab(
    tab: NavigationItem.Tab<*>,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    badge: (@Composable () -> Unit)?,
) {
    val colors = AppTheme.colors
    val title = stringResource(tab.title)
    val tint by animateColorAsState(
        if (selected) colors.onSurface else colors.onSurfaceVariant, tween(160), label = "tab-tint"
    )
    val lift by animateFloatAsState(
        if (selected) 1f else 0f,
        spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "tab-lift",
    )
    Column(
        modifier = modifier
            .fluxPress(0.9f)
            .semantics { this.selected = selected }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box {
            FluxIcon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = tint,
                size = 20.dp,
                lively = selected,
                modifier = Modifier.graphicsLayer {
                    translationY = -3.dp.toPx() * lift
                },
            )
            if (badge != null) {
                Box(Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-4).dp)) { badge() }
            }
        }
        Text(
            text = title,
            style = AppTheme.typography.labelMedium.copy(fontSize = 10.sp, lineHeight = 12.sp),
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 3.dp, bottom = 6.dp, start = 2.dp, end = 2.dp),
        )
    }
}
