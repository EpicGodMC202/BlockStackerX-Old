package me.epicgodmc.blockstackerx.support;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.epicframework.util.NmsMethods;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardHook {

    private final BlockStackerX plugin;

    public WorldGuardHook(BlockStackerX plugin) {
        this.plugin = plugin;
    }

    public boolean canBuild(Player player, Location location) {
        return NmsMethods.canBuild(player, location);
    }

}
