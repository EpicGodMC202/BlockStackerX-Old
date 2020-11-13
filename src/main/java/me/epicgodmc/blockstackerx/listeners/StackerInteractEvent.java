package me.epicgodmc.blockstackerx.listeners;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.enumerators.Permission;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.utils.Settings;
import me.epicgodmc.blockstackerx.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class StackerInteractEvent implements Listener {

    private final BlockStackerX plugin;
    private final Settings settings;

    public StackerInteractEvent(BlockStackerX plugin) {
        this.plugin = plugin;
        this.settings = plugin.getSettings();

        plugin.registerListener(this, "Handle Stacker Interaction");
    }

    @EventHandler
    public void interact(PlayerInteractEvent e) {
        Action action = e.getAction();

        if (action == settings.getAddAction()) {
            handleAdd(e);
            return;
        }
        if (action == settings.getSubtractAction()) handleSub(e);
    }


    private void handleAdd(PlayerInteractEvent e) {
        Location l = e.getClickedBlock().getLocation();
        if (plugin.getStackerStore().contains(l)) {
            StackerBlock stacker = plugin.getStackerStore().getStacker(l);
            try {
                if (e.getHand().equals(EquipmentSlot.OFF_HAND)) {
                    e.setCancelled(true);
                    return;
                }
            } catch (NoSuchMethodError ignored) {
            }
            ItemStack hand = e.getPlayer().getItemInHand();
            if (Utils.isBlock(hand)) {
                e.setCancelled(true);
                Material material = hand.getType();
                if (Permission.STACKER_ADD_BLOCKS.has(e.getPlayer(), false)) {
                    if (plugin.getSettings().getAvailableMaterials(stacker.getType()).contains(material)) {
                        if (!Utils.isStacker(hand)) {
                            if (plugin.getDependencyManager().getSkyblockHook().canModifyStacker(stacker, e.getPlayer())) {
                                if (stacker.getValue() == 0) {
                                    stacker.setStackMaterial(material);
                                    plugin.getLangManager().getText("materialChosen").replaceText("%s", material.toString().toLowerCase()).send(e.getPlayer(), true);
                                }
                                if (stacker.getStackMaterial().equals(material)) {
                                    if (!e.getPlayer().isSneaking()) {
                                        if (stacker.canAddValue(1)) {
                                            int subAmt = Utils.subtractPlayerHandAmount(e.getPlayer(), 1);
                                            stacker.incrementValue(subAmt);
                                            plugin.getLangManager().getText("valueAdded").replaceInteger("%1", subAmt).replaceText("%2", material.toString().toLowerCase()).send(e.getPlayer(), true);
                                        } else
                                            plugin.getLangManager().getText("maxStorageReached").replaceInteger("%s", plugin.getSettings().getMaxStorage(stacker.getType())).send(e.getPlayer(), true);
                                    } else {
                                        if (stacker.canAddValue(e.getPlayer().getItemInHand().getAmount())) {
                                            int subAmt = Utils.subtractPlayerHandAmount(e.getPlayer(), 64);
                                            stacker.incrementValue(subAmt);
                                            plugin.getLangManager().getText("valueAdded").replaceInteger("%1", subAmt).replaceText("%2", material.toString().toLowerCase()).send(e.getPlayer(), true);
                                        } else if (stacker.getStorageLeft() >= 1) {
                                            int storageLeft = stacker.getStorageLeft();
                                            int subAmt = Utils.subtractPlayerHandAmount(e.getPlayer(), storageLeft);
                                            stacker.incrementValue(subAmt);
                                            plugin.getLangManager().getText("valueAdded").replaceInteger("%1", subAmt).replaceText("%2", material.toString().toLowerCase()).send(e.getPlayer(), true);
                                        } else
                                            plugin.getLangManager().getText("maxStorageReached").replaceInteger("%s", plugin.getSettings().getMaxStorage(stacker.getType())).send(e.getPlayer(), true);
                                    }
                                } else
                                    plugin.getLangManager().getText("materialNoMatch").replaceText("%s", stacker.getStackMaterial().toString().toLowerCase()).send(e.getPlayer(), true);
                            } else plugin.getLangManager().getText("stackerNoPermission").send(e.getPlayer(), true);
                        } else plugin.getLangManager().getText("invalidBlock").send(e.getPlayer(), true);
                    } else plugin.getLangManager().getText("invalidBlock").send(e.getPlayer(), true);
                } else plugin.getLangManager().getText("stackerNoPermission").send(e.getPlayer(), true);
            } else {
                if (!plugin.getSettings().getLinkedGUI(stacker.getType()).equals("NA"))
                {
                    if (plugin.getDependencyManager().getSkyblockHook().canModifyStacker(stacker, e.getPlayer()))
                    {
                        plugin.getGuiManager().openStacker(e.getPlayer(), stacker);
                    }else plugin.getLangManager().getText("stackerNoPermission").send(e.getPlayer(), true);
                }else plugin.getLangManager().getText("invalidBlock").send(e.getPlayer(), true);
            }
        }


    }

    private void handleSub(PlayerInteractEvent e) {
        Location l = e.getClickedBlock().getLocation();
        if (plugin.getStackerStore().contains(l)) {
            StackerBlock stacker = plugin.getStackerStore().getStacker(l);
            if (stacker.canSubtractValue(1)) {
                if (plugin.getDependencyManager().getSkyblockHook().canModifyStacker(stacker, e.getPlayer())) {
                    if (Permission.STACKER_SUB_BLOCKS.has(e.getPlayer(), false)) {
                        if (!Utils.isPickaxe(e.getPlayer().getItemInHand().getType())) {
                            e.setCancelled(true);
                            if (!e.getPlayer().isSneaking()) {
                                if (Utils.hasAvailableSlot(e.getPlayer(), stacker.getStackMaterial(), 1)) {
                                    e.getPlayer().getInventory().addItem(new ItemStack(stacker.getStackMaterial(), 1));
                                    plugin.getLangManager().getText("valueSubtracted").replaceInteger("%1", 1).replaceText("%2", stacker.getStackMaterial().toString().toLowerCase()).send(e.getPlayer(), true);
                                    stacker.subtractValue(1);
                                } else plugin.getLangManager().getText("inventoryFull").send(e.getPlayer(), true);
                            } else {
                                if (stacker.canSubtractValue(64)) {
                                    if (Utils.hasAvailableSlot(e.getPlayer(), stacker.getStackMaterial(), 64)) {
                                        e.getPlayer().getInventory().addItem(new ItemStack(stacker.getStackMaterial(), 64));
                                        plugin.getLangManager().getText("valueSubtracted").replaceInteger("%1", 64).replaceText("%2", stacker.getStackMaterial().toString().toLowerCase()).send(e.getPlayer(), true);
                                        stacker.subtractValue(64);
                                    } else plugin.getLangManager().getText("inventoryFull").send(e.getPlayer(), true);
                                } else {
                                    if (Utils.hasAvailableSlot(e.getPlayer(), stacker.getStackMaterial(), stacker.getValue())) {
                                        e.getPlayer().getInventory().addItem(new ItemStack(stacker.getStackMaterial(), stacker.getValue()));
                                        plugin.getLangManager().getText("valueSubtracted").replaceInteger("%1", stacker.getValue()).replaceText("%2", stacker.getStackMaterial().toString().toLowerCase()).send(e.getPlayer(), true);
                                        stacker.setValue(0);
                                    } else plugin.getLangManager().getText("inventoryFull").send(e.getPlayer(), true);
                                }
                            }
                        }
                    } else {
                        e.setCancelled(true);
                        plugin.getLangManager().getText("stackerNoPermission").send(e.getPlayer(), true);
                    }
                } else {
                    e.setCancelled(true);
                    plugin.getLangManager().getText("stackerNoPermission").send(e.getPlayer(), true);
                }
            } else {
                e.setCancelled(true);
                plugin.getLangManager().getText("minStorageReached").send(e.getPlayer(), true);
            }
        }

    }
}
