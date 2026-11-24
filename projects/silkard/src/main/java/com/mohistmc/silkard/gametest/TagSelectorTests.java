package com.mohistmc.silkard.gametest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

/**
 * Behaviour tests for @e[tag=...] selectors, with tags changed through every channel a server
 * or plugin can use (commands, Entity#addTag/removeTag, the live Bukkit scoreboard-tag Set incl. clear,
 * removeIf and Iterator#remove, NBT load). Expected values are vanilla's answers.
 */
final class TagSelectorTests {
    private TagSelectorTests() {
    }

    static void matrix(final GameTestHelper helper) {
        final List<Mob> mobs = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            final Mob m = i % 2 == 0 ? helper.spawn(EntityTypes.PIG, new BlockPos(1 + i, 1, 1)) : helper.spawn(EntityTypes.COW, new BlockPos(1 + i, 1, 1));
            m.setNoAi(true);
            m.addTag("silk_t");
            mobs.add(m);
        }
        helper.runAfterDelay(2, () -> {
            run(helper, "scoreboard objectives add silk_test dummy");
            final StringBuilder f = new StringBuilder();
            count(helper, "@e[tag=silk_t]", 6, f);
            count(helper, "@e[tag=silk_t,type=pig]", 3, f);
            count(helper, "@e[type=cow,tag=silk_t]", 3, f);
            count(helper, "@e[tag=silk_t,limit=2]", 2, f);
            count(helper, "@e[tag=silk_t,tag=silk_u]", 0, f);
            count(helper, "@e[tag=silk_t,tag=!silk_u]", 6, f);

            // commands
            run(helper, "tag @e[tag=silk_t,type=pig] add silk_u");
            count(helper, "@e[tag=silk_u]", 3, f);
            count(helper, "@e[tag=silk_t,tag=silk_u]", 3, f);
            run(helper, "tag @e[tag=silk_u,limit=1] remove silk_u");
            count(helper, "@e[tag=silk_u]", 2, f);
            // Entity#addTag / removeTag
            mobs.get(1).addTag("silk_u");
            count(helper, "@e[tag=silk_u]", 3, f);
            mobs.get(1).removeTag("silk_u");
            count(helper, "@e[tag=silk_u]", 2, f);
            // live Bukkit set: add, remove, iterator remove, removeIf, clear
            mobs.get(3).getBukkitEntity().getScoreboardTags().add("silk_u");
            count(helper, "@e[tag=silk_u]", 3, f);
            mobs.get(3).getBukkitEntity().getScoreboardTags().remove("silk_u");
            count(helper, "@e[tag=silk_u]", 2, f);
            mobs.get(5).getBukkitEntity().getScoreboardTags().add("silk_u");
            for (final Iterator<String> it = mobs.get(5).getBukkitEntity().getScoreboardTags().iterator(); it.hasNext(); ) {
                if (it.next().equals("silk_u")) {
                    it.remove();
                }
            }
            count(helper, "@e[tag=silk_u]", 2, f);
            mobs.get(5).entityTags().add("silk_u");
            mobs.get(5).entityTags().removeIf("silk_u"::equals);
            count(helper, "@e[tag=silk_u]", 2, f);
            // which two pigs still hold silk_u depends on entity-table order (limit=1 above): pick them at runtime
            final List<Mob> holders = mobs.stream().filter(m -> m.entityTags().contains("silk_u")).toList();
            if (holders.size() != 2) {
                f.append("\n  expected two mobs to hold silk_u, found ").append(holders.size());
                helper.fail(Component.literal("tag selector mismatches:" + f));
                return;
            }
            holders.get(0).getBukkitEntity().getScoreboardTags().clear();
            count(helper, "@e[tag=silk_t]", 5, f);
            count(helper, "@e[tag=silk_u]", 1, f);
            // NBT load replaces the whole tag set
            final Mob holder = holders.get(1);
            run(helper, "data merge entity " + holder.getStringUUID() + " {Tags:[\"silk_v\"]}");
            count(helper, "@e[tag=silk_t]", 4, f);
            count(helper, "@e[tag=silk_v]", 1, f);
            count(helper, "@e[tag=silk_u]", 0, f);
            // entities joining and leaving the world with a tag
            final Mob extra = helper.spawn(EntityTypes.PIG, new BlockPos(7, 1, 7));
            extra.setNoAi(true);
            extra.addTag("silk_v");
            count(helper, "@e[tag=silk_v]", 2, f);
            extra.discard();
            count(helper, "@e[tag=silk_v]", 1, f);
            count(helper, "@e[tag=]", 0, f); // our mobs all have tags; count only covers tag-less entities in the world
            // execute as order-insensitive count
            run(helper, "scoreboard players set #silk silk_test 0");
            run(helper, "execute as @e[tag=silk_t] run scoreboard players add #silk silk_test 1");
            check(helper, "execute as @e[tag=silk_t]", 4, f);

            // side effects must still happen on entities WITHOUT the tag when an nbt= filter comes first:
            // vanilla saves every candidate for nbt=, and Entity#saveWithoutId resets a NaN rotation to 0.
            // Each probe runs the selector three times with the NaN set before the last run, so any
            // cache that only kicks in on a repeated query is warm when it matters.
            final Mob witness = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(4, 1, 6));
            witness.setNoAi(true);
            // Filters run in the order written: with nbt= first vanilla saves the untagged zombie (yaw reset),
            // with tag= first the tag filter rejects it before nbt= runs (yaw stays NaN).
            final Object[][] probes = {
                {"@e[type=zombie,nbt={},tag=silk_nobody]", true},
                {"@e[nbt={},tag=silk_nobody]", true},
                {"@e[nbt={},tag=silk_nobody,limit=1]", true},
                {"@e[tag=silk_nobody,nbt={}]", false},
            };
            for (final Object[] probe : probes) {
                final String selector = (String) probe[0];
                final boolean vanillaSaves = (Boolean) probe[1];
                run(helper, "execute if entity " + selector);
                run(helper, "execute if entity " + selector);
                setYRot(witness, Float.NaN);
                run(helper, "execute if entity " + selector);
                final boolean saved = witness.getYRot() == 0.0F;
                if (saved != vanillaSaves) {
                    f.append("\n  ").append(selector).append(vanillaSaves ? " must still save the untagged zombie (yaw not reset)" : " must not save the untagged zombie (yaw was reset)");
                }
                setYRot(witness, 0.0F);
            }
            if (!f.isEmpty()) {
                helper.fail(Component.literal("tag selector mismatches:" + f));
            } else {
                helper.succeed();
            }
        });
    }

    private static void setYRot(final Entity entity, final float value) {
        try {
            final Field field = Entity.class.getDeclaredField("yRot");
            field.setAccessible(true);
            field.setFloat(entity, value); // setYRot() refuses non-finite values
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void count(final GameTestHelper helper, final String selector, final int expected, final StringBuilder f) {
        run(helper, "scoreboard players set #silk silk_test -1");
        if (selector.equals("@e[tag=]")) {
            // tag-less entities exist elsewhere (other tests); only check our mobs are not among them
            run(helper, "execute store result score #silk silk_test if entity @e[tag=,tag=silk_t]");
        } else {
            run(helper, "execute store result score #silk silk_test if entity " + selector);
        }
        check(helper, selector, expected, f);
    }

    private static void check(final GameTestHelper helper, final String what, final int expected, final StringBuilder f) {
        final Scoreboard scoreboard = helper.getLevel().getServer().getScoreboard();
        final Objective objective = scoreboard.getObjective("silk_test");
        final ReadOnlyScoreInfo info = objective == null ? null : scoreboard.getPlayerScoreInfo(ScoreHolder.forNameOnly("#silk"), objective);
        final int got = info == null ? Integer.MIN_VALUE : info.value();
        if (got != expected) {
            f.append("\n  ").append(what).append(" -> ").append(got).append(", vanilla expects ").append(expected);
        }
    }

    private static void run(final GameTestHelper helper, final String command) {
        NbtTests.run(helper, command);
    }
}
