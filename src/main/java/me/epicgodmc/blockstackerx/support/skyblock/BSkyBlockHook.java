package me.epicgodmc.blockstackerx.support.skyblock;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.enumerators.Permission;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.eclipse.jdt.annotation.NonNull;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.events.BentoBoxEvent;
import world.bentobox.bentobox.database.objects.Island;

import java.util.*;

public class BSkyBlockHook implements SkyblockHook, Listener {

    private final BlockStackerX plugin;

    public BSkyBlockHook(BlockStackerX plugin) {
        this.plugin = plugin;
        plugin.registerListener(this, "Calculate BSkyBlock island levels");
    }

    @Override
    public Collection<UUID> getTeam(World world, UUID uuid) {
        Island island = BentoBox.getInstance().getIslands().getIsland(world, uuid);
        if (island != null)
        {
            return island.getMemberSet();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean hasSameTeam(World world, UUID p1, UUID p2) {
        return getTeam(world, p1).contains(p2);
    }

    @Override
    public boolean canModifyStacker(StackerBlock stacker, Player player) {
        return stacker.getOwner().equals(player.getUniqueId())
                || plugin.getStackerSettings().getDoTeamStacking(stacker.getType())
                && this.hasSameTeam(stacker.getLocation().toBukkitLoc().getWorld(), stacker.getOwner(), player.getUniqueId())
                || Permission.BYPASS.has(player, false);
    }


    @EventHandler
    public void calculate(BentoBoxEvent e) {
        Map<String, Object> keyValues = e.getKeyValues();
        if ("IslandLevelCalculatedEvent".equals(e.getEventName())) {
            Island island = (Island) keyValues.get("island");
            long level = (long) keyValues.get("level");
            Set<UUID> team = island.getMembers().keySet();
            HashMap<Location, StackerBlock> stackers = plugin.getStackerStore().collectStackers(null, team);

            for (StackerBlock stacker : stackers.values()) {
                if (stacker.hasStackMaterial()) {
                    double newLevels = stacker.calculateLevels();
                    level += newLevels;
                }
            }
            keyValues.put("level", level);
            e.setKeyValues(keyValues);
        }
    }
}
