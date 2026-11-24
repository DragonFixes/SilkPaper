# Event coverage

Bukkit, Paper and Purpur events that Leaf fires and SilkPaper does not. Silkard has no vanilla
patches, so every event Paper fires from a patched Minecraft class needs a mixin here; any
missing one is silently never fired, and plugins listening to it do nothing.

Update this file whenever an event is wired up, and commit it with the fix.

## How to regenerate

Two scans, both against a Leaf clone with `gradlew applyAllPatches` run (see
`docs/leaf-port-status.md`):

1. **Factory helpers with no caller.** For each `public static call*/handle*` method in
   `org/bukkit/craftbukkit/event/CraftEventFactory.java`, count call sites in
   `projects/silkard/src/main/java`. Zero means the event never fires.
2. **Events Leaf constructs inline.** Collect every `new (pkg.)*XxxEvent(` in
   `leaf-server/src/minecraft/java`, strip the package, sort unique, and subtract the same list
   from `projects/silkard/src/main/java`. Then drop names SilkPaper mentions at all (some are
   fired another way) and check those by hand. Scan 1 misses everything this one finds.

Use one `rg` pass per scan; a loop of one search per name takes minutes.

## Fixed

PlayerDeathEvent, EntityDeathEvent, InventoryOpenEvent and InventoryCloseEvent for vanilla
containers, PlayerDropItemEvent, PlayerLevelChangeEvent, EntityBreakDoorEvent, TradeSelectEvent,
PlayerBedEnterEvent, PlayerBedFailEnterEvent, PlayerBedLeaveEvent, PlayerInteractEntityEvent /
PlayerInteractAtEntityEvent, PlayerUseUnknownEntityEvent.

## Missing, by plugin impact

### Tier 1: core Bukkit events most plugins use

| Event | Leaf call site |
|---|---|
| PlayerToggleSneakEvent, PlayerToggleSprintEvent, PlayerToggleFlightEvent | ServerGamePacketListenerImpl |
| PlayerItemHeldEvent, PlayerSwapHandItemsEvent | ServerGamePacketListenerImpl |
| PlayerAnimationEvent / PlayerArmSwingEvent | ServerGamePacketListenerImpl |
| InventoryCreativeEvent | ServerGamePacketListenerImpl |
| PlayerInputEvent, PlayerJumpEvent | ServerGamePacketListenerImpl |
| PlayerItemConsumeEvent, EntityResurrectEvent | LivingEntity |
| VehicleEnterEvent, VehicleExitEvent, VehicleBlockCollisionEvent | Entity |
| PlayerAdvancementDoneEvent | PlayerAdvancements |
| ServerTickStartEvent, ServerTickEndEvent | MinecraftServer |
| PlayerSpawnLocationEvent, AsyncPlayerSpawnLocationEvent | PrepareSpawnTask |
| PlayerPostRespawnEvent, PlayerServerFullCheckEvent, ProfileWhitelistVerifyEvent | PlayerList |
| UnknownCommandEvent | Commands |
| PlayerEditBookEvent, PlayerRecipeBookClickEvent, PlayerRecipeBookSettingsChangeEvent, PlayerBundleItemSelectEvent | ServerGamePacketListenerImpl (factory helpers) |
| LingeringPotionSplashEvent, WaterBottleSplash | ThrownLingeringPotion, AbstractThrownPotion (factory helpers) |
| BlockPreDispenseEvent, BlockFailedDispenseEvent | DispenserBlock, DropperBlock (factory helpers) |
| PlayerAttemptPickupItemEvent, PlayerPickupExperienceEvent | ItemEntity, ExperienceOrb |
| PlayerLaunchProjectileEvent, PlayerReadyArrowEvent, PlayerElytraBoostEvent | items, Player |
| CreeperIgniteEvent, SlimeSplitEvent, EntityAirChangeEvent, EntityPortalExitEvent | mobs, Entity |
| EntityMountEvent, EntityDismountEvent | Entity |
| PlayerTradeEvent, PlayerPurchaseEvent (villagers; custom merchants already fire them) | AbstractVillager, WanderingTrader |
| PlayerArmorChangeEvent | LivingEntity |
| PlayerItemCooldownEvent | ItemCooldowns |
| PrepareHopperTransferEvent, PlayerCustomClickEvent, ServerResourcesReloadedEvent | HopperBlockEntity, packet listener, MinecraftServer |

### Tier 2: Paper events used by protection, logging and anti-cheat plugins

