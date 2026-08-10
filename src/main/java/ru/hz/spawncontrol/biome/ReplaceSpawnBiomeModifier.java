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

/**
 * Атомарно заменяет записи одного EntityType.
 *
 * В Forge 1.20.1 фазы называются ADD и REMOVE, а не ADDITIONS/REMOVALS.
 * Операция выполняется в REMOVE: старые записи удаляются, после чего в
 * разрешённый биом добавляется ровно одна итоговая запись.
 */
public record ReplaceSpawnBiomeModifier(
        HolderSet<Biome> biomes,
        EntityType<?> entityType,
        int weight,
        int minCount,
        int maxCount
) implements BiomeModifier {

    public static final Codec<ReplaceSpawnBiomeModifier> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Biome.LIST_CODEC
                            .fieldOf("biomes")
                            .forGetter(ReplaceSpawnBiomeModifier::biomes),
                    ForgeRegistries.ENTITY_TYPES.getCodec()
                            .fieldOf("entity_type")
                            .forGetter(ReplaceSpawnBiomeModifier::entityType),
                    Codec.intRange(1, 10000)
                            .fieldOf("weight")
                            .forGetter(ReplaceSpawnBiomeModifier::weight),
                    Codec.intRange(1, 64)
                            .fieldOf("min_count")
                            .forGetter(ReplaceSpawnBiomeModifier::minCount),
                    Codec.intRange(1, 64)
                            .fieldOf("max_count")
                            .forGetter(ReplaceSpawnBiomeModifier::maxCount)
            ).apply(instance, ReplaceSpawnBiomeModifier::new)
    );

    @Override
    public void modify(
            Holder<Biome> biome,
            Phase phase,
            ModifiableBiomeInfo.BiomeInfo.Builder builder
    ) {
        // Правильное имя фазы в Forge 47.x — REMOVE.
        if (phase != Phase.REMOVE) {
            return;
        }

        var category = entityType.getCategory();
        var spawnBuilder = builder.getMobSpawnSettings();

        // Удаляем все исходные и ранее добавленные записи этого вида.
        spawnBuilder.getSpawner(category)
                .removeIf(spawnerData -> spawnerData.type == entityType);

        // Возвращаем одну итоговую запись только в разрешённые биомы.
        if (biomes.contains(biome)) {
            spawnBuilder.addSpawn(
                    category,
                    new MobSpawnSettings.SpawnerData(
                            entityType,
                            weight,
                            minCount,
                            maxCount
                    )
            );
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return ModBiomeModifiers.REPLACE_SPAWN.get();
    }
}
