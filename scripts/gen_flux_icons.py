"""Convert the Flux echo icons (SVG, ink + echo groups) into Kotlin ImageVector properties.

Usage: python -I scripts/gen_flux_icons.py <Flux Icons svg dir> composeApp/src/commonMain/kotlin/org/spotube/spotube/resources/iconsax
Only the `ink` group is read; the echo is rebuilt in code by fluxIcon().
"""
import os
import re
import sys
import xml.etree.ElementTree as ET

HEADER = '''/*
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
'''


def f(v, default=0.0):
    return float(v) if v is not None else default


def num(x):
    s = ('%.3f' % x).rstrip('0').rstrip('.')
    return s if s not in ('-0', '') else '0'


def shape_to_d(el):
    tag = el.tag.split('}')[-1]
    a = el.attrib
    if tag == 'path':
        return a['d']
    if tag == 'circle':
        cx, cy, r = f(a.get('cx')), f(a.get('cy')), f(a.get('r'))
        return 'M%s %sa%s %s 0 1 0 %s 0a%s %s 0 1 0 %s 0z' % (
            num(cx - r), num(cy), num(r), num(r), num(2 * r), num(r), num(r), num(-2 * r))
    if tag == 'ellipse':
        cx, cy, rx, ry = f(a.get('cx')), f(a.get('cy')), f(a.get('rx')), f(a.get('ry'))
        return 'M%s %sa%s %s 0 1 0 %s 0a%s %s 0 1 0 %s 0z' % (
            num(cx - rx), num(cy), num(rx), num(ry), num(2 * rx), num(rx), num(ry), num(-2 * rx))
    if tag == 'line':
        return 'M%s %sL%s %s' % (num(f(a.get('x1'))), num(f(a.get('y1'))),
                                  num(f(a.get('x2'))), num(f(a.get('y2'))))
    if tag == 'rect':
        x, y, w, h = f(a.get('x')), f(a.get('y')), f(a.get('width')), f(a.get('height'))
        rx = a.get('rx')
        ry = a.get('ry')
        rx = f(rx if rx is not None else ry)
        ry = f(ry if ry is not None else rx)
        rx, ry = min(rx, w / 2), min(ry, h / 2)
        if rx <= 0:
            return 'M%s %sh%sv%sh%sz' % (num(x), num(y), num(w), num(h), num(-w))
        return ('M%s %sh%sa%s %s 0 0 1 %s %sv%sa%s %s 0 0 1 %s %sh%sa%s %s 0 0 1 %s %sv%sa%s %s 0 0 1 %s %sz' % (
            num(x + rx), num(y), num(w - 2 * rx),
            num(rx), num(ry), num(rx), num(ry), num(h - 2 * ry),
            num(rx), num(ry), num(-rx), num(ry), num(-(w - 2 * rx)),
            num(rx), num(ry), num(-rx), num(-ry), num(-(h - 2 * ry)),
            num(rx), num(ry), num(rx), num(-ry)))
    if tag in ('polyline', 'polygon'):
        pts = [float(p) for p in re.split(r'[\s,]+', a['points'].strip())]
        d = 'M' + ' '.join('%s %s' % (num(pts[i]), num(pts[i + 1])) for i in range(0, len(pts), 2))
        return d + ('z' if tag == 'polygon' else '')
    raise ValueError('unsupported element ' + tag)


def main(src, out):
    count = 0
    for name in sorted(os.listdir(src)):
        if not name.endswith('.svg'):
            continue
        prop = name[:-4]
        root = ET.parse(os.path.join(src, name)).getroot()
        ink = next(g for g in root.iter() if g.tag.endswith('g') and g.attrib.get('class') == 'ink')
        strokes, fills = [], []
        for el in ink:
            d = shape_to_d(el)
            (fills if el.attrib.get('fill', 'none') != 'none' else strokes).append(d)
        def lst(items):
            if not items:
                return 'emptyList()'
            return 'listOf(\n' + ''.join('                "%s",\n' % d for d in items) + '            )'
        body = HEADER + '''
val Iconsax.{p}: ImageVector
    get() {{
        _{p}?.let {{ return it }}
        return fluxIcon(
            name = "{p}",
            strokes = {s},
            fills = {f},
        ).also {{ _{p} = it }}
    }}

@Suppress("ObjectPropertyName")
private var _{p}: ImageVector? = null
'''.format(p=prop, s=lst(strokes), f=lst(fills))
        with open(os.path.join(out, prop + '.kt'), 'w', newline='\n', encoding='utf-8') as fh:
            fh.write(body)
        count += 1
    print('generated', count)


if __name__ == '__main__':
    main(sys.argv[1], sys.argv[2])
