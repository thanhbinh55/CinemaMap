"""Render a seamless original campfire loop, without footage or photo pixels.

Python 3 + NumPy + Pillow + ffmpeg/libx264. IMG_5257.HEIC supplied by the
student is a visual reference for orange flames, tepee logs and a dark night;
the photograph itself is not copied into the video or the Android package.
Run: python3 tools/generate_campfire.py
"""
from pathlib import Path
import argparse
import math
import subprocess

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

WIDTH, HEIGHT, FPS, DURATION = 1008, 432, 24, 5
ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "app/src/main/res/raw/campfire_loop.mp4"
FX_WIDTH, FX_HEIGHT = WIDTH // 2, HEIGHT // 2
X, Y = np.meshgrid(np.arange(FX_WIDTH, dtype=np.float32) * 2,
                   np.arange(FX_HEIGHT, dtype=np.float32) * 2)
UP = 358 - Y
RNG = np.random.default_rng(5257)
NOISE = [RNG.random((16 * 2**i, 64), dtype=np.float32) for i in range(5)]


def noise(grid, x, y):
    """Smooth periodic value noise; a whole vertical period passes in 5 s."""
    ix, iy = np.floor(x).astype(np.int32), np.floor(y).astype(np.int32)
    a, b = x - ix, y - iy
    a, b = a * a * (3 - 2 * a), b * b * (3 - 2 * b)
    x0, x1 = ix % grid.shape[1], (ix + 1) % grid.shape[1]
    y0, y1 = iy % grid.shape[0], (iy + 1) % grid.shape[0]
    return ((grid[y0, x0] * (1 - a) + grid[y0, x1] * a) * (1 - b)
            + (grid[y1, x0] * (1 - a) + grid[y1, x1] * a) * b)


def flicker(phase):
    return 0.87 + .07 * math.sin(phase * 7) + .04 * math.sin(phase * 13 + 1.2)


def ground():
    """Static dark ground, subtle paving and warm gravel around the hearth."""
    yy, xx = np.mgrid[:HEIGHT, :WIDTH].astype(np.float32)
    base = np.zeros((HEIGHT, WIDTH, 3), dtype=np.float32)
    base[:] = [3, 6, 10]
    floor = np.clip((yy - 285) / 120, 0, 1)[..., None]
    base += floor * np.array([9, 5, 2], dtype=np.float32)
    halo = np.exp(-((xx - 506) / 260)**2 - ((yy - 366) / 70)**2)
    base += halo[..., None] * np.array([47, 20, 5], dtype=np.float32)
    grain = RNG.normal(0, 1.4, (HEIGHT, WIDTH, 1)).astype(np.float32)
    base += grain * floor
    image = Image.fromarray(np.clip(base, 0, 255).astype(np.uint8))
    draw = ImageDraw.Draw(image)
    for row in range(7):
        y = 319 + row * row * 2 + row * 6
        draw.line([(0, y), (WIDTH, y)], fill=(15 + row, 10 + row, 9), width=1)
        for col in range(-7, 8):
            x = 504 + col * (36 + row * 11) + (row % 2) * 24
            draw.line([(x, y), (x + col * 4, y + 16 + row * 4)], fill=(18, 12, 9))
    for _ in range(180):
        x = float(RNG.normal(505, 107))
        y = float(RNG.normal(365, 12))
        r = float(RNG.uniform(1, 4))
        draw.ellipse((x-r, y-r*.5, x+r, y+r*.5), fill=(24, 18, 14))
    return image


BASE = ground()
LOGS = [((466, 351), (519, 231), 12), ((555, 353), (498, 230), 13),
        ((433, 358), (496, 215), 11), ((586, 359), (522, 221), 12),
        ((481, 363), (508, 243), 14), ((541, 366), (523, 240), 14)]


def log(draw, start, end, radius, phase, front=False):
    a, b = np.array(start, dtype=float), np.array(end, dtype=float)
    vector = b - a
    direction = vector / np.linalg.norm(vector)
    normal = np.array([-direction[1], direction[0]])
    points = [tuple(a - normal*radius), tuple(b - normal*radius*.65),
              tuple(b + normal*radius*.65), tuple(a + normal*radius)]
    draw.polygon(points, fill=(18, 12, 9) if front else (24, 14, 9))
    for strand in range(5):
        offset = (strand - 2) * radius / 3
        p, q = a + normal*offset, b + normal*offset*.65
        draw.line([tuple(p), tuple(q)], fill=(36 + strand*2, 22, 13), width=1)
    # Lit edge stays outside the dark, recognizable silhouette of the wood.
    edge = -1 if (a[0] < 510) else 1
    p, q = a + normal*radius*edge, b + normal*radius*.65*edge
    strength = flicker(phase)
    draw.line([tuple(p), tuple(q)], fill=(int(202*strength), int(76*strength), 14), width=2)
    draw.ellipse((start[0]-radius, start[1]-radius*.5,
                  start[0]+radius, start[1]+radius*.5), fill=(36, 17, 9))


