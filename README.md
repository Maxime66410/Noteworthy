# Noteworthy

Noteworthy is a Minecraft mod for Forge, NeoForge and Fabric that adds a jukebox playing note block songs. The songs are simple JSON files loaded from datapacks, so anyone can add their own without touching the code.

## What's in the mod

**The Noteworthy Jukebox**
Craft it like a vanilla jukebox, with a netherite ingot in the middle instead of the diamond. Right click it to open its menu, or sneak + right click with an empty hand to play the next song without opening anything. It keeps playing where it was after you leave and come back, and sends out note particles while it plays.

**The menu**
- A list of every loaded song with its author and length, a search bar, and a sort button (title, author or duration).
- ▶ to play a song, ⏸ to pause it, ⏮ / ⏭ to go back or skip, and a Stop button.
- A progress bar you can click or drag to jump anywhere in the song.
- A line showing the instruments and the number of parts of the selected song.

**Play modes**
- **Once**: stops at the end of the song.
- **Repeat**: plays the same song again.
- **List**: plays the next song in the list, then starts over.
- **Shuffle**: plays every song once in a random order before shuffling again.

**Settings**
Each jukebox has its own range (8 to 64 blocks) and volume (0 to 100 %). The sound fades with distance and goes silent at the edge of the range.

**Look at it**
Looking at a jukebox that's playing shows the song and its time under your crosshair.

## Songs

The mod comes with 16 public domain songs: Brahms' Lullaby, Can-Can, Canon in D, Eine kleine Nachtmusik, Frère Jacques, Für Elise, Greensleeves, Gymnopédie No. 1, In the Hall of the Mountain King, Jingle Bells, Korobeiniki, Morning Mood, Ode to Joy, Scarborough Fair, Turkish March and Twinkle Twinkle Little Star.

To add your own, put a JSON file in `data/<namespace>/noteworthy/songs/` in a datapack and run `/reload`. Here's a small one:

```json
{
  "title": "My Song",
  "author": "Me",
  "tempo": 4,
  "parts": [
    {
      "instrument": "harp",
      "notes": ["C4:2", "E4", "G4", "-:2", "C4+E4+G4:4"]
    }
  ]
}
```

A note is its name and octave (`C4`, `F#3`, `Bb4`...), from F#3 to F#5. `:2` makes it last 2 steps, `-` is a rest and `+` plays several notes at once. `tempo` is the number of game ticks per step (20 ticks = 1 second). A full guide of the format is coming.

## Commands (OP only)

Without `[pos]`, the commands act on the jukebox you're looking at.

| Command | What it does |
|---|---|
| `/noteworthy list` | Lists all the loaded songs |
| `/noteworthy play <song> [pos]` | Plays a song |
| `/noteworthy stop [pos]` | Stops the jukebox |
| `/noteworthy pause [pos]` | Pauses the song |
| `/noteworthy resume [pos]` | Resumes the song |
| `/noteworthy next [pos]` | Plays the next song |
| `/noteworthy previous [pos]` | Goes back to the start of the song, or to the previous one |
| `/noteworthy mode <once\|repeat\|list\|shuffle> [pos]` | Changes the play mode |
| `/noteworthy sort <title\|author\|duration> [pos]` | Changes the sort |
| `/noteworthy range <8-64> [pos]` | Changes the range in blocks |
| `/noteworthy volume <0-100> [pos]` | Changes the volume in percent |

## Versions

- Minecraft 26.3
- Forge 66.1.0
- NeoForge 26.3.0.95
- Fabric Loader 0.20.1 / Fabric API 0.165.0+26.3, Fabric API needs to be installed with the mod

## Project structure

All 3 loaders use the same code in `src/`, each one just has its own small package to boot the mod (`org.furranystudio.noteworthy.forge`, `.neoforge` and `.fabric`).

```
forge/ neoforge/ fabric/     one gradle project per loader
src/main/java/org/furranystudio/noteworthy/
src/main/resources/
  assets/noteworthy/         blockstates, item models and translations
  data/noteworthy/           recipe, loot table and the songs
```

## Building

There's no root build, each loader folder is a separate gradle project so you build them with `-p`:

```
gradle -p forge build
gradle -p fabric build
gradle -p neoforge build
```

You need Gradle 9.7+ running on JDK 25.

## Contributing

Want to help? Check [CONTRIBUTING.md](CONTRIBUTING.md) first. Translations are done with pull requests on the language files. If you find a security issue, please follow [SECURITY.md](SECURITY.md) instead of opening a public issue. Everyone is expected to follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## Credits

- Made by **Maxime66410** for **FurranyStudio**.

## License

Noteworthy is under the [MIT License](LICENSE).
