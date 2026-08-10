package ru.hz.spawncontrol.biome;

import com.mojang.serialization.Codec;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.hz.spawncontrol.HzSpawnControl;

public final class ModBiomeModifiers {
    public static final DeferredRegister<Codec<? extends BiomeModifier>> REGISTER =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, HzSpawnControl.MODID);
    public static final RegistryObject<Codec<? extends BiomeModifier>> REPLACE_SPAWN =
            REGISTER.register("replace_spawn", () -> ReplaceSpawnBiomeModifier.CODEC);
    private ModBiomeModifiers() {}
}
