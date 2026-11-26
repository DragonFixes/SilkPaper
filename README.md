<div align="center">
  <h1 align="center">SilkPaper 26.3</h1>
  <h3 align="center">Fabric hybrid server running Paper, Purpur and Leaf plugins alongside Fabric mods</h3>

  <p align="center">
    <a href="https://fabricmc.net/">
      <img alt="Fabric" src="https://img.shields.io/badge/Fabric-26.3-FF8B00">
    </a>
    <a href="https://www.azul.com/downloads/?version=java-25-lts#zulu">
      <img alt="JDK" src="https://img.shields.io/badge/JDK-25-007396?logo=java&logoColor=white">
    </a>
    <a href="https://docs.gradle.org/9.8.1/release-notes.html">
      <img alt="Gradle" src="https://img.shields.io/badge/Gradle-9.8.1-02303A?logo=gradle&logoColor=white">
    </a>
  </p>
</div>

SilkPaper is a fork of [Silkard](https://github.com/MohistMC/Silkard), MohistMC's Fabric server
that implements the Bukkit/Spigot API. SilkPaper adds the Paper API and server code on top,
together with the Purpur, Gale and Leaf layers, so plugins written for Paper and its forks run
next to Fabric mods.

SilkPaper is not Silkard, Paper, Purpur or Leaf, and is not supported by their teams. Report
SilkPaper problems in [this repository's issues](https://github.com/DragonFixes/SilkPaper/issues).

> [!WARNING]
> SilkPaper is in active development. The Paper port boots and runs, but several Paper and
> Leaf features still need vanilla patches that Silkard does not have (see
> [Not yet supported](#-not-yet-supported)). Test on a copy of your world first.

## 🚀 Features

- Fabric mods and Bukkit, Spigot and Paper plugins on one server
- Paper API, including Brigadier commands, the lifecycle event API and registry API
- Purpur, Gale and Leaf APIs and their configuration files
- Purpur commands: `/tpsbar`, `/rambar`, `/ram`, `/ping`, `/uptime`, `/compass`, `/credits`, `/demo`
- Paper's `/version`, `/tps` and `/mspt`, and the bundled [spark](https://spark.lucko.me/) profiler
- Paper's watchdog, with thread dumps when the server stops responding
- Silkard extras: `/silkard`, vanish, `/hat`, `/openinv`, warps and back locations in SQLite or MySQL

## 🧩 Upstream versions

| Layer | Source | Synced at |
|---|---|---|
| Silkard | [MohistMC/Silkard](https://github.com/MohistMC/Silkard) `26.3` | merged into `develop` |
| Bukkit API | [Spigot](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit) | [`29025a7e`](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/commits/29025a7e) |
| CraftBukkit | [Spigot](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit) | [`e89124d3e`](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/commits/e89124d3e) |
| Spigot | [Spigot](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot) | [`bed90fca`](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/commits/bed90fca) |
| Paper | [PaperMC/Paper](https://github.com/PaperMC/Paper) | [`df120fd8`](https://github.com/PaperMC/Paper/commit/df120fd8b5211f61146a59f69497a5a62064b917) |
| Purpur, Gale, Leaf | [Winds-Studio/Leaf](https://github.com/Winds-Studio/Leaf) `ver/26.3` | [`c2c8942c`](https://github.com/Winds-Studio/Leaf/commit/c2c8942cb39c21828dc351ae7d858da77e4b53d7) |

The Paper/Purpur/Leaf API commits are recorded in `paper-api-upstream.properties` by
`scripts\sync-leaf-api`.

## 📁 Configuration files

| Location | Files |
|---|---|
| Server root | `server.properties`, `bukkit.yml`, `spigot.yml`, `commands.yml`, `help.yml` |
| `paper-config/` | `paper-global.yml`, `paper-world-defaults.yml`, `purpur.yml`, `gale-global.yml`, `gale-world-defaults.yml`, `leaf-global.yml` |
| Each world folder | `paper-world.yml`, `gale-world.yml` (per-world overrides) |
| `silkard-config/` | `silkard.yml`, `database.yml`, `worlds.yml` |
| `config/` | Fabric mod configs only |

Paper, Purpur, Gale and Leaf normally keep their files in `config/`. SilkPaper moves them to
`paper-config/` because Fabric mods use `config/`. On first start, existing Paper, Gale and
Leaf files in `config/` (and a root `purpur.yml`) are moved to `paper-config/` automatically.

The `silkard-config/` folder, `silkard.yml` and the `/silkard` command keep Silkard's names, so
existing Silkard servers can switch without renaming anything.

## 🚧 Not yet supported

These options are read from the config but forced off, with a warning on startup:

| Option (`leaf-global.yml`) | Why |
|---|---|
| `async.async-pathfinding.enabled` | needs Leaf's vanilla pathfinding patches |
| `async.async-playerdata-save.enabled` | needs Leaf's vanilla player data patches |
| `async.async-entity-tracker.enabled` | needs Leaf's vanilla entity tracker patches |
| `async.parallel-world-ticking.enabled` | needs SparklyPaper's vanilla world ticking patches |
| `network.protocol-support.do-a-barrel-roll-protocol` | needs Leaf's vanilla network patches |

Paper's anti-xray engine is not included, because it depends on vanilla chunk packet patches.

## 🛠️ Building and running

Requires JDK 25. From PowerShell or `cmd` in the repository root:

```powershell
scripts\build-deploy.bat            # build, then copy the server jar to deploy\
scripts\run-server.bat -AcceptEula  # run it in deploy\server (accepts the Minecraft EULA)
scripts\boot-test.bat               # boot once, run console commands, stop, report problems
```

The jar is `deploy\silkpaper-<minecraft version>-<commit>-server.jar`. Put Fabric mods in
`mods\` and plugins in `plugins\` inside the server folder.

| Script | What it does |
|---|---|
| `build-deploy` | Deletes `deploy\*.jar`, runs `gradlew jar`, copies the server jar into `deploy\` (`-Clean`, `-DryRun`) |
| `run-server` | Runs the newest jar in `deploy\` in a server folder (`-Memory`, `-Gui`, `-ServerDir`, `-AcceptEula`, `-Java`) |
| `boot-test` | Startup smoke test: boots, types console commands, stops, fails on errors, crashes or stalls (`-Fresh`, `-Commands`) |
| `game-test` | Runs the in-server gameplay tests in `gametests\packs` on a flat world, fails if one fails or the server logs an error (`-Tests`, `-KeepWorld`) |
| `sync-upstream` | Fetches MohistMC/Silkard, updates the `26.3` mirror branch, reports drift, optionally merges (`-Merge`, `-NoFetch`, `-DryRun`) |
| `sync-leaf-api` | Re-vendors the Paper/Purpur/Leaf API from a Leaf checkout and re-applies `patches\paper-api` |
| `clean` | `gradlew clean`; `-Deploy` also deletes the deployed jars |

Every script is a `.ps1` with a `.bat` launcher beside it. Run
`Get-Help .\scripts\<name>.ps1 -Full` for all options.

## ⚙️ Technical stack

| Component | Source | Purpose |
|---|---|---|
| Fabric | [FabricMC/fabric-api](https://github.com/FabricMC/fabric-api) | Mod support |
| Silkard | [MohistMC/Silkard](https://github.com/MohistMC/Silkard) | Bukkit/Spigot on Fabric |
| Bukkit, CraftBukkit, Spigot | [SpigotMC](https://hub.spigotmc.org/stash/projects/SPIGOT) | Plugin support |
| Paper | [PaperMC/Paper](https://github.com/PaperMC/Paper) | Paper API and server features |
| Purpur, Gale, Leaf | [Winds-Studio/Leaf](https://github.com/Winds-Studio/Leaf) | Fork APIs, configs and optimizations |
| Arclight | [IzzelAliz/Arclight](https://github.com/IzzelAliz/Arclight) | Fabric installer and mixin tools |

## 📜 License

SilkPaper keeps the licenses of the projects it builds on; see [LICENSE](LICENSE).

## 🙏 Credits

SilkPaper is built on [Silkard](https://github.com/MohistMC/Silkard) by
[MohistMC](https://github.com/MohistMC). Silkard did the hard part of running the Bukkit and
Spigot API on Fabric, and SilkPaper would not exist without it. Thanks also to the
[Paper](https://github.com/PaperMC/Paper), [Purpur](https://github.com/PurpurMC/Purpur),
[Gale](https://github.com/Dreeam-qwq/Gale), [Leaf](https://github.com/Winds-Studio/Leaf),
[Spigot](https://www.spigotmc.org/) and [Fabric](https://fabricmc.net/) teams, whose code
SilkPaper ports and runs.
