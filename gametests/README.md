# SilkPaper gameplay tests

Vanilla GameTest-framework tests that run inside a full SilkPaper server, so they exercise the
CraftBukkit, Paper, Purpur and Leaf code a production server runs. They pin vanilla and Paper
behaviour down (hoppers, redstone, selectors, villager POI and brains, pathfinding, inside-block
effects, entity tracking, targeting, the Allies API) so a mixin that breaks it fails the run.

The tests and the fake-player harness are ported from FastPaper's gameplay-parity tests. FastPaper's
differential and benchmark tests compare its Swift optimisations with vanilla code; SilkPaper has no
Swift code, so those are left out.

```powershell
scripts\build-deploy.bat
scripts\game-test.bat                                   # every test
scripts\game-test.bat -Tests "silkpaper:poi_*"          # one group
scripts\game-test.bat -Tests "silkpaper:hopper*" -KeepWorld
```

`game-test` starts the deployed jar in `deploy\game-test` on a newly generated flat world, with
every datapack in `gametests\packs\` copied into `world\datapacks`, and passes
`-Dsilkpaper.gametest=<selector>`. Once the worlds are loaded, `SilkGameTests` runs the matching
tests at x=z=100000, writes `deploy\game-test\report.xml` (JUnit style) and the number of failed
required tests to `result.txt`, and stops the server. The script fails if any required test failed.
Without `-Dsilkpaper.gametest` the runner does nothing.

Code: `projects/silkard/src/main/java/com/mohistmc/silkard/gametest/`. Test functions are
registered by `MixinBuiltinTestFunctions` while vanilla fills the TEST_FUNCTION registry.

## Fake players

`TestPlayer` joins a fake player through the normal join path (`PlayerList#placeNewPlayer`, join
event, permissions) on an in-memory netty channel. `command("...")` writes a real
`ServerboundChatCommandPacket` into that channel, so the command takes the route a typed one takes
(packet listener, `PlayerCommandPreprocessEvent`, permission checks, `@s`/`@p` from the player).
`drainChat()` and `drainPackets()` return what the server sent. Always call
`TestPlayer.leaveAll(helper)` when the test ends (tests in a batch run side by side).

The fake player answers chunk batches as a client does. Its connection is never ticked, so move it
with `teleport(x, y, z)`, which also moves its tracked chunks along; a bare `player().teleportTo`
leaves the server tracking entities around the old position.

```java
TestPlayer op = TestPlayer.join(helper, "SilkOp", new BlockPos(2, 1, 2), true);   // op
TestPlayer guest = TestPlayer.join(helper, "SilkGuest", new BlockPos(6, 1, 6), false);
helper.runAfterDelay(1, () -> {
    try {
        guest.command("tag @s add x");          // refused: guest is not an op
        op.command("tag @p add nearest");       // @p resolved from the op's own position
    } finally {
        TestPlayer.leaveAll(helper);
    }
});
```

## Adding a test

1. Add a method to one of the test classes and register it in `SilkTestFunctions#load` as
   `silkpaper:<name>`.
2. Add `packs/silkpaper-tests/data/silkpaper/test_instance/<name>.json`:
   ```json
   { "type": "minecraft:function", "function": "silkpaper:<name>", "environment": "silkpaper:default",
     "structure": "silkpaper:platform", "max_ticks": 100, "setup_ticks": 0, "required": true }
   ```
   `silkpaper:platform` is 9x6x9 with a stone floor at relative y=0; `silkpaper:arena` is 24x24.
   For another size:
   `python gametests/tools/make_platform.py packs/silkpaper-tests/data/silkpaper/structure/<name>.nbt X Y Z`.

`silkpaper:default` freezes time at midnight (`clock_time` 18000), clears the weather and disables
mob spawning, so results do not depend on the clock. `silkpaper:day` is the same at noon. Time and
game rules are kept per world, so the `clock_time` and `game_rules` environments are redirected to
the test's level by mixins.

Gotchas (most found while building the FastPaper suite):
- `helper.setBlock(pos, Blocks.REDSTONE_WIRE)` places an unconnected dot that powers nothing
  sideways; start from the cross state (see `redstone_dust_lights_lamp`).
- 26.x structure NBT palettes use `id`, not `Name`. 26.3 entity types live in `EntityTypes`.
- Never assume which entity `limit=1` (or `execute as` order) picks: it follows the entity table's
  iteration order, which changes with every other entity in the world, other tests included.
- Selector filters run in the order written and stop at the first failure: `@e[tag=x,nbt={}]` never
  runs `nbt=` on entities without `x`, `@e[nbt={},tag=x]` does (and `nbt=` has side effects).
- Mobs far from players are frozen by entity activation range: put a fake player near any test
  that needs mobs to act.
- Villagers only walk to a workstation in their day schedule; use `silkpaper:day` for job tests.
- `-Tests` takes one selector; comma lists silently match fewer tests.
- Calling `Mob#setTarget(target)` without a reason logs "Unknown target reason"; pass
  `TargetReason.CUSTOM` from tests.
