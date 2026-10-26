# Contributing to Noteworthy

Thanks for wanting to help with Noteworthy! This is a personal project, so please read this page before opening an issue or a pull request.

## Ways to contribute

- **Report a bug**: open an issue with the steps to reproduce it, your loader (Forge, NeoForge or Fabric), the mod version and your logs if the game crashed.
- **Suggest an idea**: open an issue and explain what you would like to see and why.
- **Translate the mod**: see [Translations](#translations).
- **Contribute code**: see [Before you start](#before-you-start) and [Pull requests](#pull-requests).

## Before you start

For anything bigger than a small fix, open an issue first so we can talk about it. That way you don't spend time on something that won't be merged.

## Setting up

- JDK 25
- Gradle 9.7 or newer

There is no root build, each loader folder is a separate Gradle project:

```
gradle -p forge build
gradle -p fabric build
gradle -p neoforge build
```

## Code style

- Follow the style of the code around your change (naming, formatting, structure).
- Keep the header at the top of every Java file (`File`, `Author`, `Created`, `Last Modified`) and update `Last Modified` when you edit a file.
- Shared code goes in `src/`. Each loader only has its own small package to boot the mod (`org.furranystudio.noteworthy.forge`, `.neoforge` and `.fabric`), so a change must work on all 3 loaders.

## Translations

Translations are done with pull requests on the language files in `src/main/resources/assets/noteworthy/lang/`.

- To add a language, copy `en_us.json`, rename it with the [Minecraft language code](https://minecraft.wiki/w/Language) (for example `de_de.json`) and translate the values. Don't change the keys.
- To fix a translation, edit the line in the existing file.

## Assets

Source textures and models go in `art/`. Any asset you submit must be your own work, or under a license that allows it to be used in this project (tell us which one in your pull request).

## Pull requests

- One topic per pull request.
- It must build on all 3 loaders.
- Explain what you changed and what you tested in game.

## AI policy

**AI generated contributions are not accepted.** This covers everything: code, textures, models, sounds, translations and texts.

If a pull request or an issue looks AI generated, it will be closed. Contribute only what you wrote and understand yourself.

## License

By contributing, you agree that your contribution is released under the [MIT License](LICENSE), like the rest of the project.
