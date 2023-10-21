package com.aviatorrob06.disx;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;


public class DisxJukeboxUsageCooldownManager {
    private static final Map<BlockPos, Integer> COOLDOWNS = new HashMap<>();
    private static final int COOLDOWN_TIME = 20; // Cooldown time in ticks (20 ticks = 1 second)

    public static void updateCooldown(BlockPos pos) {
        COOLDOWNS.put(pos, COOLDOWN_TIME);
    }

    public static void tickCooldowns() {
        COOLDOWNS.replaceAll((pos, timeLeft) -> timeLeft > 0 ? timeLeft - 1 : 0);
        COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    public static boolean isOnCooldown(BlockPos pos) {
        return COOLDOWNS.containsKey(pos);
    }
}
