package ru.hz.spawncontrol.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.ForgeRegistries;

public record ReplaceSpawnBiomeModifier(HolderSet<Biome> biomes, EntityType<?> entityType,
                                        int weight, int minCount, int maxCount) implements BiomeModifier {
    public static final Codec<ReplaceSpawnBiomeModifier> CODEC = RecordCodecBuilder.create(i -> i.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(ReplaceSpawnBiomeModifier::biomes),
            ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("entity_type").forGetter(ReplaceSpawnBiomeModifier::entityType),
            Codec.intRange(1, 10000).fieldOf("weight").forGetter(ReplaceSpawnBiomeModifier::weight),
            Codec.intRange(1, 64).fieldOf("min_count").forGetter(ReplaceSpawnBiomeModifier::minCount),
            Codec.intRange(1, 64).fieldOf("max_count").forGetter(ReplaceSpawnBiomeModifier::maxCount)
    ).apply(i, ReplaceSpawnBiomeModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        // Выполняем замену атомарно в последней стандартной фазе: сначала удаляем все старые
        // записи этого EntityType, затем добавляем ровно одну запись в разрешённом биоме.
        if (phase != Phase.REMOVALS) return;
        var category = entityType.getCategory();
        var spawns = builder.getMobSpawnSettings();
        spawns.getSpawner(category).removeIf(data -> data.type == entityType);
        if (biomes.contains(biome)) {
            spawns.addSpawn(category, new MobSpawnSettings.SpawnerData(entityType, weight, minCount, maxCount));
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return ModBiomeModifiers.REPLACE_SPAWN.get();
    }
}
