package me.epicgodmc.blockstackerx.listeners;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.enumerators.Permission;
import me.epicgodmc.blockstackerx.support.holograms.StackerHologram;
import me.epicgodmc.blockstackerx.utils.Utils;
import me.epicgodmc.epicapi.util.SimpleLocation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;


public class StackerPlaceEvent implements Listener {

    private final BlockStackerX plugin;

    public StackerPlaceEvent(BlockStackerX plugin) {
        this.plugin = plugin;
        plugin.registerListener(this, "Track Stacker placements");
    }

    @EventHandler
    public void place(BlockPlaceEvent e) {
        Block placedBlock = e.getBlockPlaced();
        ItemStack hand = e.getItemInHand();

        String type = Utils.getType(hand);
        String state = Utils.getStackerState(hand);


        if (!type.equals("n/a") || state.equalsIgnoreCase("ITEM")) {
            if (plugin.getDependencyManager().isWorldguard()) {
                if (!plugin.getDependencyManager().getWorldGuardHook().canBuild(e.getPlayer(), placedBlock.getLocation())) {
                    return;
                }
            }
            Player player = e.getPlayer();
            if (Permission.STACKER_PLACE.has(player, true)) {
                StackerHologram hologram = plugin.getDependencyManager().getNewHologram(plugin);
                if (hologram != null) {
                    if (state.equalsIgnoreCase("BLOCK")) {
                        float[] offset = plugin.getStackerSettings().getDisplayOffset(type);
                        Location holoLocation = e.getBlock().getLocation().add(offset[0], offset[1], offset[2]);
                        hologram.create(type, 0, holoLocation);
                        StackerBlock s = new StackerBlock(plugin, player.getUniqueId(), type, hologram, new SimpleLocation(placedBlock.getLocation()));
                        plugin.getStackerStore().setStack(placedBlock.getLocation(), s);
                    } else if (state.equalsIgnoreCase("ITEM")) {
                        Map<String, Object> data = Utils.getStackerItemData(hand);

                        String sType = (String) data.get("type");
                        Material material = (Material) data.get("material");
                        int value = (int) data.get("value");

                        float[] offset = plugin.getStackerSettings().getDisplayOffset(sType);
                        Location holoLocation = e.getBlock().getLocation().add(offset[0], offset[1], offset[2]);

                        hologram.create(sType, value, holoLocation);
                        StackerBlock s = new StackerBlock(plugin, 0, player.getUniqueId(), sType, hologram, new SimpleLocation(placedBlock.getLocation()), material, value);
                        plugin.getStackerStore().setStack(placedBlock.getLocation(), s);
                    }
                }
            } else {
                e.setCancelled(true);
                plugin.getLangSettings().sendText(player, "noPermission", true);
            }

        }
    }
}

