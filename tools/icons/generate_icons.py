"""Generate all Android/iOS/store app-icon assets from the master artwork.

Usage:  python tools/icons/generate_icons.py [path-to-master.png]
Default master: C:/Users/saleh/Desktop/dyrecto assets/apple store icon.png

Outputs (repo-relative):
  android/app/src/main/res/mipmap-*/          launcher PNGs + adaptive foreground/monochrome
  iosApp/Dyrecto/Assets.xcassets/AppIcon.appiconset/AppIcon-1024.png  (alpha stripped)
  store/google-play-icon-512.png, store/app-store-icon-1024.png
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw

REPO = Path(__file__).resolve().parents[2]
MASTER = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(
    "C:/Users/saleh/Desktop/dyrecto assets/apple store icon.png")

RES = REPO / "android/app/src/main/res"
DENSITIES = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}


def save(img: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path, "PNG", optimize=True)
    print(f"  {path.relative_to(REPO)}  {img.size[0]}x{img.size[1]}")


def resized(src: Image.Image, size: int) -> Image.Image:
    return src.resize((size, size), Image.LANCZOS)


def circle_mask(img: Image.Image) -> Image.Image:
    mask = Image.new("L", img.size, 0)
    ImageDraw.Draw(mask).ellipse((0, 0, img.size[0] - 1, img.size[1] - 1), fill=255)
    out = img.copy().convert("RGBA")
    out.putalpha(mask)
    return out


def keyed_glyph(src: Image.Image) -> Image.Image:
    """Glyph with the dark background keyed to transparency (luminance-keyed:
    the background is near-black, the glyph is light grey + blue)."""
    rgba = src.convert("RGBA")
    px = rgba.load()
    w, h = rgba.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            lum = 0.299 * r + 0.587 * g + 0.114 * b
            if lum < 60 and not (b > 120 and b > r + 30):  # dark & not the blue pixel
                px[x, y] = (r, g, b, 0)
    return rgba


def glyph_bbox(rgba: Image.Image):
    return rgba.getbbox()  # alpha-aware after keying


def main() -> None:
    master = Image.open(MASTER).convert("RGB")
    print(f"master: {MASTER} {master.size}")

    # Work at 1024 for keying speed; foreground layers are <=432px anyway.
    work = resized(master, 1024)
    glyph = keyed_glyph(work)
    bbox = glyph_bbox(glyph)
    glyph_c = glyph.crop(bbox)
    print(f"glyph bbox: {bbox}")

    # ---- Android legacy launcher icons (full-bleed square + round) ----
    for d, scale in DENSITIES.items():
        size = int(48 * scale)
        icon = resized(master, size)
        save(icon, RES / f"mipmap-{d}" / "ic_launcher.png")
        save(circle_mask(icon), RES / f"mipmap-{d}" / "ic_launcher_round.png")

    # ---- Adaptive background: the artwork's vertical gradient, rebuilt from a
    # column outside the glyph and mapped so the mask-visible central 72dp spans
    # the FULL gradient range (otherwise the mask crop flattens it). ----
    mw, mh = master.size
    col_x = mw // 20  # safely left of the glyph
    for d, scale in DENSITIES.items():
        canvas_px = int(108 * scale)
        bg = Image.new("RGB", (canvas_px, canvas_px))
        inset = 18 / 108
        for y in range(canvas_px):
            t = (y / canvas_px - inset) / (1 - 2 * inset)
            t -= 0.20  # push the gradient transition lower in the tile
            t = min(1.0, max(0.0, t))
            c = master.getpixel((col_x, min(mh - 1, int(t * (mh - 1)))))
            bg.paste(c, (0, y, canvas_px, y + 1))
        save(bg, RES / f"mipmap-{d}" / "ic_launcher_background.png")

    # ---- Adaptive foreground: glyph centered in 108dp canvas. Masks show the
    # central ~72dp, and the master artwork's glyph spans ~45% of the tile, so
    # glyph = 0.45 * 72/108 = 0.30 of the canvas to match the store artwork. ----
    for d, scale in DENSITIES.items():
        canvas_px = int(108 * scale)
        target = int(canvas_px * 0.36)
        gw, gh = glyph_c.size
        f = target / max(gw, gh)
        g = glyph_c.resize((max(1, int(gw * f)), max(1, int(gh * f))), Image.LANCZOS)
        layer = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
        layer.paste(g, ((canvas_px - g.size[0]) // 2, (canvas_px - g.size[1]) // 2), g)
        save(layer, RES / f"mipmap-{d}" / "ic_launcher_foreground.png")

        # Monochrome layer (themed icons): white shape carried by alpha.
        mono = Image.new("RGBA", layer.size, (0, 0, 0, 0))
        white = Image.new("RGBA", layer.size, (255, 255, 255, 255))
        mono.paste(white, (0, 0), layer.getchannel("A"))
        save(mono, RES / f"mipmap-{d}" / "ic_launcher_monochrome.png")

    # ---- iOS single-size app icon (no alpha) ----
    ios = resized(master, 1024)
    save(ios, REPO / "iosApp/Dyrecto/Assets.xcassets/AppIcon.appiconset/AppIcon-1024.png")

    # ---- Store listings ----
    save(resized(master, 512).convert("RGBA"), REPO / "store/google-play-icon-512.png")
    save(ios, REPO / "store/app-store-icon-1024.png")


if __name__ == "__main__":
    main()
