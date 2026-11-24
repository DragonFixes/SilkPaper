package com.mohistmc.silkard.mixin.vanilla.world.inventory;

import com.mohistmc.silkard.injected.paper.core.PaperItemExtras;
import com.mohistmc.silkard.injected.world.inventory.ContextAnvilMenu;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.view.CraftAnvilView;
import org.jspecify.annotations.Nullable;
import org.purpurmc.purpur.PurpurConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Anvil logic as in Leaf: CraftBukkit's prepare event, denied cost and per-view maximum repair
 * cost; Paper's anvil damage event and level restriction bypass; Purpur's anvil API events,
 * unsafe enchantment options, colored names and cumulative cost toggle.
 *
 * <p>The priority is below the default so other mods' injections into these methods, such as Fabric
 * API's enchanting check in {@code createResult}, apply on top of the overwrites.
 *
 * @author Mgazul
 * @date 2026/6/8
 */
@Mixin(value = AnvilMenu.class, priority = 500)
public abstract class MixinAnvilMenu extends ItemCombinerMenu implements ContextAnvilMenu {

    // CraftBukkit start
    @Unique
    private static final int DEFAULT_DENIED_COST = -1;

    @Unique
    public int maximumRepairCost = 40;

    @Unique
    private CraftAnvilView bukkitEntity;
    // CraftBukkit end

    @Shadow @Final private DataSlot cost;
    @Shadow private int repairItemCountCost;
    @Shadow private @Nullable String itemName;
    @Shadow private boolean onlyRenaming;

    protected MixinAnvilMenu(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slotDefinition) {
        super(menuType, containerId, inventory, access, slotDefinition);
    }

    @Override
    public CraftAnvilView getBukkitView() {
        if (bukkitEntity != null) {
            return bukkitEntity;
        }
        CraftInventoryAnvil inventory = new CraftInventoryAnvil(this.access.getLocation(), this.inputSlots, this.resultSlots);
        this.bukkitEntity = new CraftAnvilView(this.player.getBukkitEntity(), inventory, (AnvilMenu) (Object) this);
        this.bukkitEntity.updateFromLegacy(inventory);
        return this.bukkitEntity;
    }

    @Override
    public int silkard$getDeniedCost() {
        return DEFAULT_DENIED_COST;
    }

    @Override
    public int silkard$maximumRepairCost() {
        return maximumRepairCost;
    }

    @Override
    public void silkard$maximumRepairCost(int maximumRepairCost) {
        this.maximumRepairCost = maximumRepairCost;
    }

    @Unique
    private PaperItemExtras.AnvilMenu silkard$paper() {
        return (PaperItemExtras.AnvilMenu) this;
    }

    /**
     * @author Mgazul
     * @reason CraftBukkit - a cost of 0 is a free item; Purpur - Anvil API bypass cost
     */
    @Overwrite
    protected boolean mayPickup(Player player, boolean hasItem) {
        return (player.hasInfiniteMaterials() || player.experienceLevel >= this.cost.get())
            && (this.silkard$paper().paper$bypassCost() || this.cost.get() > DEFAULT_DENIED_COST) && hasItem;
    }

