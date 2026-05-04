#!/usr/bin/env python3
"""
Export icons from SVG/XML to PNG for presentation slides
Requires: cairosvg or convert (ImageMagick)
"""

import os
import sys
import subprocess
from pathlib import Path

# Project paths
PROJECT_ROOT = Path(__file__).parent.parent
DRAWABLE_DIR = PROJECT_ROOT / "app/src/main/res/drawable"
OUTPUT_DIR = PROJECT_ROOT / "docs/presentation_icons"

# Icons to export with their target sizes
ICONS_TO_EXPORT = {
    "ic_launcher_foreground.xml": [
        ("app_icon", 512),  # For high-res presentation
        ("app_icon_medium", 256),
        ("app_icon_small", 128)
    ],
    "ic_microphone_large.xml": [
        ("mic_button", 512),
        ("mic_button_medium", 256)
    ],
    "ic_launcher_monochrome.xml": [
        ("app_icon_mono", 512)
    ]
}

def convert_xml_to_svg(xml_path, svg_path):
    """Convert Android vector XML to SVG"""
    # Basic conversion - Android VectorDrawable to SVG
    with open(xml_path, 'r') as f:
        content = f.read()

    # Simple XML to SVG conversion
    svg_content = content.replace('android:fillColor=', 'fill=')
    svg_content = svg_content.replace('android:pathData=', 'd=')
    svg_content = svg_content.replace('android:strokeColor=', 'stroke=')
    svg_content = svg_content.replace('android:strokeWidth=', 'stroke-width=')
    svg_content = svg_content.replace('android:width=', 'width=')
    svg_content = svg_content.replace('android:height=', 'height=')
    svg_content = svg_content.replace('android:viewportWidth=', 'viewBox="0 0 ')
    svg_content = svg_content.replace('android:viewportHeight=', '')
    svg_content = svg_content.replace('<vector', '<svg')
    svg_content = svg_content.replace('</vector>', '</svg>')

    with open(svg_path, 'w') as f:
        f.write(svg_content)

def export_with_imagemagick(input_file, output_file, size):
    """Export using ImageMagick convert command"""
    try:
        cmd = [
            'convert',
            '-background', 'none',
            '-density', '300',
            '-resize', f'{size}x{size}',
            str(input_file),
            str(output_file)
        ]
        subprocess.run(cmd, check=True, capture_output=True)
        return True
    except (subprocess.CalledProcessError, FileNotFoundError):
        return False

def export_with_cairosvg(svg_path, png_path, size):
    """Export using cairosvg Python library"""
    try:
        import cairosvg
        cairosvg.svg2png(
            url=str(svg_path),
            write_to=str(png_path),
            output_width=size,
            output_height=size
        )
        return True
    except (ImportError, Exception):
        return False

def manual_export_instructions():
    """Print manual export instructions"""
    print("\n" + "="*60)
    print("INSTRUKCJE RĘCZNEGO EKSPORTU IKON")
    print("="*60)
    print("\nMetoda 1: Użyj Android Studio")
    print("-" * 60)
    print("1. Otwórz Android Studio")
    print("2. W Project view, znajdź plik ikony w res/drawable/")
    print("3. Kliknij prawym przyciskiem na plik XML")
    print("4. Wybierz 'Open in Asset Studio' lub 'Show in Finder'")
    print("5. W podglądzie, zrób screenshot lub użyj 'Export'")
    print()

    print("Metoda 2: Użyj narzędzia online")
    print("-" * 60)
    print("1. Otwórz: https://www.vectorizer.io/ lub https://convertio.co/")
    print("2. Skopiuj zawartość pliku XML")
    print("3. Wklej do konwertera SVG/PNG")
    print("4. Pobierz PNG w żądanej rozdzielczości")
    print()

    print("Metoda 3: Użyj macOS Preview (dla SVG)")
    print("-" * 60)
    print("1. Otwórz plik w Preview")
    print("2. File → Export")
    print("3. Wybierz format PNG")
    print("4. Ustaw rozdzielczość (300 DPI lub wyżej)")
    print()

    print("Metoda 4: Zainstaluj ImageMagick")
    print("-" * 60)
    print("Uruchom w terminalu:")
    print("  brew install imagemagick")
    print("Potem uruchom ponownie ten skrypt.")
    print()

    print("="*60)
    print("\nPLIKI IKON DO WYEKSPORTOWANIA:")
    print("-" * 60)
    for icon_file in ICONS_TO_EXPORT.keys():
        full_path = DRAWABLE_DIR / icon_file
        print(f"  📄 {full_path}")
    print()

    print("ZALECANE ROZMIARY DLA PREZENTACJI:")
    print("-" * 60)
    print("  • 512x512 px - dla slajdów tytułowych i dużych grafik")
    print("  • 256x256 px - dla standardowych elementów na slajdach")
    print("  • 128x128 px - dla małych ikon i ozdobników")
    print()

    print(f"FOLDER WYJŚCIOWY:")
    print(f"  📁 {OUTPUT_DIR}")
    print()

