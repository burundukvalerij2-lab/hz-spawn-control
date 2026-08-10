package ru.hz.spawncontrol.spawn;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import ru.hz.spawncontrol.config.LimitConfig;

public final class SpawnLimitHandler {
    @SubscribeEvent
    public static void checkPosition(MobSpawnEvent.PositionCheck event) {
        MobSpawnType reason = event.getSpawnType();
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) return;
        Mob mob = event.getEntity();
        if (!(mob.level() instanceof ServerLevel level)) return;
        var key = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (key == null) return;
        LimitConfig.Rule rule = LimitConfig.get(key.toString());
        if (rule == null) return;

        if (rule.chance() < 1.0 && mob.getRandom().nextDouble() >= Math.max(0.0, rule.chance())) {
            event.setResult(Event.Result.DENY);
            return;
        }
        if (rule.localLimit() <= 0) return;
        AABB box = new AABB(mob.blockPosition()).inflate(rule.horizontalRadius(), rule.verticalRadius(), rule.horizontalRadius());
        long count = level.getEntitiesOfClass(Mob.class, box,
                other -> other.isAlive() && other.getType() == mob.getType()).stream()
                .limit(rule.localLimit()).count();
        if (count >= rule.localLimit()) event.setResult(Event.Result.DENY);
    }
    private SpawnLimitHandler() {}
}
