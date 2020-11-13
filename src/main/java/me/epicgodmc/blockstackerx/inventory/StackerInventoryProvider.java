package me.epicgodmc.blockstackerx.inventory;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.utils.Utils;
import me.epicgodmc.epicframework.chat.FancyMessage;
import me.epicgodmc.epicframework.inventory.GuiButton;
import me.epicgodmc.epicframework.inventory.GuiPage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class StackerInventoryProvider {


    private final BlockStackerX plugin;
    private final StackerBlock stackerBlock;
    private final String path;
    FileConfiguration cfg;
    ConfigurationSection buttonSection;
    private final HashMap<String, String> variables;
    private final GuiPage gui;
    private final int invRows;

    public StackerInventoryProvider(BlockStackerX plugin, StackerBlock stackerBlock, String path, Player player) {
        this.plugin = plugin;
        this.stackerBlock = stackerBlock;
        this.path = path;
        cfg = plugin.getFileManager().getConfig("gui.yml").get();
        buttonSection = cfg.getConfigurationSection(path + ".buttons");
        variables = new HashMap<>();

        variables.put("%1", Bukkit.getOfflinePlayer(stackerBlock.getOwner()).getName());
        variables.put("%2", stackerBlock.getValue()+"");
        variables.put("%3", stackerBlock.calculateLevels()+"");
        variables.put("%4", stackerBlock.getMaterialValue()+"");

        invRows = cfg.getInt(path+".rows");

        String title = cfg.getString(path+".title");
        if (title == null) title = "N/A";
        gui = new GuiPage(invRows, FancyMessage.color(title.replace("%s", stackerBlock.getType())));
        init(player);

        gui.show(player);
    }

    public void init(Player player) {

        if (cfg.isSet(path+".fill"))
        {
            fill(plugin.getFramework().getGuiHelper().loadButton(cfg, path+".fill", null, e -> {e.setCancelled(true);}));
        }
        if (cfg.isSet(path+".border"))
        {
            fillBorder(plugin.getFramework().getGuiHelper().loadButton(cfg, path+".border", null, e -> {e.setCancelled(true);}));
        }

        for (String id : buttonSection.getKeys(false)) {

            setButton(id, getButtonAction(id, player));
        }

    }

    public void fillBorder(GuiButton button){
        for (int i = 0; i < invRows * 9; i++) {
            if ((i <= 8) || (i >= (invRows * 9) - 9)
                    || i == 9 || i == 18
                    || i == 27 || i == 36
                    || i == 17 || i == 26
                    || i == 35 || i == 44)
                gui.setButton(i, button);
        }
    }

    private void fill(GuiButton button)
    {
        List<Integer> emptySlots = new ArrayList<>();

        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getButton(i) == null) emptySlots.add(i);
        }

        emptySlots.forEach(e -> {
            gui.setButton(e, button);
        });
    }

    private void setButton(String id, Consumer<InventoryClickEvent> action)
    {
        int pos = cfg.getInt(path+".buttons."+id+".position");

        GuiButton button = plugin.getFramework().getGuiHelper().loadButton(cfg, path+".buttons."+id, variables, action);
        gui.setButton(pos, button);
    }

    private Consumer<InventoryClickEvent> getButtonAction(String id, Player player) {
        switch (id) {
            case "pickup":
                return inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    player.closeInventory();
                    player.getInventory().addItem(plugin.getSettings().getStackerItem(stackerBlock).build());
                    stackerBlock.delete();
                    plugin.getLangManager().getText("stackerBreak").send(player, true);
                };
            case "information":
                return inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                };
            case "deposit-inv":
                return inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    if (stackerBlock.hasStackMaterial()) {
                        int materials = Utils.countMaterials(player.getInventory().getContents(), stackerBlock.getStackMaterial());
                        if (stackerBlock.canAddValue(materials)) {
                            Utils.removeMaterials(player, stackerBlock.getStackMaterial(), materials);
                            stackerBlock.incrementValue(materials);
                            plugin.getLangManager().getText("valueAdded").replaceInteger("%1", materials).replaceText("%2", stackerBlock.getStackMaterial().toString().toLowerCase()).send(player, true);
                        } else {
                            int fitAmount = stackerBlock.getStorageLeft();
                            Utils.removeMaterials(player, stackerBlock.getStackMaterial(), fitAmount);
                            stackerBlock.incrementValue(fitAmount);
                            plugin.getLangManager().getText("valueAdded").replaceInteger("%1", fitAmount).replaceText("%2", stackerBlock.getStackMaterial().toString().toLowerCase()).send(player, true);
                        }
                        player.closeInventory();
                        plugin.getGuiManager().openStacker(player, stackerBlock);
                    } else plugin.getLangManager().getText("noStackTypeSelected").send(player, true);
                };
            case "withdraw-inv":
                return inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    if (stackerBlock.hasStackMaterial()) {
                        int invSpace = Utils.countOpenStorage(player, stackerBlock.getStackMaterial());
                        int stored = stackerBlock.getValue();

                        if (invSpace >= stored) {
                            player.getInventory().addItem(new ItemStack(stackerBlock.getStackMaterial(), stored));
                            stackerBlock.setValue(0);
                            plugin.getLangManager().getText("valueSubtracted").replaceInteger("%1", stored).replaceText("%2", stackerBlock.getStackMaterial().toString().toLowerCase()).send(player, true);
                        } else {
                            player.getInventory().addItem(new ItemStack(stackerBlock.getStackMaterial(), invSpace));
                            stackerBlock.subtractValue(invSpace);
                            plugin.getLangManager().getText("valueSubtracted").replaceInteger("%1", invSpace).replaceText("%2", stackerBlock.getStackMaterial().toString().toLowerCase()).send(player, true);
                        }
                        player.closeInventory();
                        plugin.getGuiManager().openStacker(player, stackerBlock);
                    } else plugin.getLangManager().getText("noStackTypeSelected").send(player, true);
                };
            case "toggle-hologram":
                return inventoryClickEvent -> {
                    inventoryClickEvent.setCancelled(true);
                    player.closeInventory();
                    stackerBlock.toggleHologramVisibility();
                };
        }
        return empty ->
        {
            empty.setCancelled(true);
        };
    }

}