def fire_field(time):
    phase = math.tau * time / DURATION
    turbulence = np.zeros_like(X)
    warp = np.sin(UP*.036 - phase*3) * 6 + np.sin(UP*.073 + phase*5) * 3
    for i, grid in enumerate(NOISE):
        frequency = .027 * 2**i
        turbulence += (noise(grid, (X+warp)*frequency, UP*frequency - 16*2**i*time/DURATION) - .5) * .5**i
    envelope = np.full_like(X, -2)
    for tongue in range(9):
        origin = 504 + (tongue-4)*13
        height = 230 + 45*math.sin(tongue*2.3) + 22*math.sin(phase*(2+tongue%3)+tongue)
        h = np.maximum(UP / height, 0)
        sway = np.sin(h*7 - phase*(2+tongue%3) + tongue*1.7) * (6+18*h)
        sway += np.sin(h*13 + phase*3 + tongue) * 6*h
        width = (22 + 5*math.sin(tongue)) * np.maximum(1-h, .025)**.65 + 2
        value = 1 - ((X-origin-sway) / width)**2 - h**1.6
        envelope = np.maximum(envelope, value)
    density = envelope + turbulence*.96
    alpha = np.clip((density+.1)*3, 0, 1) * np.clip(UP/14, 0, 1)
    alpha *= (UP > 0)
    heat = np.clip(density*.72 + .13 + np.maximum(0,1-UP/300)*.18, 0, 1) * .80
    # Black body inspired palette: dark red -> orange -> yellow -> pale core.
    stops = np.array([0, .18, .4, .62, .82, 1])
    colors = np.array([[155,25,2],[225,55,3],[255,103,7],
                       [255,165,26],[255,214,92],[255,245,197]], dtype=np.float32)
    rgb = np.stack([np.interp(heat, stops, colors[:,c]) for c in range(3)], axis=-1)
    rgb *= alpha[...,None]
    return Image.fromarray(np.clip(rgb,0,255).astype(np.uint8)).resize((WIDTH,HEIGHT),Image.Resampling.BICUBIC)


def render(time):
    """Every animated term is periodic, including particle spawn/fade."""
    phase = math.tau * time / DURATION
    image = BASE.copy()
    draw = ImageDraw.Draw(image)
    for entry in LOGS[:2]:
        log(draw,*entry,phase)
    fire = fire_field(time)
    glow = fire.filter(ImageFilter.GaussianBlur(19))
    array = np.asarray(image,dtype=np.float32)
    array += np.asarray(glow,dtype=np.float32)*.30
    array += np.asarray(fire,dtype=np.float32)
    image = Image.fromarray(np.clip(array,0,255).astype(np.uint8))
    draw = ImageDraw.Draw(image)
    # Red-hot embers and orange cracks in the charred wood.
    for i in range(64):
        x = 505 + math.sin(i*16.34)*79
        y = 357 + math.sin(i*9.71)*9
        light = (.7+.3*math.sin(phase*(3+i%5)+i))*flicker(phase)
        r = 1.2+(i%4)*.5
        draw.ellipse((x-r,y-r*.5,x+r,y+r*.5),fill=(int(240*light),int(76*light),9))
    for entry in LOGS[2:]:
        log(draw,*entry,phase,front=True)
    # A second luminous layer softly wraps the front logs without hiding them.
    front_glow = fire.filter(ImageFilter.GaussianBlur(1.1))
    array = np.asarray(image,dtype=np.float32)+np.asarray(front_glow,dtype=np.float32)*.05
    image = Image.fromarray(np.clip(array,0,255).astype(np.uint8))
    draw = ImageDraw.Draw(image)
    for i in range(42):
        cycles = 2 + i%3
        age = (cycles*time/DURATION + i*.61803398875) % 1
        visibility = math.sin(math.pi*age)**1.7
        x = 504 + math.sin(i*6.73)*48 + math.sin(age*7+i)*18 + age*math.sin(i)*31
        y = 339 - age*(220+i%5*15)
        length = 1.5 + age*4
        color=(int(255*visibility),int((120+i%5*19)*visibility),int(46*visibility))
        if visibility>.1:
            draw.line([(x,y),(x+math.sin(i)*1.2,y+length)],fill=color,width=1+(i%7==0))
    return image


def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--preview',type=Path,help='Save one generated frame and skip encoding')
    args=parser.parse_args()
    if args.preview:
        render(1.25).save(args.preview)
        print(args.preview)
        return
    OUTPUT.parent.mkdir(parents=True,exist_ok=True)
    command=['ffmpeg','-hide_banner','-loglevel','error','-y','-f','rawvideo','-pix_fmt','rgb24',
             '-s',f'{WIDTH}x{HEIGHT}','-r',str(FPS),'-i','-','-an','-c:v','libx264',
             '-profile:v','baseline','-level:v','3.1','-pix_fmt','yuv420p','-crf','19',
             '-g',str(FPS),'-movflags','+faststart','-map_metadata','-1',
             '-metadata','title=Campfire - CinemaMap original procedural loop',str(OUTPUT)]
    process=subprocess.Popen(command,stdin=subprocess.PIPE)
    try:
        for frame in range(FPS*DURATION):
            process.stdin.write(render(frame/FPS).tobytes())
    finally:
        process.stdin.close()
    if process.wait()!=0:
        raise SystemExit('ffmpeg could not encode the campfire')
    print(f'Created {OUTPUT} ({OUTPUT.stat().st_size:,} bytes)')


if __name__=='__main__':
    main()
