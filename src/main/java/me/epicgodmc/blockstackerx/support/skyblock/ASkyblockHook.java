package me.epicgodmc.blockstackerx.support.skyblock;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.events.IslandPreLevelEvent;
import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.enumerators.Permission;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class ASkyblockHook implements SkyblockHook, Listener {

    private final BlockStackerX plugin;
    private final ASkyBlockAPI api = ASkyBlockAPI.getInstance();

    public ASkyblockHook(BlockStackerX plugin) {
        this.plugin = plugin;
        plugin.registerListener(this, "Calculate ASkyblock island levels");
    }


    @Override
    public Collection<UUID> getTeam(World world, UUID uuid) {
        if (api.hasIsland(uuid)) {
            return api.getTeamMembers(uuid);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean hasSameTeam(World world, UUID p1, UUID p2) {
        if (api.hasIsland(p1) && api.hasIsland(p2)) {
            return api.getTeamMembers(p1).contains(p2);
        }
        return false;
    }

    @Override
    public boolean canModifyStacker(StackerBlock stacker, Player player) {
        return stacker.getOwner().equals(player.getUniqueId())
                || plugin.getStackerSettings().getDoTeamStacking(stacker.getType())
                && this.hasSameTeam(stacker.getLocation().toBukkitLoc().getWorld(), stacker.getOwner(), player.getUniqueId())
                || Permission.BYPASS.has(player, false);
    }


    @EventHandler
    public void calculate(IslandPreLevelEvent e) {
        long level = e.getLongLevel();
        UUID uuid = e.getPlayer();
        Collection<UUID> team = this.getTeam(e.getIslandLocation().getWorld(), e.getIslandOwner());
        HashMap<Location, StackerBlock> stackers = plugin.getStackerStore().collectStackers(uuid, team);

        for (StackerBlock stacker : stackers.values()) {
            if (stacker.hasStackMaterial()) {
                double newLevels = stacker.calculateLevels();
                level += newLevels;
            }
        }
        e.setLevel((int) level);
    }

}
