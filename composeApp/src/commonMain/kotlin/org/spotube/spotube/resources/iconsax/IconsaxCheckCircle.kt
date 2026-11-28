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

val Iconsax.IconsaxCheckCircle: ImageVector
    get() {
        _IconsaxCheckCircle?.let { return it }
        return fluxIcon(
            name = "IconsaxCheckCircle",
            strokes = listOf(
                "M2 12a10 10 0 1 0 20 0a10 10 0 1 0 -20 0z",
                "m16 9-5.5 5.5L8 12",
            ),
            fills = emptyList(),
        ).also { _IconsaxCheckCircle = it }
    }

@Suppress("ObjectPropertyName")
private var _IconsaxCheckCircle: ImageVector? = null
