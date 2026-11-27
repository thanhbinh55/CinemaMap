"""Create CinemaMap's original five-second cartoon using only procedural shapes.

Requires Python 3, Pillow and ffmpeg/libx264. No downloaded footage, character
assets, music or font files. The small title alphabet is drawn from a pixel grid.
Run from any directory: python3 tools/generate_trailer.py
"""
from pathlib import Path
import math
import subprocess

from PIL import Image, ImageDraw

WIDTH, HEIGHT, FPS, DURATION = 1008, 432, 24, 5
SCALE = 2
ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "app/src/main/res/raw/sao_nho_trailer.mp4"

LETTERS = {
    "A": ["01110", "10001", "10001", "11111", "10001", "10001", "10001"],
    "B": ["11110", "10001", "10001", "11110", "10001", "10001", "11110"],
    "C": ["01111", "10000", "10000", "10000", "10000", "10000", "01111"],
    "D": ["11110", "10001", "10001", "10001", "10001", "10001", "11110"],
    "E": ["11111", "10000", "10000", "11110", "10000", "10000", "11111"],
    "H": ["10001", "10001", "10001", "11111", "10001", "10001", "10001"],
    "I": ["11111", "00100", "00100", "00100", "00100", "00100", "11111"],
    "N": ["10001", "11001", "11001", "10101", "10011", "10011", "10001"],
    "O": ["01110", "10001", "10001", "10001", "10001", "10001", "01110"],
    "S": ["01111", "10000", "10000", "01110", "00001", "00001", "11110"],
    "T": ["11111", "00100", "00100", "00100", "00100", "00100", "00100"],
    "U": ["10001", "10001", "10001", "10001", "10001", "10001", "01110"],
    "V": ["10001", "10001", "10001", "10001", "10001", "01010", "00100"],
    "Y": ["10001", "10001", "01010", "00100", "00100", "00100", "00100"],
    " ": ["00000"] * 7,
}


def ease(value):
    value = min(1, max(0, value))
    return value * value * (3 - 2 * value)


class Frame:
    def __init__(self, time):
        self.image = Image.new("RGB", (WIDTH * SCALE, HEIGHT * SCALE))
        self.draw = ImageDraw.Draw(self.image)
        for y in range(HEIGHT):
            f = y / HEIGHT
            wave = math.sin(time * math.tau / DURATION)
            color = (int(206 + 11 * wave + 20 * f), int(232 + 5 * wave + 5 * f), int(247 - 17 * f))
            self.rect((0, y, WIDTH, y + 1), color)

    def points(self, points):
        return [(round(x * SCALE), round(y * SCALE)) for x, y in points]

    def rect(self, box, fill, radius=0):
        scaled = tuple(round(v * SCALE) for v in box)
        if radius:
            self.draw.rounded_rectangle(scaled, radius=round(radius * SCALE), fill=fill)
        else:
            self.draw.rectangle(scaled, fill=fill)

    def oval(self, box, fill):
        self.draw.ellipse(tuple(round(v * SCALE) for v in box), fill=fill)

    def line(self, points, fill, width=3):
        self.draw.line(self.points(points), fill=fill, width=max(1, round(width * SCALE)), joint="curve")

    def polygon(self, points, fill):
        self.draw.polygon(self.points(points), fill=fill)

    def title(self, text, center_x, y, size, color):
        width = (len(text) * 6 - 1) * size
        left = center_x - width / 2
        for index, letter in enumerate(text):
            for row, values in enumerate(LETTERS[letter]):
                for col, value in enumerate(values):
                    if value == "1":
                        x = left + (index * 6 + col) * size
                        self.rect((x, y + row * size, x + size * .86, y + (row + 1) * size - size * .14), color, size * .12)


def star(frame, x, y, radius, time):
    points = []
    for i in range(10):
        a = -math.pi / 2 + i * math.pi / 5
        r = radius if i % 2 == 0 else radius * .48
        points.append((x + math.cos(a) * r, y + math.sin(a) * r))
    frame.oval((x - radius * 1.3, y - radius * 1.3, x + radius * 1.3, y + radius * 1.3), "#FFF2C0")
    frame.polygon(points, "#F8BD4F")
    blink = (time % 2.2) > 2.05
    for dx in [-radius * .2, radius * .2]:
        if blink:
            frame.line([(x + dx - 3, y), (x + dx + 3, y)], "#244458", 2)
        else:
            frame.oval((x + dx - 2.5, y - 3, x + dx + 2.5, y + 3), "#244458")
    frame.line([(x - 4, y + 9), (x, y + 12), (x + 4, y + 9)], "#9F5838", 2)


