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

// Generated from the Spotube Flux echo icons (geometry from Lucide, ISC licence). Do not edit.

package org.spotube.spotube.resources.iconsax

import androidx.compose.ui.graphics.vector.ImageVector

val Iconsax.IconsaxRepeatArrow: ImageVector
    get() {
        _IconsaxRepeatArrow?.let { return it }
        return fluxIcon(
            name = "IconsaxRepeatArrow",
            strokes = listOf(
                "m2 9 3-3 3 3",
                "M13 18H7a2 2 0 0 1-2-2V6",
                "m22 15-3 3-3-3",
                "M11 6h6a2 2 0 0 1 2 2v10",
            ),
            fills = emptyList(),
        ).also { _IconsaxRepeatArrow = it }
    }

@Suppress("ObjectPropertyName")
private var _IconsaxRepeatArrow: ImageVector? = null