def create_simple_png_export():
    """Create simple PNG exports manually via screenshot instructions"""
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    readme = OUTPUT_DIR / "README.md"
    with open(readme, 'w') as f:
        f.write("# Ikony dla prezentacji\n\n")
        f.write("## Źródła ikon\n\n")
        for icon_file, exports in ICONS_TO_EXPORT.items():
            source = DRAWABLE_DIR / icon_file
            f.write(f"### {icon_file}\n")
            f.write(f"Źródło: `{source}`\n\n")
            f.write("Wyeksportuj w rozmiarach:\n")
            for name, size in exports:
                f.write(f"- **{name}.png** ({size}x{size} px)\n")
            f.write("\n")

        f.write("\n## Jak wyeksportować\n\n")
        f.write("1. **Android Studio:**\n")
        f.write("   - Otwórz plik XML w Android Studio\n")
        f.write("   - Podgląd ikony pojawi się po prawej stronie\n")
        f.write("   - Zrób screenshot lub użyj 'Open in Asset Studio'\n\n")

        f.write("2. **Online converter:**\n")
        f.write("   - https://www.vectorizer.io/\n")
        f.write("   - https://convertio.co/svg-png/\n")
        f.write("   - Skopiuj zawartość XML i przekonwertuj\n\n")

        f.write("3. **Zapisz w tym folderze** z odpowiednimi nazwami\n\n")

    print(f"✅ Utworzono folder: {OUTPUT_DIR}")
    print(f"✅ Utworzono plik README: {readme}")

def main():
    print("🎨 Eksport ikon dla prezentacji QuickTalk 5G")
    print("="*60)

    # Create output directory
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    # Check for available tools
    tools_available = []

    # Check ImageMagick
    try:
        subprocess.run(['convert', '--version'],
                      capture_output=True, check=True)
        tools_available.append('imagemagick')
        print("✅ ImageMagick znaleziony")
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("❌ ImageMagick nie znaleziony")

    # Check cairosvg
    try:
        import cairosvg
        tools_available.append('cairosvg')
        print("✅ cairosvg znaleziony")
    except ImportError:
        print("❌ cairosvg nie znaleziony")

    if not tools_available:
        print("\n⚠️  Brak narzędzi do automatycznego eksportu.")
        create_simple_png_export()
        manual_export_instructions()
        return

    # Export icons
    print(f"\n📤 Eksportowanie ikon do: {OUTPUT_DIR}\n")

    for icon_file, exports in ICONS_TO_EXPORT.items():
        source_xml = DRAWABLE_DIR / icon_file

        if not source_xml.exists():
            print(f"⚠️  Pominięto: {icon_file} (nie znaleziono)")
            continue

        print(f"📄 Przetwarzanie: {icon_file}")

        for name, size in exports:
            output_png = OUTPUT_DIR / f"{name}.png"

            # Try ImageMagick first (better quality)
            if 'imagemagick' in tools_available:
                if export_with_imagemagick(source_xml, output_png, size):
                    print(f"   ✅ {name}.png ({size}x{size})")
                    continue

            print(f"   ⚠️  Nie udało się wyeksportować {name}.png")

    create_simple_png_export()
    print("\n" + "="*60)
    print("✅ Eksport zakończony!")
    print(f"📁 Pliki znajdują się w: {OUTPUT_DIR}")
    print("="*60)

if __name__ == "__main__":
    main()

