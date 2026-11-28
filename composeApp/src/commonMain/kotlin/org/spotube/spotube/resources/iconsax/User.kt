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

val Iconsax.User: ImageVector
    get() {
        _User?.let { return it }
        return fluxIcon(
            name = "User",
            strokes = listOf(
                "M7 8a5 5 0 1 0 10 0a5 5 0 1 0 -10 0z",
                "M20 21a8 8 0 0 0-16 0",
            ),
            fills = emptyList(),
        ).also { _User = it }
    }

@Suppress("ObjectPropertyName")
private var _User: ImageVector? = null
