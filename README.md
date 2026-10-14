# Aether Cosmic Client

Client-side Fabric mod project for Minecraft Java 1.21.11 / Java 21.

## Current cosmic UI update
- Cosmic starfield, nebula haze, distant planet silhouettes, and perspective grid.
- Virtual WASD camera movement plus Space/Ctrl vertical camera movement while the screen is open. This moves the UI camera only; it does not move the actual Minecraft player.
- Mouse drag to move the panel, mouse drag outside the panel to look around, left-click module toggles, right-click module settings, scroll to browse/zoom.
- Separate module categories and per-module settings labels adapted to feature type.
- GitHub Actions build workflow at `.github/workflows/build.yml` uploads the compiled JAR as `aether-cosmic-client`.

## Build
Open the repository's **Actions** tab and run **Build Aether Cosmic Client**, or push to `main`/`master`. Download the `aether-cosmic-client` artifact from a successful workflow run.

## Install
Place the built JAR in the `mods` folder of a matching Minecraft 1.21.11 Fabric installation with Fabric API installed.

## Important scope notes
This is a 2D Minecraft screen that renders a 3D-inspired, navigable space illusion; it does not replace Minecraft's renderer with a fully separate 3D world. The included module registry is a UI/module foundation, and many listed modules are prototypes rather than fully implemented gameplay features. Settings controls in this build affect the UI/module state only unless a module has an implementation. Player tracking is not a hidden-player locator and does not query server internals.
