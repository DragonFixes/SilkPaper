package com.mohistmc.silkard.mixin.paper.server;

import com.mohistmc.silkard.injected.paper.server.PaperRaid;
import net.minecraft.world.entity.raid.Raid;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.persistence.PersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Raid.class)
public abstract class MixinPaperRaid implements PaperRaid {

    @Unique private static final CraftPersistentDataTypeRegistry SILKARD$DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();

    // TODO Silkard - raids are saved through a record codec with no hook for extra data, so the container lasts only until the raid unloads
    @Unique private final CraftPersistentDataContainer silkard$persistentDataContainer = new CraftPersistentDataContainer(SILKARD$DATA_TYPE_REGISTRY);

    @Override
    public PersistentDataContainer paper$persistentDataContainer() {
        return this.silkard$persistentDataContainer;
    }
}
