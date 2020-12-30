package me.epicgodmc.blockstackerx.listeners;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.enumerators.Permission;
import me.epicgodmc.blockstackerx.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class StackerRemoveEvent implements Listener {

    private final BlockStackerX plugin;

    public StackerRemoveEvent(BlockStackerX plugin) {
        this.plugin = plugin;
        plugin.registerListener(this, "Handle breaking stackers");
    }


    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Location l = e.getBlock().getLocation();
        if (plugin.getStackerStore().contains(l)) {
            Player player = e.getPlayer();
            StackerBlock stacker = plugin.getStackerStore().getStacker(l);
            if (plugin.getDependencyManager().getSkyblockHook().canModifyStacker(stacker, player)) {
                if (Permission.STACKER_BREAK.has(player, false)) {
                    Material mat = player.getItemInHand().getType();
                    if (Utils.isPickaxe(mat)) {
                        e.setCancelled(true);
                        player.getInventory().addItem(plugin.getStackerSettings().getStackerItem(stacker).build());
                        stacker.delete();
                        plugin.getLangSettings().sendText(player, "stackerBreak", true);

                    } else {
                        e.setCancelled(true);
                        plugin.getLangSettings().sendText(player, "usePickaxe", true);

                    }
                } else {
                    e.setCancelled(true);
                    plugin.getLangSettings().sendText(player, "stackerNoPermission", true);
                }
            } else {
                e.setCancelled(true);
                plugin.getLangSettings().sendText(player, "stackerNoPermission", true);
            }
        }
    }
}