def robot(frame, x, y, time, scale=1):
    # Original rounded square character; x/y are the centre of its feet.
    def p(a, b):
        return x + a * scale, y + b * scale
    def box(a, b, c, d, color, radius=0):
        frame.rect((*p(a, b), *p(c, d)), color, radius * scale)
    stride = math.sin(time * 10) * 6
    frame.line([p(-15, -20), p(-21 + stride, 0)], "#2C5366", 8 * scale)
    frame.line([p(15, -20), p(21 - stride, 0)], "#2C5366", 8 * scale)
    box(-30, -17, -6, -8, "#456B7D", 4)
    box(6, -17, 30, -8, "#456B7D", 4)
    frame.line([p(-31, -52), p(-48, -34 + stride)], "#3B7189", 8 * scale)
    frame.line([p(31, -52), p(49, -74 + stride)], "#3B7189", 8 * scale)
    box(-31, -69, 31, -20, "#4E94B0", 12)
    box(-24, -95, 24, -53, "#316B86", 11)
    box(-17, -87, 17, -65, "#E7FAF6", 7)
    for dx in [-9, 9]:
        frame.oval((*p(dx - 2.5, -81), *p(dx + 2.5, -72)), "#1E4055")
    frame.line([p(0, -95), p(0, -108)], "#316B86", 3 * scale)
    frame.oval((*p(-5, -113), *p(5, -103)), "#F8BD4F")
    box(-10, -48, 10, -31, "#AEDBD9", 4)


def render(time):
    f = Frame(time)
    # All backgrounds, clouds, islands, characters and title lettering are drawn here.
    for index in range(6):
        x = ((index * 211 - time * 18) % 1260) - 120
        y = 45 + index % 3 * 30
        f.oval((x, y, x + 126, y + 30), "#EDF8F7")
        f.oval((x + 33, y - 18, x + 89, y + 24), "#EDF8F7")
    f.oval((790, -70, 1080, 205), "#FFF4CB")
    for x, y, size in [(100, 108, 7), (310, 54, 5), (740, 108, 7)]:
        radius = size + math.sin(time * 4 + x) * 2
        f.line([(x - radius, y), (x + radius, y)], "#6DADBB", 2)
        f.line([(x, y - radius), (x, y + radius)], "#6DADBB", 2)
    ground = 336
    f.polygon([(0, ground), (420, ground), (390, 432), (0, 432)], "#C6DCD1")
    f.polygon([(586, ground), (1008, ground), (1008, 432), (615, 432)], "#C6DCD1")
    f.rect((0, ground, 420, ground + 18), "#78ACAA", 6)
    f.rect((586, ground, 1008, ground + 18), "#78ACAA", 6)
    for x in range(30, 1000, 86):
        if 410 < x < 590:
            continue
        f.line([(x, 383), (x + 20, 396), (x + 33, 382)], "#AAC9BD", 3)
    if time < 1.5:
        a = ease(time / 1.5)
        x = 180 + 200 * a
        f.oval((x - 45, ground - 8, x + 45, ground + 7), "#608F94")
        robot(f, x, ground - 7 + math.sin(time * 9) * 3, time)
        star(f, 535, 205 + math.sin(time * 4) * 13, 35, time)
        f.title("CHUYEN DI BAT DAU", 490, 34, 3, "#31576C")
    elif time < 3.55:
        a = ease((time - 1.5) / 2.05)
        x = 380 + 280 * a
        lift = math.sin(a * math.pi) * 128
        for i in range(7):
            px = x - 35 - i * 13
            py = ground - lift - 50 + i * 5
            f.oval((px - 3, py - 3, px + 3, py + 3), "#F2CF7A")
        robot(f, x, ground - 7 - lift, time)
        star(f, x + 90, ground - 125 - lift * .65, 34, time)
    else:
        a = ease((time - 3.55) / .5)
        f.rect((95, 51 + (1 - a) * 30, 913, 268 + (1 - a) * 30), "#EDF5E9", 28)
        f.title("SAO NHO", 504, 78 + (1 - a) * 30, 13, "#285B75")
        f.title("CHUYEN DI BAT DAU", 504, 198 + (1 - a) * 30, 4, "#517D8D")
        robot(f, 650, ground - 7 + math.sin(time * 7) * 3, time, .88)
        star(f, 750, 283 + math.sin(time * 5) * 9, 30, time)
    return f.image.resize((WIDTH, HEIGHT), Image.Resampling.LANCZOS)


def main():
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    command = ["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-f", "rawvideo", "-pix_fmt", "rgb24",
               "-s", f"{WIDTH}x{HEIGHT}", "-r", str(FPS), "-i", "-", "-an", "-c:v", "libx264",
               "-profile:v", "baseline", "-level:v", "3.1", "-pix_fmt", "yuv420p", "-crf", "20",
               "-g", str(FPS), "-movflags", "+faststart", "-map_metadata", "-1",
               "-metadata", "title=Sao Nho - CinemaMap original animation", str(OUTPUT)]
    process = subprocess.Popen(command, stdin=subprocess.PIPE)
    try:
        for frame in range(FPS * DURATION):
            process.stdin.write(render(frame / FPS).tobytes())
    finally:
        process.stdin.close()
    if process.wait() != 0:
        raise SystemExit("ffmpeg could not create the trailer")
    print(f"Created {OUTPUT} ({OUTPUT.stat().st_size:,} bytes)")


if __name__ == "__main__":
    main()
