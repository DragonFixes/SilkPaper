package com.mohistmc.silkard.mixin.vanilla.server.level;

import com.mohistmc.silkard.injected.server.level.ContextServerLevel;
import com.mohistmc.silkard.injected.world.entity.ContextEntity;
import com.mohistmc.silkard.injected.world.level.ContextExplosion;
import com.mohistmc.silkard.injected.world.level.saveddata.ContextWeatherData;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.saveddata.WeatherData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.timers.TimerQueue;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import io.izzel.arclight.mixin.CreateConstructor;
import io.izzel.arclight.mixin.ShadowConstructor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Mgazul
 * @date 2026/4/2 18:11
 */
@Mixin(ServerLevel.class)
public abstract class MixinServerLevel implements ContextServerLevel {

    // @formatter:off
    @Shadow @Final private ServerChunkCache chunkSource;
    @Shadow @Final private PersistentEntitySectionManager<Entity> entityManager;
    @Shadow @Final public ServerLevelData serverLevelData;
    @Shadow @Final private List<ServerPlayer> players;
    // @formatter:on

    @Unique
    private boolean silkard$wasRaining;

    @Unique
    private float silkard$oldRain;

    @Unique
    private float silkard$oldThunder;

    // CraftBukkit start
    public LevelStorageSource.LevelStorageAccess storageSource;
    public UUID uuid;
    // Moved from MinecraftServer start
    private GameRules gameRules;
    private WorldGenSettings worldGenSettings;
    private CustomBossEvents customBossEvents;
    private RandomSequences randomSequences;
    private WeatherData weatherData;
    private TimerQueue<MinecraftServer> scheduledEvents;
    private ServerClockManager clockManager;
    @Unique private SavedDataStorage savedDataStorage;

    public WorldGenSettings getWorldGenSettings() {
        return this.worldGenSettings != null ? this.worldGenSettings : ((ServerLevel) (Object) this).getServer().getWorldGenSettings();
    }

    public CustomBossEvents getCustomBossEvents() {
        return this.customBossEvents != null ? this.customBossEvents : ((ServerLevel) (Object) this).getServer().getCustomBossEvents();
    }

    public RandomSource getRandomSequence(Identifier key) {
        return this.getRandomSequences().get(key, this.getWorldGenSettings().options().seed());
    }

    public RandomSequences getRandomSequences() {
        return this.randomSequences != null ? this.randomSequences : ((ServerLevel) (Object) this).getServer().getRandomSequences();
    }

    // CraftBukkit start
    @Inject(method = "getWeatherData", at = @At("RETURN"))
    private void silkard$initWeatherData(CallbackInfoReturnable<WeatherData> cir) {
        ((ContextWeatherData) (Object) cir.getReturnValue()).silkard$init((ServerLevel) (Object) this);
    }

    @Shadow public abstract SavedDataStorage getDataStorage();
    @Shadow public abstract MinecraftServer getServer();

