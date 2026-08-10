package ru.hz.spawncontrol.spawn;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.registries.ForgeRegistries;
import ru.hz.spawncontrol.HzSpawnControl;
import java.util.*;

public final class SpawnPlacementHandler {
    private static final Map<String, Set<String>> EXTRA_BIOMES = new HashMap<>();
    static {
        put("minecraft:sheep", "minecraft:desert","minecraft:badlands");
        put("minecraft:donkey", "minecraft:desert","minecraft:badlands");
        put("minecraft:camel", "minecraft:desert","minecraft:badlands");
        put("minecraft:llama", "minecraft:stony_peaks");
        put("minecraft:goat", "minecraft:windswept_hills");
        put("minecraft:rabbit", "minecraft:badlands","minecraft:jungle","minecraft:plains");
        put("minecraft:turtle", "minecraft:snowy_beach","minecraft:stony_shore");
        put("minecraft:wolf", "minecraft:windswept_gravelly_hills","minecraft:savanna","minecraft:badlands");
        put("minecraft:fox", "minecraft:forest");
        put("minecraft:armadillo", "minecraft:desert");
    }
    private static void put(String id, String... biomes) { EXTRA_BIOMES.put(id, Set.of(biomes)); }

    public static void register(SpawnPlacementRegisterEvent event) {
        EXTRA_BIOMES.keySet().forEach(id -> {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(id));
            if (type == null) { HzSpawnControl.LOGGER.warn("Surface rule skipped: {} missing", id); return; }
            registerOne(event, type);
        });
        HzSpawnControl.LOGGER.info("Registered {} additive spawn-placement rules", EXTRA_BIOMES.size());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerOne(SpawnPlacementRegisterEvent event, EntityType<?> raw) {
        EntityType<? extends Mob> type = (EntityType<? extends Mob>) raw;
        // Для OR Forge требует null type и null heightmap; родные значения сущности сохраняются.
        event.register((EntityType) type, null, null,
                (entityType, level, reason, pos, random) -> test(entityType, level, reason, pos, random),
                SpawnPlacementRegisterEvent.Operation.OR);
    }

    private static boolean test(EntityType<?> type, ServerLevelAccessor level, MobSpawnType reason,
                                BlockPos pos, RandomSource random) {
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) return false;
        var entityId = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (entityId == null) return false;
        Optional<ResourceKey<Biome>> biomeKey = level.getBiome(pos).unwrapKey();
        if (biomeKey.isEmpty()) return false;
        String biome = biomeKey.get().location().toString();
        Set<String> permitted = EXTRA_BIOMES.get(entityId.toString());
        if (permitted == null || !permitted.contains(biome)) return false;
        if (level.getRawBrightness(pos, 0) < 9) return false;
        if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.above()).isEmpty()) return false;
        if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) return false;
        if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) return false;
        BlockState below = level.getBlockState(pos.below());
        return naturalSurface(biome, below);
    }

    private static boolean naturalSurface(String biome, BlockState s) {
        if (biome.equals("minecraft:desert"))
            return s.is(Blocks.SAND) || s.is(Blocks.SANDSTONE) || s.is(Blocks.CUT_SANDSTONE) || s.is(Blocks.SMOOTH_SANDSTONE);
        if (biome.equals("minecraft:badlands"))
            return s.is(Blocks.RED_SAND) || s.is(Blocks.RED_SANDSTONE) || s.is(Blocks.TERRACOTTA) || s.is(BlockTags.TERRACOTTA) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.GRASS_BLOCK);
        if (biome.equals("minecraft:stony_peaks"))
            return s.is(Blocks.STONE) || s.is(Blocks.CALCITE) || s.is(Blocks.GRAVEL) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT);
        if (biome.equals("minecraft:windswept_hills") || biome.equals("minecraft:windswept_gravelly_hills"))
            return s.is(Blocks.STONE) || s.is(Blocks.GRAVEL) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.COARSE_DIRT);
        if (biome.equals("minecraft:snowy_beach"))
            return s.is(Blocks.SAND) || s.is(Blocks.GRAVEL) || s.is(Blocks.STONE) || s.is(Blocks.SNOW_BLOCK);
        if (biome.equals("minecraft:stony_shore"))
            return s.is(Blocks.STONE) || s.is(Blocks.GRAVEL) || s.is(Blocks.ANDESITE) || s.is(Blocks.DIORITE) || s.is(Blocks.GRANITE);
        return s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.PODZOL) || s.is(Blocks.ROOTED_DIRT) || s.is(Blocks.MOSS_BLOCK);
    }
    private SpawnPlacementHandler() {}
}
