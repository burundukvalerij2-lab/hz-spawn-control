package ru.hz.spawncontrol;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import ru.hz.spawncontrol.biome.ModBiomeModifiers;
import ru.hz.spawncontrol.config.LimitConfig;
import ru.hz.spawncontrol.spawn.SpawnLimitHandler;
import ru.hz.spawncontrol.spawn.SpawnPlacementHandler;

@Mod(HzSpawnControl.MODID)
public final class HzSpawnControl {
    public static final String MODID = "hz_spawn_control";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HzSpawnControl() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBiomeModifiers.REGISTER.register(modBus);
        modBus.addListener(SpawnPlacementHandler::register);
        MinecraftForge.EVENT_BUS.register(SpawnLimitHandler.class);
        MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
    }

    private void serverAboutToStart(ServerAboutToStartEvent event) {
        LimitConfig.load();
    }
}