    // CraftBukkit - per-world clock manager
    @Inject(method = "clockManager", at = @At("HEAD"), cancellable = true)
    private void silkard$perWorldClockManager(CallbackInfoReturnable<ServerClockManager> cir) {
        if (this.clockManager == null) {
            ServerClockManager manager = this.getDataStorage().computeIfAbsent(ServerClockManager.TYPE);
            manager.init(this.getServer());
            manager.silkard$init((ServerLevel) (Object) this);
            this.clockManager = manager;
        }
        cir.setReturnValue(this.clockManager);
    }
    // CraftBukkit - sleeping through the night skips this world's clock, not the server-wide one
    @Redirect(method = "tick", require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"))
    private ServerClockManager silkard$sleepLevelClock(MinecraftServer server) {
        return ((ServerLevel) (Object) this).clockManager();
    }
    // CraftBukkit end

    public void setWeatherParameters(int clearTime, int rainTime, boolean raining, boolean thundering) {
        WeatherData weatherdata = this.getWeatherData();

        weatherdata.setClearWeatherTime(clearTime);
        weatherdata.setRainTime(rainTime);
        weatherdata.setThunderTime(rainTime);
        weatherdata.setRaining(raining);
        weatherdata.setThundering(thundering);
    }

    @Override
    public TimerQueue<MinecraftServer> getScheduledEvents() {
        return this.scheduledEvents != null ? this.scheduledEvents : ((ServerLevel) (Object) this).getServer().getScheduledEvents();
    }
    // Moved from MinecraftServer end

    public LevelChunk getChunkIfLoaded(int x, int z) {
        return this.chunkSource.getChunk(x, z, false);
    }

    @Override
    public ResourceKey<LevelStem> getTypeKey() {
        if (this.silkard$typeKey != null) {
            return this.silkard$typeKey;
        }
        return storageSource.silkard$getTypeKey();
    }

    @Override
    public LevelStorageSource.LevelStorageAccess silkard_storageSource() {
       return storageSource;
    }

    @Override
    public void silkard$setStorageSource(LevelStorageSource.LevelStorageAccess storageSource) {
        this.storageSource = storageSource;
    }

    // CraftBukkit start
    public org.bukkit.generator.ChunkGenerator generator;
    public org.bukkit.generator.BiomeProvider biomeProvider;
    public org.bukkit.World.Environment environment;

    @Unique
    private org.bukkit.craftbukkit.CraftWorld silkard$bukkitWorld;

    @Override
    public UUID silkard$uuid() {
        if (this.uuid == null) {
            this.uuid = UUID.nameUUIDFromBytes(("silkard-world:" + ((ServerLevel) (Object) this).dimension().identifier()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return this.uuid;
    }

    @Override
    public org.bukkit.craftbukkit.CraftWorld getWorld() {
        if (this.silkard$bukkitWorld == null) {
            this.silkard$bukkitWorld = new org.bukkit.craftbukkit.CraftWorld((ServerLevel) (Object) this,
                    org.bukkit.craftbukkit.util.CraftNamespacedKey.fromMinecraft(((ServerLevel) (Object) this).dimension().identifier()), this.biomeProvider,
                    (this.environment == null) ? org.bukkit.World.Environment.NORMAL : this.environment);
        }
        return this.silkard$bukkitWorld;
    }

    @Override
    public org.bukkit.generator.ChunkGenerator silkard$generator() {
        return this.generator;
    }

    @Override
    public org.bukkit.generator.BiomeProvider silkard$biomeProvider() {
        return this.biomeProvider;
    }

    @Override
    public void silkard$setGenerator(org.bukkit.generator.ChunkGenerator gen, org.bukkit.generator.BiomeProvider provider) {
        this.generator = gen;
        this.biomeProvider = provider;
    }

    @Override
    public org.bukkit.World.Environment silkard$environment() {
        return this.environment == null ? org.bukkit.World.Environment.NORMAL : this.environment;
    }

    @Override
    public void silkard$setEnvironment(org.bukkit.World.Environment env) {
        this.environment = env;
    }
    // CraftBukkit end

    // Paper start - rework world loading process
    // Every dimension shares the server's storage access, so the level stem, Bukkit name and UUID
    // are kept per level rather than derived from the storage access.
    @Unique private ResourceKey<LevelStem> silkard$typeKey;
    @Unique private String silkard$bukkitName;

    @ShadowConstructor
    public void silkard$constructor(MinecraftServer server, java.util.concurrent.Executor executor, LevelStorageSource.LevelStorageAccess levelStorage,
                                    ServerLevelData levelData, ResourceKey<net.minecraft.world.level.Level> dimension, LevelStem levelStem,
                                    boolean isDebug, long biomeZoomSeed, List<net.minecraft.world.level.CustomSpawner> customSpawners, boolean tickTime) {
        throw new AbstractMethodError();
    }

    /**
     * Paper's constructor, which plugins compiled against Paper (Worlds) call directly. The level's
     * data is its {@code paper:level_overrides} saved data, stored with Paper's metadata (UUID) and
     * PDC in the dimension's own {@code data/} folder.
     */
    @CreateConstructor
    public void silkard$constructor(MinecraftServer server, java.util.concurrent.Executor executor, LevelStorageSource.LevelStorageAccess levelStorage,
                                    WorldGenSettings worldGenSettings, ResourceKey<net.minecraft.world.level.Level> dimension, LevelStem levelStem,
                                    boolean isDebug, long biomeZoomSeed, List<net.minecraft.world.level.CustomSpawner> customSpawners, boolean tickTime,
                                    ResourceKey<LevelStem> typeKey, org.bukkit.World.Environment env,
                                    org.bukkit.generator.ChunkGenerator gen, org.bukkit.generator.BiomeProvider biomeProvider,
                                    SavedDataStorage savedDataStorage, io.papermc.paper.world.PaperWorldLoader.LoadedWorldData loadedWorldData) {
        final io.papermc.paper.world.saveddata.PaperLevelOverrides levelOverrides = loadedWorldData.levelOverrides();
        savedDataStorage.set(io.papermc.paper.world.saveddata.PaperLevelOverrides.TYPE, levelOverrides);
        savedDataStorage.set(io.papermc.paper.world.saveddata.PaperWorldMetadata.TYPE, new io.papermc.paper.world.saveddata.PaperWorldMetadata(loadedWorldData.uuid()));
        savedDataStorage.set(io.papermc.paper.world.saveddata.PaperWorldPDC.TYPE, loadedWorldData.pdc() == null ? io.papermc.paper.world.saveddata.PaperWorldPDC.TYPE.constructor().get() : loadedWorldData.pdc());
        com.mohistmc.silkard.paper.world.PaperLevelInit.begin(new com.mohistmc.silkard.paper.world.PaperLevelInit(
            dimension, worldGenSettings, typeKey, env, gen, biomeProvider, savedDataStorage, loadedWorldData));
        this.silkard$constructor(server, executor, levelStorage, levelOverrides, dimension, levelStem, isDebug, biomeZoomSeed, customSpawners, tickTime);
        com.mohistmc.silkard.paper.world.PaperLevelInit.end();
        this.worldGenSettings = worldGenSettings;
        this.storageSource = levelStorage;
        this.silkard$typeKey = typeKey;
        this.silkard$bukkitName = loadedWorldData.bukkitName();
        this.uuid = loadedWorldData.uuid();
        this.environment = env;
        this.generator = gen;
        this.biomeProvider = biomeProvider;
        this.savedDataStorage = savedDataStorage;
        // Paper - per-level scheduledEvents. The overworld's data folder is the main save's, which
        // the server's own queue already reads scheduled_events.dat from, so the overworld shares
        // that queue rather than loading a second copy of the same file.
        this.scheduledEvents = dimension == net.minecraft.world.level.Level.OVERWORLD
            ? server.getScheduledEvents()
            : savedDataStorage.computeIfAbsent(TimerQueue.TYPE);
        // CraftBukkit - the constructor registers the Bukkit world; plugins that build levels
        // themselves (Worlds) look it up with Bukkit.getWorld right after construction.
        ((org.bukkit.craftbukkit.CraftServer) org.bukkit.Bukkit.getServer()).addWorld(this.getWorld());
    }

    // Paper - per-level scheduledEvents: each level runs its own queue against its own clock. The
    // server-wide queue holds trigger times from whichever level scheduled them, so a level whose
    // clock runs ahead would find "1t" functions always due and re-run them forever in one tick.
    @Redirect(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getScheduledEvents()Lnet/minecraft/world/level/timers/TimerQueue;"))
    private TimerQueue<MinecraftServer> silkard$levelScheduledEvents(MinecraftServer server) {
        return this.getScheduledEvents();
    }

    // Vanilla reads the server's (overworld's) settings; a Paper-style level has its own seed and structures setting.
    @Redirect(method = "<init>(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/world/level/storage/ServerLevelData;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/LevelStem;ZJLjava/util/List;Z)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorldGenSettings()Lnet/minecraft/world/level/levelgen/WorldGenSettings;"))
    private WorldGenSettings silkard$levelWorldGenSettings(MinecraftServer server) {
        com.mohistmc.silkard.paper.world.PaperLevelInit init = com.mohistmc.silkard.paper.world.PaperLevelInit.current(((ServerLevel) (Object) this).dimension());
        return init != null ? init.worldGenSettings() : server.getWorldGenSettings();
    }

    @Override
    public @org.jspecify.annotations.Nullable String silkard$bukkitName() {
        return this.silkard$bukkitName;
    }
    // Paper end - rework world loading process

    @Override
    public void silkard_tickEntityManager() {
        this.entityManager.tick(); // SPIGOT-6526
    }

    // CraftBukkit start - portal handling moved to Entity#postTick
    @Inject(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V", shift = At.Shift.AFTER))
    private void silkard$postTick(Entity entity, CallbackInfo ci) {
        ((ContextEntity) entity).postTick();
    }

    @Inject(method = "tickPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;rideTick()V", shift = At.Shift.AFTER))
    private void silkard$passengerPostTick(Entity vehicle, Entity passenger, CallbackInfo ci) {
        ((ContextEntity) passenger).postTick();
    }
    // CraftBukkit end

    // CraftBukkit - 26.3 dropped the SpawnReason overloads: fire the add-entity event here, then delegate to the raw 1-arg add.
    public boolean addFreshEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        if (!CraftEventFactory.doEntityAddEventCalling((ServerLevel) (Object) this, entity, reason)) {
            return false;
        }
        return ((ServerLevel) (Object) this).addFreshEntity(entity);
    }

    // CraftBukkit - 2-arg tryAdd, vanilla only ships the no-reason variant
    @Override
    public boolean tryAddFreshEntityWithPassengers(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        if (entity.getSelfAndPassengers().map(Entity::getUUID).anyMatch(this.entityManager::isLoaded)) {
            return false;
        }
        ((com.mohistmc.silkard.injected.world.level.ContextServerLevelAccessor) this).addFreshEntityWithPassengers(entity, reason);
        return true;
    }

    // CraftBukkit start - weather per world / per player
    @Inject(method = "prepareWeather", at = @At("HEAD"), cancellable = true)
    private void silkard$prepareWeather(WeatherData weatherData, CallbackInfo ci) {
        ((ContextWeatherData) (Object) weatherData).silkard$init((ServerLevel) (Object) this);
        if (!((ServerLevel) (Object) this).dimensionType().hasSkyLight()) {
            ci.cancel();
        }
    }

    @Inject(method = "advanceWeatherCycle", at = @At("HEAD"))
    private void silkard$advanceWeatherHead(CallbackInfo ci) {
        this.silkard$wasRaining = ((ServerLevel) (Object) this).isRaining();
        this.silkard$oldRain = ((ServerLevel) (Object) this).getRainLevel(1.0F);
        this.silkard$oldThunder = ((ServerLevel) (Object) this).getThunderLevel(1.0F);
    }

    @Redirect(method = "advanceWeatherCycle", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void silkard$skipWeatherBroadcast(PlayerList playerList, Packet<?> packet) {
    }

    @Redirect(method = "advanceWeatherCycle", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/resources/ResourceKey;)V"))
    private void silkard$skipWeatherBroadcastDim(PlayerList playerList, Packet<?> packet, ResourceKey<Level> dimension) {
    }

    @Inject(method = "advanceWeatherCycle", at = @At("TAIL"))
    private void silkard$advanceWeatherTail(CallbackInfo ci) {
        for (ServerPlayer player : this.players) {
            if (player.level() == (Object) this) {
                ((com.mohistmc.silkard.injected.server.level.ContextServerPlayer) player).tickWeather();
            }
        }

        if (this.silkard$wasRaining != ((ServerLevel) (Object) this).isRaining()) {
            for (ServerPlayer player : this.players) {
                if (player.level() == (Object) this) {
                    ((com.mohistmc.silkard.injected.server.level.ContextServerPlayer) player).setPlayerWeather((!this.silkard$wasRaining ? org.bukkit.WeatherType.DOWNFALL : org.bukkit.WeatherType.CLEAR), false);
                }
            }
        }

        for (ServerPlayer player : this.players) {
            if (player.level() == (Object) this) {
                ((com.mohistmc.silkard.injected.server.level.ContextServerPlayer) player).updateWeather(this.silkard$oldRain, ((ServerLevel) (Object) this).getRainLevel(1.0F), this.silkard$oldThunder, ((ServerLevel) (Object) this).getThunderLevel(1.0F));
            }
        }
    }

    /**
     * @author Mgazul
     * @reason CraftBukkit - do not reset durations when weather stopped by sleeping
     */
    @Overwrite
    public void resetWeatherCycle() {
        WeatherData weatherdata = ((ServerLevel) (Object) this).getWeatherData();

        weatherdata.setRaining(false);
        if (!weatherdata.isRaining()) {
            weatherdata.setRainTime(0);
        }
        weatherdata.setThundering(false);
        if (!weatherdata.isThundering()) {
            weatherdata.setThunderTime(0);
        }
    }

    // CraftBukkit start - save hooks
    @Inject(method = "save", at = @At("HEAD"))
    private void silkard$worldSave(ProgressListener progressListener, boolean flush, boolean noSave, CallbackInfo ci) {
        if (!noSave) {
            Bukkit.getPluginManager().callEvent(new WorldSaveEvent(((ServerLevel) (Object) this).getWorld()));
            // A dimension of the main save keeps its own data as paper:level_overrides saved data
            // (written by the data storage below); the save's level.dat is written by the server.
            if (this.serverLevelData instanceof net.minecraft.world.level.storage.WorldData worldData) {
                com.mojang.authlib.GameProfile gameprofile = ((ServerLevel) (Object) this).getServer().getSingleplayerProfile();
                this.storageSource.saveDataTag(worldData, gameprofile == null ? null : gameprofile.id());
            }
            if (flush) {
                this.getDataStorage().saveAndJoin();
            } else {
                this.getDataStorage().scheduleSave();
            }
        }
    }
    // CraftBukkit end

    /**
     * @author Mgazul
     * @reason CraftBukkit - nullable remove cause on immediately removing a player
     */
    @Overwrite
    public void removePlayerImmediately(ServerPlayer player, Entity.RemovalReason reason) {
        ((ContextEntity) player).setRemoved(reason, null);
    }

    /**
     * @author Mgazul
     * @reason CraftBukkit - explode0 entry used by CraftWorld.createExplosion
     */
    @Overwrite
    public void explode(Entity source, DamageSource damageSource, ExplosionDamageCalculator damageCalculator, double x, double y, double z, float r, boolean fire, Level.ExplosionInteraction interactionType, ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles, WeightedList<ExplosionParticleInfo> blockParticles, Holder<SoundEvent> explosionSound) {
        this.explode0(source, damageSource, damageCalculator, x, y, z, r, fire, interactionType, smallExplosionParticles, largeExplosionParticles, blockParticles, explosionSound);
    }

    @Override
    public net.minecraft.world.level.ServerExplosion explode0(Entity source, DamageSource damageSource, ExplosionDamageCalculator damageCalculator, double x, double y, double z, float r, boolean fire, Level.ExplosionInteraction interactionType, ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles, WeightedList<ExplosionParticleInfo> blockParticles, Holder<SoundEvent> explosionSound) {
        Explosion.BlockInteraction blockInteraction = switch (interactionType) {
            case NONE -> Explosion.BlockInteraction.KEEP;
            case BLOCK -> this.getDestroyType(GameRules.BLOCK_EXPLOSION_DROP_DECAY);
            case MOB -> ((Boolean) ((ServerLevel) (Object) this).getGameRules().get(GameRules.MOB_GRIEFING)) ? this.getDestroyType(GameRules.MOB_EXPLOSION_DROP_DECAY) : Explosion.BlockInteraction.KEEP;
            case TNT -> this.getDestroyType(GameRules.TNT_EXPLOSION_DROP_DECAY);
            case TRIGGER -> Explosion.BlockInteraction.TRIGGER_BLOCK;
            default -> Explosion.BlockInteraction.DESTROY; // STANDARD
        };

        Vec3 vec3 = new Vec3(x, y, z);
        net.minecraft.world.level.ServerExplosion serverexplosion = new net.minecraft.world.level.ServerExplosion((ServerLevel) (Object) this, source, damageSource, damageCalculator, vec3, r, fire, blockInteraction);
        int i = serverexplosion.explode();

        if (((ContextExplosion) (Object) serverexplosion).silkard$wasCanceled()) {
            return serverexplosion;
        }

        ParticleOptions particleoptions2 = serverexplosion.isSmall() ? smallExplosionParticles : largeExplosionParticles;

        for (ServerPlayer serverplayer : this.players) {
            if (serverplayer.distanceToSqr(vec3) < 4096.0D) {
                serverplayer.connection.send(new net.minecraft.network.protocol.game.ClientboundExplodePacket(vec3, r, i, java.util.Optional.ofNullable(serverexplosion.getHitPlayers().get(serverplayer)), particleoptions2, explosionSound, blockParticles, source == null || !source.isSilent()));
            }
        }

        return serverexplosion;
    }

    @Shadow
    private Explosion.BlockInteraction getDestroyType(net.minecraft.world.level.gamerules.GameRule<Boolean> gameRule) { throw new AssertionError(); }

    public SavedDataStorage getWorldContainerDataStorage() {
        return this.getDataStorage();
    }

    @Override
    public net.minecraft.world.level.storage.PrimaryLevelData silkard$serverLevelDataCB() {
        // A dimension of the main save has its own paper:level_overrides; the rest of its data is the save's.
        return this.serverLevelData instanceof net.minecraft.world.level.storage.PrimaryLevelData data ? data : (net.minecraft.world.level.storage.PrimaryLevelData) this.getServer().getWorldData();
    }

    /**
     * @author Mgazul
     * @reason CraftBukkit - per world respawn data
     */
    @Overwrite
    public void setRespawnData(LevelData.RespawnData respawnData) {
        ((com.mohistmc.silkard.injected.server.ContextMinecraftServer) ((ServerLevel) (Object) this).getServer()).silkard$setRespawnData(respawnData, (ServerLevel) (Object) this);
    }

    /**
     * @author Mgazul
     * @reason CraftBukkit - per world respawn data
     */
    @Overwrite
    public LevelData.RespawnData getRespawnData() {
        return this.serverLevelData.getRespawnData();
    }

    /**
     * @author Mgazul
     * @reason CraftBukkit - read the level's own level data
     */
    @Overwrite
    public boolean isFlat() {
        if (!(this.serverLevelData instanceof net.minecraft.world.level.storage.PrimaryLevelData data)) {
            return this.getWorldGenSettings().dimensions().get(this.getTypeKey()).map(levelStem -> levelStem.generator() instanceof net.minecraft.world.level.levelgen.FlatLevelSource).orElse(false); // Paper
        }
        return data.isFlatWorld();
    }

    /**
     * @author Mgazul
     * @reason CraftBukkit - read the level's own worldgen settings
     */
    @Overwrite
    public long getSeed() {
        return this.getWorldGenSettings().options().seed();
    }

    /**
     * @author Mgazul
     * @reason CraftBukkit - per world game rules
     */
    @Overwrite
    public GameRules getGameRules() {
        return this.gameRules != null ? this.gameRules : ((ServerLevel) (Object) this).getServer().getGameRules();
    }

    /**
     * @author Mgazul
     * @reason CraftBukkit - per world weather data
     */
    @Overwrite
    public WeatherData getWeatherData() {
        return this.weatherData != null ? this.weatherData : ((ServerLevel) (Object) this).getServer().getWeatherData();
    }

    @Unique
    private boolean silkard$savedDataClosed;

    @Inject(method = "close", at = @At("TAIL"))
    private void silkard$closeDataStorage(CallbackInfo ci) {
        if (this.savedDataStorage != null && !this.silkard$savedDataClosed) {
            this.silkard$savedDataClosed = true;
            try {
                this.savedDataStorage.close();
            } catch (IllegalStateException ignored) {
                // already closed by another owner
            }
        }
    }

    /**
     * @author Mgazul
     * @reason CraftBukkit - compare level stems instead of dimensions
     */
    @Overwrite
    public boolean isAllowedToEnterPortal(Level toLevel) {
        return toLevel.getTypeKey() == LevelStem.NETHER ? (Boolean) this.getGameRules().get(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS) : true;
    }

    /**
     * @author Mgazul
     * @reason CraftBukkit - visibility aware block destruction packet
     */
    @Overwrite
    public void destroyBlockProgress(int id, BlockPos blockPos, int progress) {
        net.minecraft.world.entity.player.Player player = null;
        Entity entity = ((ServerLevel) (Object) this).getEntity(id);
        if (entity instanceof net.minecraft.world.entity.player.Player) {
            player = (net.minecraft.world.entity.player.Player) entity;
        }

        for (ServerPlayer serverplayer : ((ServerLevel) (Object) this).getServer().getPlayerList().getPlayers()) {
            if (serverplayer.level() == (Object) this && serverplayer.getId() != id) {
                double d0 = (double) blockPos.getX() - serverplayer.getX();
                double d1 = (double) blockPos.getY() - serverplayer.getY();
                double d2 = (double) blockPos.getZ() - serverplayer.getZ();

                if (player != null && !serverplayer.getBukkitEntity().canSee(((com.mohistmc.silkard.injected.server.level.ContextServerPlayer) player).getBukkitEntity())) {
                    continue;
                }

                if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0D) {
                    serverplayer.connection.send(new ClientboundBlockDestructionPacket(id, blockPos, progress));
                }
            }
        }
    }

    // CraftBukkit start - particles with a sending player
    @Override
    public <T extends ParticleOptions> int sendParticlesSource(ServerPlayer sender, T particle, boolean overrideLimiter, boolean alwaysShow, double x, double y, double z, int count, double xDist, double yDist, double zDist, double speed) {
        ClientboundLevelParticlesPacket clientboundlevelparticlespacket = new ClientboundLevelParticlesPacket(particle, overrideLimiter, alwaysShow, x, y, z, (float) xDist, (float) yDist, (float) zDist, (float) speed, count);
        int j = 0;

        for (int k = 0; k < this.players.size(); ++k) {
            ServerPlayer serverplayer = this.players.get(k);
            if (sender != null && !serverplayer.getBukkitEntity().canSee(sender.getBukkitEntity())) {
                continue;
            }

            if (((ServerLevel) (Object) this).sendParticles(serverplayer, overrideLimiter, x, y, z, clientboundlevelparticlespacket)) {
                ++j;
            }
        }

        return j;
    }
    // CraftBukkit end

    // CraftBukkit start
    @Override
    public boolean strikeLightning(Entity entity) {
        return this.strikeLightning(entity, LightningStrikeEvent.Cause.UNKNOWN);
    }

    @Override
    public boolean strikeLightning(Entity entity, LightningStrikeEvent.Cause cause) {
        LightningStrikeEvent event = CraftEventFactory.callLightningStrikeEvent((LightningStrike) entity.getBukkitEntity(), cause);
        if (event.isCancelled()) {
            return false;
        }
        return ((ServerLevel) (Object) this).addFreshEntity(entity);
    }
    // CraftBukkit end

    // Paper start - Call missing map initialize event and set id
    // A map read from disk has no id of its own; it gets one, and its MapInitializeEvent, the
    // first time it is looked up.
    @Inject(method = "getMapData", at = @At("RETURN"))
    private void silkard$initMapData(net.minecraft.world.level.saveddata.maps.MapId id, CallbackInfoReturnable<net.minecraft.world.level.saveddata.maps.MapItemSavedData> cir) {
        net.minecraft.world.level.saveddata.maps.MapItemSavedData mapData = cir.getReturnValue();
        if (mapData != null && mapData.silkard_id() == null) {
            mapData.silkard_id(id);
            new org.bukkit.event.server.MapInitializeEvent(mapData.silkard_mapView()).callEvent();
        }
    }

    @Inject(method = "setMapData", at = @At("HEAD"))
    private void silkard$setMapDataId(net.minecraft.world.level.saveddata.maps.MapId id, net.minecraft.world.level.saveddata.maps.MapItemSavedData data, CallbackInfo ci) {
        data.silkard_id(id);
        new org.bukkit.event.server.MapInitializeEvent(data.silkard_mapView()).callEvent();
    }
    // Paper end - Call missing map initialize event and set id
}