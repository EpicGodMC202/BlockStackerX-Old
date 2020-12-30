package me.epicgodmc.blockstackerx.inventory;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.config.GuiSettings;
import me.epicgodmc.blockstackerx.utils.Utils;
import me.epicgodmc.epicapi.inventory.ClickHandler;
import me.epicgodmc.epicapi.inventory.Menu;
import me.epicgodmc.epicapi.inventory.MenuItem;
import me.epicgodmc.epicapi.inventory.MenuRowSize;
import me.epicgodmc.epicapi.item.ItemBuilder;
import me.epicgodmc.epicapi.item.XItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class StackerGUI extends Menu {

    private final BlockStackerX plugin;
    private final StackerBlock stackerBlock;
    private final String guiType;
    private final HashMap<String, String> variables;
    private final GuiSettings guiSettings;
    private final int invRows;

    public StackerGUI(BlockStackerX plugin, Player player, StackerBlock block, String guiType) {
        super(plugin.getMenuRegistrar(), plugin.getGuiSettings().getGuiTitle(guiType).replace("%s", block.getValue()+""), MenuRowSize.fromInteger(plugin.getGuiSettings().getGuiSize(guiType)), player);
        this.guiSettings = plugin.getGuiSettings();
        this.plugin = plugin;
        this.stackerBlock = block;
        this.guiType = guiType;
        this.invRows = guiSettings.getGuiSize(guiType) / 9;
        this.variables = new HashMap<>();
        setupVariables();

    }

    private void setupVariables() {
        variables.put("%1", Bukkit.getOfflinePlayer(stackerBlock.getOwner()).getName());
        variables.put("%2", stackerBlock.getValue() + "");
        variables.put("%3", stackerBlock.calculateLevels() + "");
        variables.put("%4", stackerBlock.getMaterialValue() + "");
    }

    @Override
    public MenuItem[] getMenuItems() {
        MenuItem[] array = newMenuItemArray();

        if (plugin.getGuiSettings().isHasFill(guiType)) {
            MenuItem menuItem = new MenuItem(guiSettings.getFillMaterial(guiType), (clicker, info) -> true);
            fill(menuItem, array);
        }
        if (plugin.getGuiSettings().isHasBorder(guiType)) {
            MenuItem menuItem = new MenuItem(guiSettings.getBorderMaterial(guiType), (clicker, info) -> true);
            fillBorder(menuItem, array);
        }

        for (String id : guiSettings.getButtonKeys(guiType)) {
            setButton(array, id, getClickhandler(id, getPlayer()));
        }
        return array;
    }

    private void setButton(MenuItem[] array, String id, ClickHandler action) {
        int pos = plugin.getGuiSettings().getButtonPosition(guiType, id);

        ItemBuilder button = guiSettings.getGuiButton(guiType, id);
        this.variables.forEach(button::addPlaceHolder);
        MenuItem menuItem = new MenuItem(button.build(), action);

        array[pos] = menuItem;
    }

    private MenuItem[] fill(MenuItem item, MenuItem[] array) {
        List<Integer> emptySlots = new ArrayList<>();


        for (int i = 0; i < array.length; i++) {
            MenuItem current = array[i];
            if (current == null || current.getItemStack() == null || current.getItemStack().getType().equals(Material.AIR))
                emptySlots.add(i);
        }

        emptySlots.forEach(e -> {
            array[e] = item;
        });
        return array;
    }

    public MenuItem[] fillBorder(MenuItem item, MenuItem[] array) {
        for (int i = 0; i < invRows * 9; i++) {
            if ((i <= 8) || (i >= (invRows * 9) - 9)
                    || i == 9 || i == 18
                    || i == 27 || i == 36
                    || i == 17 || i == 26
                    || i == 35 || i == 44)
                array[i] = item;
        }
        return array;
    }

    private ClickHandler getClickhandler(String id, Player player) {
        switch (id) {
            case "pickup":
                return (clicker, info) ->
                {
                    player.closeInventory();
                    XItemStack.addItems(player.getInventory(), false, plugin.getStackerSettings().getStackerItem(stackerBlock).build());
                    stackerBlock.delete();
                    plugin.getLangSettings().sendText(player, "stackerBreak", true);
                    return true;
                };
            case "information":
                return (clicker, info) -> true;
            case "deposit-inv":
                return (clicker, info) ->
                {
                    if (stackerBlock.hasStackMaterial()) {
                        int materials = Utils.countMaterials(player.getInventory().getContents(), stackerBlock.getStackMaterial());
                        if (stackerBlock.canAddValue(materials)) {
                            Utils.removeMaterials(player, stackerBlock.getStackMaterial(), materials);
                            stackerBlock.incrementValue(materials);
                            plugin.getLangSettings().getText("valueAdded", true)
                                    .addPlaceHolder("%1", materials)
                                    .addPlaceHolder("%2", stackerBlock.getStackMaterial().toString().toLowerCase())
                                    .send(player);
                        } else {
                            int fitAmount = stackerBlock.getStorageLeft();
                            Utils.removeMaterials(player, stackerBlock.getStackMaterial(), fitAmount);
                            stackerBlock.incrementValue(fitAmount);
                            plugin.getLangSettings().getText("valueAdded", true)
                                    .addPlaceHolder("%1", fitAmount)
                                    .addPlaceHolder("%2", stackerBlock.getStackMaterial().toString().toLowerCase())
                                    .send(player);
                        }
                        player.closeInventory();
                        plugin.getGuiManager().openStacker(player, stackerBlock);
                    } else plugin.getLangSettings().sendText(player, "noStackTypeSelected", true);
                    return true;
                };
            case "withdraw-inv":
                return (clicker, info) ->
                {
                    if (stackerBlock.hasStackMaterial()) {
                        int invSpace = Utils.countOpenStorage(player, stackerBlock.getStackMaterial());
                        int stored = stackerBlock.getValue();

                        if (invSpace >= stored) {
                            player.getInventory().addItem(new ItemStack(stackerBlock.getStackMaterial(), stored));
                            stackerBlock.setValue(0);
                            plugin.getLangSettings().getText("valueSubtracted", true)
                                    .addPlaceHolder("%1", stored)
                                    .addPlaceHolder("%2", stackerBlock.getStackMaterial().toString().toLowerCase())
                                    .send(player);
                        } else {
                            player.getInventory().addItem(new ItemStack(stackerBlock.getStackMaterial(), invSpace));
                            stackerBlock.subtractValue(invSpace);
                            plugin.getLangSettings().getText("valueSubtracted", true)
                                    .addPlaceHolder("%1", invSpace)
                                    .addPlaceHolder("%2", stackerBlock.getStackMaterial().toString().toLowerCase())
                                    .send(player);
                        }
                        player.closeInventory();
                        plugin.getGuiManager().openStacker(player, stackerBlock);
                    } else plugin.getLangSettings().sendText(player, "noStackTypeSelected", true);
                    return true;
                };
            case "toggle-hologram":
                return (clicker, info) ->
                {
                    player.closeInventory();
                    stackerBlock.toggleHologramVisibility();
                    return true;
                };
        }
        return (clicker, info) -> true;
    }
}