    /**
     * @author SilkPaper
     * @reason Purpur - Anvil API take event and bypass cost; Paper - AnvilDamagedEvent
     */
    @Overwrite
    protected void onTake(Player player, ItemStack carried) {
        // Purpur start - Anvil API
        if (org.purpurmc.purpur.event.inventory.AnvilTakeResultEvent.getHandlerList().getRegisteredListeners().length > 0) {
            new org.purpurmc.purpur.event.inventory.AnvilTakeResultEvent(player.getBukkitEntity(), this.getBukkitView(), CraftItemStack.asBukkitMirror(carried)).callEvent();
        }
        // Purpur end - Anvil API
        if (!player.hasInfiniteMaterials()) {
            if (this.silkard$paper().paper$bypassCost()) {
                if (player instanceof ServerPlayer serverPlayer) ((com.mohistmc.silkard.mixin.purpur.server.level.PurpurServerPlayerAccessor) serverPlayer).purpur$setLastSentExp(-1); // Purpur - Anvil API
            } else {
                player.giveExperienceLevels(-this.cost.get());
            }
        }

        if (this.repairItemCountCost > 0) {
            ItemStack addition = this.inputSlots.getItem(1);
            if (!addition.isEmpty() && addition.getCount() > this.repairItemCountCost) {
                addition.shrink(this.repairItemCountCost);
                this.inputSlots.setItem(1, addition);
            } else {
                this.inputSlots.setItem(1, ItemStack.EMPTY);
            }
        } else if (!this.onlyRenaming) {
            this.inputSlots.setItem(1, ItemStack.EMPTY);
        }

        this.cost.set(DEFAULT_DENIED_COST); // CraftBukkit - use a variable for set a cost for denied item
        if (player instanceof ServerPlayer serverPlayer
            && !StringUtil.isBlank(this.itemName)
            && !this.inputSlots.getItem(0).getHoverName().getString().equals(this.itemName)) {
            serverPlayer.getTextFilter().processStreamMessage(this.itemName);
        }

        this.inputSlots.setItem(0, ItemStack.EMPTY);
        this.access.execute((level, pos) -> {
            BlockState state = level.getBlockState(pos);
            if (!player.hasInfiniteMaterials() && state.is(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12F) {
                BlockState newBlockState = AnvilBlock.damage(state);
                // Paper start - AnvilDamageEvent
                com.destroystokyo.paper.event.block.AnvilDamagedEvent event = new com.destroystokyo.paper.event.block.AnvilDamagedEvent(this.getBukkitView(), newBlockState != null ? newBlockState.asBlockData() : null);
                if (!event.callEvent()) {
                    return;
                } else if (event.getDamageState() == com.destroystokyo.paper.event.block.AnvilDamagedEvent.DamageState.BROKEN) {
                    newBlockState = null;
                } else {
                    newBlockState = ((org.bukkit.craftbukkit.block.data.CraftBlockData) event.getDamageState().getMaterial().createBlockData()).getState().setValue(AnvilBlock.FACING, state.getValue(AnvilBlock.FACING));
                }
                // Paper end - AnvilDamageEvent
                if (newBlockState == null) {
                    level.removeBlock(pos, false);
                    level.levelEvent(LevelEvent.SOUND_ANVIL_BROKEN, pos, 0);
                } else {
                    level.setBlock(pos, newBlockState, Block.UPDATE_CLIENTS);
                    level.levelEvent(LevelEvent.SOUND_ANVIL_USED, pos, 0);
                }
            } else {
                level.levelEvent(LevelEvent.SOUND_ANVIL_USED, pos, 0);
            }
        });
    }

    /**
     * @author SilkPaper
     * @reason CraftBukkit prepare event and repair cost limit; Paper level restriction bypass;
     *     Purpur anvil API, unsafe enchantments and anvil colors
     */
    @Overwrite
    public void createResult() {
        PaperItemExtras.AnvilMenu paper = this.silkard$paper();
        // Purpur start - Anvil API
        paper.paper$setBypassCost(false);
        paper.paper$setCanDoUnsafeEnchants(false);
        if (org.purpurmc.purpur.event.inventory.AnvilUpdateResultEvent.getHandlerList().getRegisteredListeners().length > 0) {
            new org.purpurmc.purpur.event.inventory.AnvilUpdateResultEvent(this.getBukkitView()).callEvent();
        }
        boolean unsafe = paper.paper$canDoUnsafeEnchants();
        // Purpur end - Anvil API

        ItemStack input = this.inputSlots.getItem(0);
        this.onlyRenaming = false;
        this.cost.set(1);
        int price = 0;
        long tax = 0L;
        int namingCost = 0;
        if (!input.isEmpty() && unsafe || EnchantmentHelper.canStoreEnchantments(input)) { // Purpur - Anvil API
            ItemStack result = input.copy();
            ItemStack addition = this.inputSlots.getItem(1);
            ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(result));
            tax += (long) input.getOrDefault(DataComponents.REPAIR_COST, 0).intValue() + addition.getOrDefault(DataComponents.REPAIR_COST, 0).intValue();
            this.repairItemCountCost = 0;
            if (!addition.isEmpty()) {
                boolean usingBook = addition.has(DataComponents.STORED_ENCHANTMENTS);
                if (result.isDamageableItem() && input.isValidRepairItem(addition)) {
                    int repairAmount = Math.min(result.getDamageValue(), result.getMaxDamage() / 4);
                    if (repairAmount <= 0) {
                        CraftEventFactory.callPrepareAnvilEvent(this.getBukkitView(), ItemStack.EMPTY); // CraftBukkit
                        this.cost.set(DEFAULT_DENIED_COST); // CraftBukkit
                        return;
                    }

                    int count;
                    for (count = 0; repairAmount > 0 && count < addition.getCount(); count++) {
                        int resultDamage = result.getDamageValue() - repairAmount;
                        result.setDamageValue(resultDamage);
                        price++;
                        repairAmount = Math.min(result.getDamageValue(), result.getMaxDamage() / 4);
                    }

                    this.repairItemCountCost = count;
                } else {
                    if (!usingBook && (!result.is(addition.getItem()) || !result.isDamageableItem())) {
                        CraftEventFactory.callPrepareAnvilEvent(this.getBukkitView(), ItemStack.EMPTY); // CraftBukkit
                        this.cost.set(DEFAULT_DENIED_COST); // CraftBukkit
                        return;
                    }

                    if (result.isDamageableItem() && !usingBook) {
                        int remaining1 = input.getMaxDamage() - input.getDamageValue();
                        int remaining2 = addition.getMaxDamage() - addition.getDamageValue();
                        int additional = remaining2 + result.getMaxDamage() * 12 / 100;
                        int remaining = remaining1 + additional;
                        int resultDamage = result.getMaxDamage() - remaining;
                        if (resultDamage < 0) {
                            resultDamage = 0;
                        }

                        if (resultDamage < result.getDamageValue()) {
                            result.setDamageValue(resultDamage);
                            price += 2;
                        }
                    }

                    ItemEnchantments additionalEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(addition);
                    boolean isAnyEnchantmentCompatible = false;
                    boolean isAnyEnchantmentNotCompatible = false;

                    for (Object2IntMap.Entry<Holder<Enchantment>> entry : additionalEnchantments.entrySet()) {
                        Holder<Enchantment> enchantmentHolder = entry.getKey();
                        int current = enchantments.getLevel(enchantmentHolder);
                        int level = entry.getIntValue();
                        level = current == level ? level + 1 : Math.max(level, current);
                        Enchantment enchantment = enchantmentHolder.value();
                        // Purpur start - Config to allow unsafe enchants
                        boolean compatible = unsafe || PurpurConfig.allowInapplicableEnchants || enchantment.canEnchant(input); // the enchantment fits this item type
                        boolean canEnchant1 = true; // no conflict with an enchantment already on the item
                        // Purpur end - Config to allow unsafe enchants
                        if (this.player.hasInfiniteMaterials() || input.is(Items.ENCHANTED_BOOK)) {
                            compatible = true;
                        }

                        Set<Holder<Enchantment>> removedEnchantments = new HashSet<>(); // Purpur - Config to allow unsafe enchants
                        for (Holder<Enchantment> other : enchantments.keySet()) {
                            if (!other.equals(enchantmentHolder) && !Enchantment.areCompatible(enchantmentHolder, other)) {
                                canEnchant1 = unsafe || PurpurConfig.allowIncompatibleEnchants; // Purpur - Anvil API
                                // Purpur start - Config to allow unsafe enchants
                                if (!canEnchant1 && PurpurConfig.replaceIncompatibleEnchants) {
                                    removedEnchantments.add(other);
                                    canEnchant1 = true;
                                }
                                // Purpur end - Config to allow unsafe enchants
                                price++;
                            }
                        }
                        enchantments.removeIf(removedEnchantments::contains); // Purpur - Config to allow unsafe enchants

                        if (!compatible || !canEnchant1) { // Purpur - Config to allow unsafe enchants
                            isAnyEnchantmentNotCompatible = true;
                        } else {
                            isAnyEnchantmentCompatible = true;
                            if (!PurpurConfig.allowHigherEnchantsLevels && level > enchantment.getMaxLevel() && !paper.paper$bypassEnchantmentLevelRestriction()) { // Paper - bypass anvil level restrictions // Purpur
                                level = enchantment.getMaxLevel();
                            }

                            enchantments.set(enchantmentHolder, level);
                            int fee = enchantment.getAnvilCost();
                            if (usingBook) {
                                fee = Math.max(1, fee / 2);
                            }

                            price += fee * level;
                            if (input.getCount() > 1) {
                                price = 40;
                            }
                        }
                    }

                    if (isAnyEnchantmentNotCompatible && !isAnyEnchantmentCompatible) {
                        CraftEventFactory.callPrepareAnvilEvent(this.getBukkitView(), ItemStack.EMPTY); // CraftBukkit
                        this.cost.set(DEFAULT_DENIED_COST); // CraftBukkit
                        return;
                    }
                }
            }

            if (this.itemName != null && !StringUtil.isBlank(this.itemName)) {
                if (!this.itemName.equals(input.getHoverName().getString())) {
                    namingCost = 1;
                    price += namingCost;
                    result.set(DataComponents.CUSTOM_NAME, this.silkard$renamedName(this.itemName)); // Purpur - Allow anvil colors
                }
            } else if (input.has(DataComponents.CUSTOM_NAME)) {
                namingCost = 1;
                price += namingCost;
                result.remove(DataComponents.CUSTOM_NAME);
            }

            int finalPrice = price <= 0 ? 0 : (int) Mth.clamp(tax + price, 0L, 2147483647L);
            this.cost.set(finalPrice);
            if (price <= 0) {
                result = ItemStack.EMPTY;
            }

            if (namingCost == price && namingCost > 0) {
                // CraftBukkit start
                if (this.cost.get() >= this.maximumRepairCost) {
                    this.cost.set(this.maximumRepairCost - 1);
                }
                // CraftBukkit end
                this.onlyRenaming = true;
            }

            // Purpur start - Anvil API
            if (paper.paper$bypassCost() && this.cost.get() >= this.maximumRepairCost) {
                this.cost.set(this.maximumRepairCost - 1);
            }
            // Purpur end - Anvil API

            if (this.cost.get() >= this.maximumRepairCost && !this.player.hasInfiniteMaterials()) { // CraftBukkit
                result = ItemStack.EMPTY;
            }

            if (!result.isEmpty()) {
                int baseCost = result.getOrDefault(DataComponents.REPAIR_COST, 0);
                if (baseCost < addition.getOrDefault(DataComponents.REPAIR_COST, 0)) {
                    baseCost = addition.getOrDefault(DataComponents.REPAIR_COST, 0);
                }

                if (namingCost != price || namingCost == 0) {
                    baseCost = AnvilMenu.calculateIncreasedRepairCost(baseCost);
                }

                result.set(DataComponents.REPAIR_COST, baseCost);
                EnchantmentHelper.setEnchantments(result, enchantments.toImmutable());
            }

            CraftEventFactory.callPrepareAnvilEvent(this.getBukkitView(), result); // CraftBukkit
            this.broadcastChanges();

            // Purpur start - Anvil API: the client rejects unsafe results, so they are sent directly
            if ((unsafe || PurpurConfig.allowInapplicableEnchants || PurpurConfig.allowIncompatibleEnchants) && result != ItemStack.EMPTY
                && this.player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 2, result));
                serverPlayer.connection.send(new ClientboundContainerSetDataPacket(this.containerId, 0, this.cost.get()));
            }
            // Purpur end - Anvil API
        } else {
            CraftEventFactory.callPrepareAnvilEvent(this.getBukkitView(), ItemStack.EMPTY); // CraftBukkit
            this.cost.set(DEFAULT_DENIED_COST); // CraftBukkit
        }
        this.sendAllDataToRemote(); // CraftBukkit - SPIGOT-6686, SPIGOT-7931: Always send completed inventory to stay in sync with client
    }

    /**
     * Purpur - Allow anvil colors. Players with the purpur.anvil permissions can use legacy color
     * and format codes or MiniMessage, and start the name with a reset to drop the italics.
     */
    @Unique
    private Component silkard$renamedName(String itemName) {
        if (this.player == null) {
            return Component.literal(itemName);
        }
        org.bukkit.craftbukkit.entity.CraftHumanEntity bukkit = this.player.getBukkitEntity();
        String name = itemName;
        boolean removeItalics = false;
        if (bukkit.hasPermission("purpur.anvil.remove_italics")) {
            if (name.startsWith("&r")) {
                name = name.substring(2);
                removeItalics = true;
            } else if (name.startsWith("<r>")) {
                name = name.substring(3);
                removeItalics = true;
            } else if (name.startsWith("<reset>")) {
                name = name.substring(7);
                removeItalics = true;
            }
        }
        if (this.player.level().purpurConfig().anvilAllowColors) {
            if (bukkit.hasPermission("purpur.anvil.color")) {
                name = silkard$legacyCodes(name, "(?i)&([0-9a-fr])");
            }
            if (bukkit.hasPermission("purpur.anvil.format")) {
                name = silkard$legacyCodes(name, "(?i)&([k-or])");
            }
        }
        net.kyori.adventure.text.Component component;
        if (this.player.level().purpurConfig().anvilColorsUseMiniMessage && bukkit.hasPermission("purpur.anvil.minimessage")) {
            component = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(org.bukkit.ChatColor.stripColor(name));
        } else {
            component = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(name);
        }
        if (removeItalics) {
            component = component.decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false);
        }
        return io.papermc.paper.adventure.PaperAdventure.asVanilla(component);
    }

    @Unique
    private static String silkard$legacyCodes(String name, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(name);
        while (matcher.find()) {
            String match = matcher.group(1);
            name = name.replace("&" + match, "§" + match.toLowerCase(Locale.ROOT));
        }
        return name;
    }

    // Purpur - Make anvil cumulative cost configurable: with it off, repairs never get pricier
    @Inject(method = "calculateIncreasedRepairCost", at = @At("HEAD"), cancellable = true)
    private static void purpur$cumulativeCost(int baseCost, CallbackInfoReturnable<Integer> cir) {
        if (!PurpurConfig.anvilCumulativeCost) {
            cir.setReturnValue(0);
        }
    }
}
