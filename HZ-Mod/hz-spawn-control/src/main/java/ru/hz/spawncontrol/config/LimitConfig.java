package ru.hz.spawncontrol.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ru.hz.spawncontrol.HzSpawnControl;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.Map;

public final class LimitConfig {
    public record Rule(int localLimit, double chance, double horizontalRadius, double verticalRadius) {}
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<Map<String, Rule>>(){}.getType();
    private static volatile Map<String, Rule> rules = Collections.emptyMap();

    public static Rule get(String id) { return rules.get(id); }

    public static void load() {
        Path path = Paths.get("config", "hz_spawn_control", "limits.json");
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) {
                try (InputStream in = LimitConfig.class.getResourceAsStream("/default_limits.json")) {
                    if (in == null) throw new IOException("default_limits.json missing");
                    Files.copy(in, path);
                }
            }
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                Map<String, Rule> loaded = GSON.fromJson(reader, TYPE);
                rules = loaded == null ? Collections.emptyMap() : Map.copyOf(loaded);
            }
            HzSpawnControl.LOGGER.info("Loaded {} local spawn-limit rules", rules.size());
        } catch (Exception e) {
            rules = Collections.emptyMap();
            HzSpawnControl.LOGGER.error("Failed to load limits.json; limits disabled to keep spawning safe", e);
        }
    }
    private LimitConfig() {}
}
