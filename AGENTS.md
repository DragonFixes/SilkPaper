> **Note:** This file was written specifically for Claude (Claude Code), so
> some wording may not fit other agents exactly — e.g. Claude-specific tool
> names, skills, or features. Interpret those in terms of your own equivalents.
>
> The master copy of the global rules these instructions build on lives at
> `{USER HOME}/.claude/CLAUDE.md` (e.g. `C:\Users\<user>\.claude\CLAUDE.md`).
> Read it as well; rules there apply unless this file overrides them.

# SilkPaper: instructions for coding agents

## What this repository is

SilkPaper is a fork of [Silkard](https://github.com/MohistMC/Silkard) (MohistMC's Fabric server
implementing the Bukkit/Spigot API) with the Paper, Purpur, Gale and Leaf server code ported on
top. Minecraft 26.3, Fabric, Java 25.

Silkard has no vanilla source patches: everything that Paper does with a patch to a Minecraft
class is done here with a **mixin** (`projects/silkard/src/main/java/com/mohistmc/silkard/mixin/`).
When ported Paper code expects a vanilla hook (a registry registered, a thread type, a call at
startup or shutdown), look for the Paper patch in a Paper checkout (for example
`C:\Development\Java\ServerSoftware\FastPaper\paper-server\patches\sources\`) and reproduce it
as a mixin.

| Path | What |
|---|---|
| `src/main/java/com/mohistmc/launcher/` | Launcher jar (Fabric installer, game provider) |
| `projects/silkard/` | The server mod (Gradle project `:server`) |
| `projects/silkard/src/main/java/com/mohistmc/silkard/mixin/` | Mixins standing in for CraftBukkit/Paper vanilla patches |
| `projects/silkard/src/main/java/org/bukkit/craftbukkit/` | CraftBukkit, merged with Paper's changes |
| `projects/silkard/src/main/java/io/papermc/`, `org/purpurmc/`, `org/galemc/`, `org/dreeam/leaf/` | Ported Paper, Purpur, Gale, Leaf server code |
| `projects/silkard/src/paper-api/java/` | Vendored Paper + Purpur + Leaf API (do not edit directly) |
| `patches/paper-api/` | SilkPaper's changes to the vendored API, re-applied by `sync-leaf-api` |
| `paper-api-upstream.properties` | Leaf and Paper commits the API was synced from |
| `libs/maven/` | Bundled spark (`spark-paper`, `spark-api`) built from the [DragonFixes spark fork](https://github.com/DragonFixes/spark) by `sync-spark`; versions end in the fork commit and are set in `gradle.properties`. Do not edit by hand |
| `projects/silkard/src/main/java/com/mohistmc/silkard/gametest/` | In-server gameplay tests and their runner (see `gametests/README.md`) |
| `gametests/` | Test datapack (`test_instance` and structures) for the gameplay tests |
| `scripts/` | Helper scripts (see below) |
| `deploy/` | Built jar and test servers from the scripts (git-ignored) |

## Branches and remotes

| Name | What it is |
|---|---|
| `develop` | Active SilkPaper development and the main branch. Work and commit here. |
| `26.3` | Mirror of `upstream/26.3` (MohistMC/Silkard). Only fast-forward it; never commit to it. |
| remote `origin` | `git@github.com:DragonFixes/SilkPaper.git` |
| remote `upstream` | `https://github.com/MohistMC/Silkard.git` |

Never push to `upstream`. Push to `origin` only when the user asks.

Pull in Silkard updates with `scripts\sync-upstream.bat -Merge upstream/26.3`, then build and run
`scripts\boot-test.bat` before committing.

## Naming

The project is **SilkPaper**. Use that name in user-facing text (README, console messages,
version output, the jar name, watchdog messages). Java packages (`com.mohistmc.silkard.*`), the
`silkard` mod id, `silkard.yml` / `silkard-config/`, the `/silkard` command, the `SilkardBackups`
folder and the bundled inner jar path (`META-INF/libraries/com/mohistmc/silkard/...`, looked up by
`SilkardGameProvider`) keep their Silkard names so existing servers keep working. Do not rename them.

The brand (`ServerBuildInfo.brandName()`, `Bukkit.getName()`) comes from `ServerBuildInfoImpl`.
Keep it there, not in the vendored API.

## Helper scripts

Each script in `scripts/` is a `.ps1` (all logic) with a `.bat` launcher that forwards its
arguments. Run them from any directory; paths resolve from the repository root.

The build and the server need Java 25+. Scripts that run Gradle or the server pick a JDK with
`scripts\lib\JavaHome.ps1`: `JAVA_HOME` if it is 25+, else the `java` on PATH, else the newest
installed JDK 25 (`{USERPROFILE}\.jdks`, `Program Files\Java`, Adoptium, Zulu, ...). They set
`JAVA_HOME` for Gradle themselves, so they work from a shell where `java` is an older JDK.

| Script | What it does |
|---|---|
| `build-deploy` | Deletes `deploy\*.jar`, runs `gradlew jar`, copies `build\libs\*-server.jar` into `deploy\` |
| `run-server` | Runs the newest jar in `deploy\` in a server folder (default `deploy\server`) |
| `boot-test` | Boots the deployed jar in `deploy\boot-test`, types console commands after startup, stops, and fails on ERROR lines, exceptions, crash reports or a stall |
| `game-test` | Runs the gameplay tests in `gametests\packs` on the deployed jar in `deploy\game-test` (flat world), and fails if a required test fails or the server logs an error |
| `sync-upstream` | Fetches `upstream` and `origin`, fast-forwards the `26.3` mirror, reports drift, optionally merges a ref (never pushes) |
| `sync-leaf-api` | Re-vendors the Paper/Purpur/Leaf API from a Leaf checkout and re-applies `patches\paper-api` |
| `sync-spark` | Builds the bundled spark from the spark fork and vendors it into `libs\maven` |
| `clean` | `gradlew clean`; `-Deploy` also deletes the deployed jars |

```powershell
scripts\build-deploy.bat                 # build and deploy
scripts\build-deploy.bat -Clean          # gradle clean first
scripts\build-deploy.bat -DryRun         # show what would happen

scripts\run-server.bat -AcceptEula       # first run
scripts\run-server.bat -Memory 8G
scripts\run-server.bat -ServerDir C:\Servers\test -Gui

scripts\boot-test.bat                                  # default commands: version, tps, ram, uptime
scripts\boot-test.bat -Fresh                           # newly generated world and configs
scripts\boot-test.bat -Commands "plugins,mspt" -CommandDelaySeconds 5
scripts\boot-test.bat -MixinAudit                      # also apply every mixin, not only startup classes
.\scripts\boot-test.ps1 -Commands (Get-Content scripts\purpur-smoke.txt) -CommandDelaySeconds 5
                                                       # place ticking blocks, wait 20 s, catch runtime throws
.\scripts\boot-test.ps1 -Commands (Get-Content scripts\death-smoke.txt) -CommandDelaySeconds 5
                                                       # kill mobs; each "Test passed. Count: 1" means drops survived
.\scripts\boot-test.ps1 -Commands (Get-Content scripts\ride-smoke.txt) -CommandDelaySeconds 5
                                                       # mount/dismount a minecart and pop a totem; every line should say "Test passed"

scripts\game-test.bat                                  # every gameplay test
scripts\game-test.bat -Tests "silkpaper:poi_*"         # one group (a single selector)
scripts\game-test.bat -Tests "silkpaper:hopper*" -KeepWorld

scripts\sync-upstream.bat                              # fetch, update mirror, report drift
scripts\sync-upstream.bat -Merge upstream/26.3         # merge Silkard into the current branch
scripts\sync-upstream.bat -Merge upstream/26.3 -Force  # skip the confirmation prompt

scripts\sync-leaf-api.bat -Clone                       # clone Leaf ver/26.3 and re-vendor the API

scripts\sync-spark.bat                                 # build ..\..\Fixes\Spark and vendor it
scripts\sync-spark.bat -SparkDir D:\src\spark -Force   # another checkout, no prompt
scripts\sync-spark.bat -DryRun

scripts\clean.bat -DryRun
scripts\clean.bat -Deploy
```

## Verifying a change

A successful compile does not mean the server works: much of the ported Paper code only fails
at runtime (missing vanilla hooks, missing jar-in-jar libraries). After any server change:

1. `scripts\build-deploy.bat`
2. `scripts\boot-test.bat` (add `-Fresh` when world generation or configs are involved, and pass
   the commands the change touches with `-Commands`). After adding or changing mixins, add
   `-MixinAudit`. A mixin is only applied when its target class loads, and a plain boot never
   loads many classes (pistons, AI behaviours, most items). The audit loads every target and logs
   `Mixin audit: <class> failed: ...` for each injection that no longer matches. Give injectors
   that list several methods an exact `require = N`, or the audit passes when only one matches.
   A mixin that applies can still throw at runtime. `scripts\purpur-smoke.txt` places lava,
   water, observers, coral, crops, a piston and more, then waits 20 seconds with random ticks
   sped up, so hooks on those paths run. Add a line for each block or item a change touches. In
   `-Commands`, a comma separates commands, so a block state takes one property per line.
3. `scripts\game-test.bat` when the change touches gameplay (entities, AI, blocks, selectors,
   tracking, players joining and leaving). The tests run vanilla's GameTest framework inside the
   server, with fake players joining through the real login path; see `gametests/README.md` for
   adding one. They have caught mixins that applied cleanly but broke behaviour (water cauldrons
   that never extinguished, plugin ally rules that were ignored, players removed twice on kick).

`boot-test` uses a server folder with no plugins, so it does not catch plugin-compatibility
breakage. For that, `test-servers\drakgg\smp` is a good test folder: a copy of a real SMP with
about 70 Paper plugins (FAWE, WorldGuard, mcMMO, LuckPerms, nightcore plugins, CMILib, Worlds and
more). Copy the deployed jar in and run it there, for example
`scripts\run-server.bat -ServerDir C:\Development\Java\ServerSoftware\SilkPaper\test-servers\drakgg\smp`,
then check its `logs\latest.log` for plugin errors. `test-servers\` and `test-logs\` (logs the user
shares from servers) are git-ignored; never commit them.

`boot-test` checks the log every 15 seconds. On a stall it saves a `jstack` dump to
`deploy\boot-test-threads-<time>.txt` before killing the server; read it to see where the server
is blocked. A shutdown stall after "Saving chunks for level ... the_end" or "Saving worlds" has been
seen twice, both on freshly generated worlds stopped within a second of "Done"; it has not
been reproduced with a dump yet.

Jar-in-jar (`include(...)` in `projects/silkard/build.gradle`) is not transitive: a library's own
runtime dependencies must be listed too, or classes are missing only at runtime.

## Commits

Follow the global rules in `{USER HOME}/.claude/CLAUDE.md` for commit message format and
committing after each change. Commit on `develop`.
