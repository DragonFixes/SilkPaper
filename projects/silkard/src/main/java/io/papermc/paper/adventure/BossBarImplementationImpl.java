package io.papermc.paper.adventure;

import com.google.common.collect.Collections2;
import java.util.Set;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBarImplementation;
import net.kyori.adventure.bossbar.BossBarViewer;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public final class BossBarImplementationImpl implements BossBar.Listener, BossBarImplementation {
    private final BossBar bar;
    private ServerBossEvent vanilla;

    public BossBarImplementationImpl(final BossBar bar) {
        this.bar = bar;
    }

    public void playerShow(final CraftPlayer player) {
        if (this.vanilla == null) {
            this.vanilla = new ServerBossEvent(
                net.minecraft.util.Mth.createInsecureUUID(net.minecraft.util.RandomSource.create()),
                PaperAdventure.asVanilla(this.bar.name()),
                PaperAdventure.asVanilla(this.bar.color()),
                PaperAdventure.asVanilla(this.bar.overlay())
            );
            this.vanilla.setProgress(this.bar.progress());
            this.syncFlags();
            this.bar.addListener(this);
        }
        this.vanilla.addPlayer(player.getHandle());
    }

    // The vanilla event builds its packets from its own fields, so every Adventure change is
    // copied into it; ServerBossEvent's setters send the update packet when a value changes.
    private void syncFlags() {
        this.vanilla.setDarkenScreen(this.bar.hasFlag(BossBar.Flag.DARKEN_SCREEN));
        this.vanilla.setPlayBossMusic(this.bar.hasFlag(BossBar.Flag.PLAY_BOSS_MUSIC));
        this.vanilla.setCreateWorldFog(this.bar.hasFlag(BossBar.Flag.CREATE_WORLD_FOG));
    }

    public void playerHide(final CraftPlayer player) {
        if (this.vanilla != null) {
            this.vanilla.removePlayer(player.getHandle());
            if (this.vanilla.getPlayers().isEmpty()) {
                this.bar.removeListener(this);
                this.vanilla = null;
            }
        }
    }

    @Override
    public void bossBarNameChanged(final @NonNull BossBar bar, final @NonNull Component oldName, final @NonNull Component newName) {
        if (this.vanilla != null) {
            this.vanilla.setName(PaperAdventure.asVanilla(newName));
        }
    }

    @Override
    public void bossBarProgressChanged(final @NonNull BossBar bar, final float oldProgress, final float newProgress) {
        if (this.vanilla != null) {
            this.vanilla.setProgress(newProgress);
        }
    }

    @Override
    public void bossBarColorChanged(final @NonNull BossBar bar, final BossBar.@NonNull Color oldColor, final BossBar.@NonNull Color newColor) {
        if (this.vanilla != null) {
            this.vanilla.setColor(PaperAdventure.asVanilla(newColor));
        }
    }

    @Override
    public void bossBarOverlayChanged(final @NonNull BossBar bar, final BossBar.@NonNull Overlay oldOverlay, final BossBar.@NonNull Overlay newOverlay) {
        if (this.vanilla != null) {
            this.vanilla.setOverlay(PaperAdventure.asVanilla(newOverlay));
        }
    }

    @Override
    public void bossBarFlagsChanged(final @NonNull BossBar bar, final @NonNull Set<BossBar.Flag> flagsAdded, final @NonNull Set<BossBar.Flag> flagsRemoved) {
        if (this.vanilla != null) {
            this.syncFlags();
        }
    }

    @Override
    public @NotNull Iterable<? extends BossBarViewer> viewers() {
        return this.vanilla == null ? Set.of() : Collections2.transform(this.vanilla.getPlayers(), ServerPlayer::getBukkitEntity);
    }
}
