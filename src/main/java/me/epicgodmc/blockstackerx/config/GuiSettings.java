package me.epicgodmc.blockstackerx.config;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.epicapi.item.ItemBuilder;
import me.epicgodmc.epicapi.storage.Config;
import me.epicgodmc.epicapi.storage.LightningBuilder;
import me.epicgodmc.epicapi.storage.internal.settings.ConfigSettings;
import me.epicgodmc.epicapi.storage.internal.settings.ReloadSettings;
import me.epicgodmc.epicapi.storage.sections.FlatFileSection;
import me.epicgodmc.epicapi.util.ColorAPI;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GuiSettings {

    private Config config;

    public GuiSettings(BlockStackerX plugin) {
        config = LightningBuilder.fromPath("gui", plugin.getDataFolder().getPath())
                .addInputStream(plugin.getResource("gui.yml"))
                .setReloadSettings(ReloadSettings.INTELLIGENT)
                .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                .createConfig();
    }


    public boolean guiExists(String type) {
        return config.contains("guis." + type);
    }

    public Set<String> getButtonKeys(String type) {
        return config.getSection("guis." + type + ".buttons").singleLayerKeySet();
    }

    public String getGuiTitle(String type) {
        return ColorAPI.color(config.getString("guis." + type + ".title"));
    }

    public boolean isHasFill(String type) {
        return config.contains("guis." + type + ".fill");
    }

    public boolean isHasBorder(String type) {
        return config.contains("guis." + type + ".border");
    }

    public ItemStack getFillMaterial(String type) {
        ItemBuilder builder = new ItemBuilder();
        return builder.fromConfig(config, "guis." + type + ".fill").build();
    }

    public ItemStack getBorderMaterial(String type) {
        ItemBuilder builder = new ItemBuilder();
        return builder.fromConfig(config, "guis." + type + ".border").build();
    }

    public int getButtonPosition(String gui, String button) {
        return config.getInt("guis." + gui + ".buttons." + button + ".position");
    }

    public ItemBuilder getGuiButton(String gui, String button) {
        ItemBuilder builder = new ItemBuilder();
        return builder.fromConfig(config, "guis." + gui + ".buttons." + button);
    }

    public int getGuiSize(String type) {
        return config.getInt("guis." + type + ".slots");
    }
}
