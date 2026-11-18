package org.bukkit.craftbukkit;

import io.papermc.paper.util.Holderable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import org.bukkit.GameEvent;
import org.bukkit.NamespacedKey;

public class CraftGameEvent extends GameEvent implements Holderable<net.minecraft.world.level.gameevent.GameEvent> {

    // Paper's Holderable and the Spigot-compat RegistryAware both default getKeyOrNull; Paper's wins.
    @Override
    public org.bukkit.NamespacedKey getKeyOrNull() {
        return this.getHolder().unwrapKey().map(org.bukkit.craftbukkit.util.CraftNamespacedKey::fromResourceKey).orElse(null);
    }

    public static GameEvent minecraftHolderToBukkit(Holder<net.minecraft.world.level.gameevent.GameEvent> minecraft) {
        return CraftRegistry.minecraftHolderToBukkit(minecraft, Registries.GAME_EVENT);
    }

    public static Holder<net.minecraft.world.level.gameevent.GameEvent> bukkitToMinecraftHolder(GameEvent bukkit) {
        return CraftRegistry.bukkitToMinecraftHolder(bukkit);
    }

    private final Holder<net.minecraft.world.level.gameevent.GameEvent> holder;

    public CraftGameEvent(final Holder<net.minecraft.world.level.gameevent.GameEvent> holder) {
        this.holder = holder;
    }

    @Override
    public Holder<net.minecraft.world.level.gameevent.GameEvent> getHolder() {
        return this.holder;
    }

    @Override
    public int getRange() {
        return this.getHandle().notificationRadius();
    }

    @Override
    public int getVibrationLevel() {
        return net.minecraft.world.level.gameevent.vibrations.VibrationSystem.getGameEventFrequency(this.getHolder().unwrapKey().orElseThrow());
    }

    @Override
    public NamespacedKey getKey() {
        return Holderable.super.getKey();
    }

    @Override
    public boolean equals(Object other) {
        return Holderable.super.implEquals(other);
    }

    @Override
    public int hashCode() {
        return Holderable.super.implHashCode();
    }

    @Override
    public String toString() {
        return Holderable.super.implToString();
    }
}
