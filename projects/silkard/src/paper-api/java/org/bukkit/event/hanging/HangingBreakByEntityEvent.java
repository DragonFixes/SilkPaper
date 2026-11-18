package org.bukkit.event.hanging;

import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Triggered when a hanging entity is removed by an entity
 */
@NullMarked
public class HangingBreakByEntityEvent extends HangingBreakEvent {

    private final Entity remover;
    private final DamageSource damageSource;

    @ApiStatus.Internal
    public HangingBreakByEntityEvent(final Hanging hanging, final Entity remover, final DamageSource damageSource) {
        this(hanging, remover, damageSource, HangingBreakEvent.RemoveCause.ENTITY);
    }

    @ApiStatus.Internal
    public HangingBreakByEntityEvent(final Hanging hanging, final Entity remover, final DamageSource damageSource, final HangingBreakEvent.RemoveCause cause) {
        super(hanging, cause);
        this.remover = remover;
        this.damageSource = damageSource;
    }

    /**
     * Gets the entity that removed the hanging entity.
     *
     * @return the entity that removed the hanging entity
     */
    public Entity getRemover() {
        return this.remover;
    }

    /**
     * Gets the {@link DamageSource} that caused the hanging entity to be removed.
     *
     * @return the damage source
     */
    public DamageSource getDamageSource() {
        return this.damageSource;
    }
    // Silkard start - Spigot API compat
    public HangingBreakByEntityEvent(final Hanging hanging, final Entity remover) {
        this(hanging, remover, HangingBreakEvent.RemoveCause.ENTITY);
    }

    public HangingBreakByEntityEvent(final Hanging hanging, final Entity remover, final HangingBreakEvent.RemoveCause cause) {
        this(hanging, remover, org.bukkit.damage.DamageSource.builder(org.bukkit.damage.DamageType.GENERIC).build(), cause);
    }
    // Silkard end

}
