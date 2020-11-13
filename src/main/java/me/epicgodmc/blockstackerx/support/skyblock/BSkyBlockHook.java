package me.epicgodmc.blockstackerx.support.skyblock;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.enumerators.Permission;
import org.bukkit.Location;
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
        plugin.registerListener(this, null);
    }

    @Override
    public Collection<UUID> getTeam(UUID uuid) {
        @NonNull Optional<Island> optionalIsland = BentoBox.getInstance().getIslands().getIslandById(uuid.toString());
        if (optionalIsland.isPresent()) {
            return optionalIsland.get().getMembers().keySet();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean hasSameTeam(UUID p1, UUID p2) {
        @NonNull Optional<Island> optionalIsland = BentoBox.getInstance().getIslands().getIslandById(p1.toString());
        if (optionalIsland.isPresent()) {
            Island island = optionalIsland.get();
            return island.getMembers().containsKey(p2);
        }
        return false;
    }

    @Override
    public boolean canModifyStacker(StackerBlock stacker, Player player) {
        return stacker.getOwner().equals(player.getUniqueId())
                || plugin.getSettings().getDoTeamStacking(stacker.getType())
                && this.hasSameTeam(stacker.getOwner(), player.getUniqueId())
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
