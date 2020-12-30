package me.epicgodmc.blockstackerx.listeners;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.config.StackerSettings;
import me.epicgodmc.blockstackerx.enumerators.Permission;
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
    private final StackerSettings settings;

    public StackerInteractEvent(BlockStackerX plugin) {
        this.plugin = plugin;
        this.settings = plugin.getStackerSettings();

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
                    if (plugin.getStackerSettings().getAvailableMaterials(stacker.getType()).contains(material)) {
                        if (!Utils.isStacker(hand)) {
                            if (plugin.getDependencyManager().getSkyblockHook().canModifyStacker(stacker, e.getPlayer())) {
                                if (stacker.getValue() == 0) {
                                    stacker.setStackMaterial(material);
                                    plugin.getLangSettings().getText("materialChosen", true)
                                            .addPlaceHolder("%s", material.toString().toLowerCase())
                                            .send(e.getPlayer());
                                }
                                if (stacker.getStackMaterial().equals(material)) {
                                    if (!e.getPlayer().isSneaking()) {
                                        if (stacker.canAddValue(1)) {
                                            int subAmt = Utils.subtractPlayerHandAmount(e.getPlayer(), 1);
                                            stacker.incrementValue(subAmt);
                                            plugin.getLangSettings().getText("valueAdded", true)
                                                    .addPlaceHolder("%1", subAmt)
                                                    .addPlaceHolder("%2", material.toString().toLowerCase())
                                                    .send(e.getPlayer());
                                        } else
                                            plugin.getLangSettings().getText("maxStorageReached", true)
                                                    .addPlaceHolder("%s", settings.getMaxStorage(stacker.getType()))
                                                    .send(e.getPlayer());
                                    } else {
                                        if (stacker.canAddValue(e.getPlayer().getItemInHand().getAmount())) {
                                            int subAmt = Utils.subtractPlayerHandAmount(e.getPlayer(), 64);
                                            stacker.incrementValue(subAmt);
                                            plugin.getLangSettings().getText("valueAdded", true)
                                                    .addPlaceHolder("%1", subAmt)
                                                    .addPlaceHolder("%2", material.toString().toLowerCase())
                                                    .send(e.getPlayer());
                                        } else if (stacker.getStorageLeft() >= 1) {
                                            int storageLeft = stacker.getStorageLeft();
                                            int subAmt = Utils.subtractPlayerHandAmount(e.getPlayer(), storageLeft);
                                            stacker.incrementValue(subAmt);
                                            plugin.getLangSettings().getText("valueAdded", true)
                                                    .addPlaceHolder("%1", subAmt)
                                                    .addPlaceHolder("%2", material.toString().toLowerCase())
                                                    .send(e.getPlayer());
                                        } else
                                            plugin.getLangSettings().getText("maxStorageReached", true)
                                                    .addPlaceHolder("%s", settings.getMaxStorage(stacker.getType()))
                                                    .send(e.getPlayer());
                                    }
                                } else
                                    plugin.getLangSettings().getText("materialNoMatch", true).addPlaceHolder("%s", stacker.getStackMaterial().toString().toLowerCase()).send(e.getPlayer());
                            } else plugin.getLangSettings().sendText(e.getPlayer(), "stackerNoPermission", true);
                        } else plugin.getLangSettings().sendText(e.getPlayer(), "invalidBlock", true);
                    } else plugin.getLangSettings().sendText(e.getPlayer(), "invalidBlock", true);
                } else plugin.getLangSettings().sendText(e.getPlayer(), "stackerNoPermission", true);
            } else {
                if (!plugin.getStackerSettings().getLinkedGUI(stacker.getType()).equals("NA")) {
                    if (plugin.getDependencyManager().getSkyblockHook().canModifyStacker(stacker, e.getPlayer())) {
                        plugin.getGuiManager().openStacker(e.getPlayer(), stacker);
                    } else plugin.getLangSettings().sendText(e.getPlayer(), "stackerNoPermission", true);
                } else plugin.getLangSettings().sendText(e.getPlayer(), "invalidBlock", true);
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
                                    plugin.getLangSettings().getText("valueSubtracted", true)
                                            .addPlaceHolder("%1", 1)
                                            .addPlaceHolder("%2", stacker.getStackMaterial().toString().toLowerCase())
                                            .send(e.getPlayer());
                                    stacker.subtractValue(1);
                                } else plugin.getLangSettings().sendText(e.getPlayer(), "inventoryFull", true);
                            } else {
                                if (stacker.canSubtractValue(64)) {
                                    if (Utils.hasAvailableSlot(e.getPlayer(), stacker.getStackMaterial(), 64)) {
                                        e.getPlayer().getInventory().addItem(new ItemStack(stacker.getStackMaterial(), 64));
                                        plugin.getLangSettings().getText("valueSubtracted", true)
                                                .addPlaceHolder("%1", 64)
                                                .addPlaceHolder("%2", stacker.getStackMaterial().toString().toLowerCase())
                                                .send(e.getPlayer());
                                        stacker.subtractValue(64);
                                    } else plugin.getLangSettings().sendText(e.getPlayer(), "inventoryFull", true);
                                } else {
                                    if (Utils.hasAvailableSlot(e.getPlayer(), stacker.getStackMaterial(), stacker.getValue())) {
                                        e.getPlayer().getInventory().addItem(new ItemStack(stacker.getStackMaterial(), stacker.getValue()));
                                        plugin.getLangSettings().getText("valueSubtracted", true)
                                                .addPlaceHolder("%1", stacker.getValue())
                                                .addPlaceHolder("%2", stacker.getStackMaterial().toString().toLowerCase())
                                                .send(e.getPlayer());
                                        stacker.setValue(0);
                                    } else plugin.getLangSettings().sendText(e.getPlayer(), "inventoryFull", true);
                                }
                            }
                        }
                    } else {
                        e.setCancelled(true);
                        plugin.getLangSettings().sendText(e.getPlayer(), "stackerNoPermission", true);
                    }
                } else {
                    e.setCancelled(true);
                    plugin.getLangSettings().sendText(e.getPlayer(), "stackerNoPermission", true);
                }
            } else {
                e.setCancelled(true);
                plugin.getLangSettings().sendText(e.getPlayer(), "minStorageReached", true);
            }
        }

    }
}