EntityAddToWorldEvent, EntityRemoveFromWorldEvent (ServerLevel); BlockDestroyEvent (Level);
EntityMoveEvent, EntityJumpEvent, EntityEquipmentChangedEvent (LivingEntity); PlayerInventorySlotChangeEvent (ServerPlayer);
 PlayerItemFrameChangeEvent;
PlayerNameEntityEvent; PlayerFlowerPotManipulateEvent; PlayerInsertLecternBookEvent;
PlayerLecternPageChangeEvent; PlayerShearBlockEvent; PlayerSignCommandPreprocessEvent;
UncheckedSignChangeEvent; PlayerPickBlockEvent, PlayerPickEntityEvent; PlayerFailMoveEvent;
PlayerClientOptionsChangeEvent; PlayerTrackEntityEvent, PlayerUntrackEntityEvent;
PlayerChunkLoadEvent, PlayerChunkUnloadEvent; PlayerConnectionCloseEvent; PreCreatureSpawnEvent,
PreSpawnerSpawnEvent, PlayerNaturallySpawnCreaturesEvent; EntityBreakEvent,
EntityBreakByEntityEvent; EntityDamageItemEvent; EntityPathfindEvent; EntityConstructEvent;
PrePlayerAttackEntityEvent, PlayerAttackEntityCooldownResetEvent, PlayerShieldDisableEvent;
PlayerStopUsingItemEvent; PlayerItemGroupCooldownEvent; PlayerStart/StopSpectatingEntityEvent;
PlayerTeleportEndGatewayEvent; AsyncPlayerSendSuggestionsEvent; WhitelistToggleEvent,
WhitelistStateUpdateEvent; WorldDifficultyChangeEvent; WorldBorder*ChangeEvent; StructuresLocateEvent;
PlayerConnectionInitialConfigureEvent, PlayerConnectionReconfigureEvent,
PlayerCodeOfConductSendEvent, PlayerClientLoadedWorldEvent, ClientTickEndEvent.

Factory helpers with no caller: BlockLockCheckEvent, EntityFertilizeEggEvent, EntityIgniteEvent,
EntityInteractEvent (RemoveBlockGoal), EntityLandEvent, EntityZapEvent,
PlayerToggleEntityAgeLockEvent, PrepareResultEvent, TransporterValidateTargetEvent,
VillagerReputationChangeEvent, BellResonateEvent, WitchReadyPotionEvent, PlayerRecipeListUpdateEvent,
CauldronLevelChangeEvent (only some call sites), PlayerLeashEntityEvent (check LeadItem and
LeashFenceKnotEntity).

### Tier 3: mob and block flavour events

Beacon (Activated, Deactivated, Effect, PlayerChangeBeaconEffect), bee (Purpur), BellRevealRaider,
BlockBreakBlock, BlockBreakProgressUpdate, CartographyItem, Compost/EntityCompostItem,
DragonEggForm, ElderGuardianAppearance, EnderDragon (FireballHit, Flame, ShootFireball),
Enderman (AttackPlayer, Escape), EntityAttemptSmash/SpinAttack, EntityCollideWithEntity, EntityDye,
EntityEffectTick, EntityInsideBlock, EntityLoadCrossbow, EntityLunge, EntityPortalReady,
EntityPotentSulfurLaunch, EntityPushedByEntityAttack, EntityToggleSit, FishHookStateChange,
GoatRamEntity, ItemCrafted, Llama caravan, PhantomPreSpawn, PlayerDeepSleep, PlayerLoomPatternSelect,
PlayerMapFilled, PlayerStonecutterRecipeSelect, PlayerSwapWithEquipmentSlot, Purpur spawner eggs,
PufferFishStateChange, Purpur PreBlock/PreEntityExplode, Leaf BlockExplosionHit, ShulkerDuplicate,
SkeletonHorseTrap, Slime (ChangeDirection, Swim, TargetLivingEntity, Wander), SulfurCubeSwallowItem,
TameableDeathMessage, TargetHit, ThrownEggHatch, Turtle (GoHome, LayEgg, StartDigging),
VaultChangeState, WardenAngerChange, Witch (ConsumePotion, ThrowPotion), GS4Query,
AsyncServerDataFixerRemoveBlockEntity, Purpur PlayerBookTooLarge, Leaf AsyncPreAuthenticate,
Ridable (Purpur ridable mobs, not ported).

Not events plugins see (ignore): JFR events (ChunkGeneration, ChunkRegionRead/Write, ClientFps,
NetworkSummary, PacketReceived/Sent, ServerTickTime, StructureGeneration, WorldLoadFinished),
Log4jLogEvent, FlushEvent, GameTestEvent, vanilla CustomBossEvent, PaperInventoryMoveItemEvent.
