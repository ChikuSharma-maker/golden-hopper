# Golden Hopper (Fabric, Minecraft 26.x)

## Recipe
5 gold ingots + 1 chest, same layout as the vanilla hopper:

    G _ G
    G C G
    _ G _

## Using it
Right-click the Golden Hopper to open its screen:
- **Filter (7 slots, gold border):** pick up an item with your cursor, then click a filter slot. The item is NOT used up.
  Click a filter slot with an empty cursor to clear it.
- **Speed - / +:** x0.25, x0.5, x1 (vanilla), x2 (default), x4, x8.
- **Inverted button:**
  - OFF: only the selected items can enter (nothing selected = everything enters)
  - ON: everything enters EXCEPT the selected items
- The hopper's 5 normal item slots and your inventory are below.

Settings are saved with the world (they are not kept when the block is broken).

## Build
Use GitHub Actions (.github/workflows/build.yml) or locally with JDK 25:
    gradle wrapper --gradle-version 9.5.1
    ./gradlew build
