#!/usr/bin/env python3
"""Capture a small gfxinfo pan sample on a connected Android device (no dependencies).

Restarts CinemaMap to its initial task before measuring; do not run alongside UI tests.
Results describe this emulator/device/build only, not a universal FPS guarantee.
"""
import argparse
import re
import struct
import subprocess

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--adb', default='adb')
parser.add_argument('--serial', required=True)
parser.add_argument('--cycles', type=int, default=4)
args = parser.parse_args()
if not 1 <= args.cycles <= 20:
    parser.error('--cycles must be between 1 and 20')
base = [args.adb, '-s', args.serial]
package = 'vn.edu.midterm.cinemamap'


def adb(*command, binary=False):
    return subprocess.run(base + list(command), check=True, capture_output=True,
                          text=not binary).stdout


print('MODEL:', adb('shell', 'getprop', 'ro.product.model').strip())
print('API:', adb('shell', 'getprop', 'ro.build.version.sdk').strip())
print(adb('shell', 'am', 'start', '-S', '-W', '-n', package + '/.MainActivity').strip())
# Reading window accessibility tree also waits for the initial screen to settle.
adb('shell', 'uiautomator', 'dump', '/data/local/tmp/cinemamap-profile.xml')
png = adb('exec-out', 'screencap', '-p', binary=True)
width, height = struct.unpack('>II', png[16:24])
if width >= height:
    raise SystemExit('Use portrait orientation for this bounded pan sample.')
print('SCREEN:', width, 'x', height)
print('SAMPLE:', args.cycles * 4, 'one-finger swipes; 240 ms each; fit camera; 196 seats')
adb('shell', 'dumpsys', 'gfxinfo', package, 'reset')
for _ in range(args.cycles):
    for x0, y0, x1, y1 in [(0.35, .40, .65, .40), (.65, .40, .35, .40),
                            (.5, .40, .5, .55), (.5, .55, .5, .40)]:
        adb('shell', 'input', 'swipe', str(int(width * x0)), str(int(height * y0)),
            str(int(width * x1)), str(int(height * y1)), '240')
report = adb('shell', 'dumpsys', 'gfxinfo', package, 'framestats')
print(report)
frames = re.search(r'Total frames rendered: (\d+)', report)
if not frames or int(frames.group(1)) == 0:
    raise SystemExit('No rendered frames recorded; do not treat this as a passing measurement.')
