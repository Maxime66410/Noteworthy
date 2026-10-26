# Security Policy

## Supported versions

Only the latest published version of Noteworthy receives security fixes. If you are on an older version, please update first and check if the issue is still there.

## What counts as a security issue

| Security issue (report privately) | Regular bug (open a public issue) |
|---|---|
| A player can crash a server remotely, for example with the mod's network packets | A crash in singleplayer or on startup |
| A player without OP rights can use `/noteworthy` | A song file that doesn't load or plays wrong |
| A player can control a jukebox out of their reach, for example from far away or from another dimension | A gameplay or display problem |
| A player can run code, or read or write files on the server | |
| An issue that affects other players on a server (data, account) | |

If you are not sure, report it privately anyway.

## Reporting a vulnerability

Please **don't** report security issues in a public issue.

- **Preferred**: use GitHub's private reporting. Go to the [Security tab](https://github.com/Maxime66410/Noteworthy/security) of the repository and click **Report a vulnerability**. Only you and the maintainer can see the report.
- **Fallback**: send an email to teamfurranyfr@gmail.com.

Include the mod version, your loader (Forge, NeoForge or Fabric), and the steps to reproduce the issue.

## What to expect

- You'll get an answer within **7 days** saying the report was received.
- The report is then checked: either it's confirmed as a security issue, or you'll get an explanation of why it isn't.
- A fix is released in a new version as soon as possible. This is a personal project, so there is no guaranteed delay.
- If you want, you'll be credited in the release notes.
- Please don't make the issue public until the fix is released.
