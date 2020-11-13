package me.epicgodmc.blockstackerx.utils;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.epicframework.chat.FancyMessage;
import me.epicgodmc.epicframework.item.ItemBuilder;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.block.Action;

import java.util.ArrayList;
import java.util.List;

public class Settings {

    private final BlockStackerX plugin;
    private final FileConfiguration cfg;
    private final FileConfiguration worthFile;

    public Settings(BlockStackerX plugin, FileConfiguration cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.worthFile = plugin.getFileManager().getConfig("worth.yml").get();
    }

    public boolean getDoAutoSave() {
        return cfg.getBoolean("AutoSave");
    }

    public long getAutoSaveInterval() {
        long interval = (cfg.getLong("AutoSaveInterval") * 20) * 60;
        return interval;
    }

    public Action getAddAction()
    {
        return Action.valueOf(cfg.getString("global.addAction").toUpperCase());
    }

    public Action getSubtractAction()
    {
        return Action.valueOf(cfg.getString("global.subtractAction").toUpperCase());
    }

    public boolean stackerTypeExists(String type) {
        return cfg.contains("stackers." + type);
    }

    public Material getDefaultMaterial(String type)
    {
        return Material.valueOf(cfg.getString("stackers."+type+".blockType").toUpperCase());
    }


    public float[] getDisplayOffset(String type) {
        String[] data = cfg.getString("stackers." + type + ".displayOffset").split(",");

        float[] offset = new float[3];

        for (int i = 0; i < data.length; i++) {
            float f = Float.parseFloat(data[i]);
            offset[i] = f;
        }

        return offset;
    }

    public ItemBuilder getStacker(String type) {
        Material material = Material.valueOf(cfg.getString("stackers." + type + ".blockType").toUpperCase());
        Validate.notNull(material, "Material from stacker of type: " + type + " is invalid!");
        ItemBuilder output = new ItemBuilder(material);

        String display = FancyMessage.color(cfg.getString("stackers." + type + ".displayName"));
        Validate.notNull(display, "Displayname from stacker of type: " + type + " is invalid");
        output.displayname(display);

        List<String> lore = cfg.getStringList("stackers." + type + ".lore");
        Validate.notNull(lore, "Lore from stacker of type: " + type + " is invalid");
        lore.forEach(output::lore);

        return output;
    }

    public boolean doGlow(String type) {
        return cfg.getBoolean("stackers." + type + ".glow");
    }

    public String getValueFormat(String type, int value) {
        String format = cfg.getString("stackers." + type + ".valueFormat").replaceAll("%value%", String.valueOf(value));
        Validate.notNull(format, "Could not parse valueFormat for stacker of type: " + type);
        return FancyMessage.color(format);
    }

    public int getMaxStorage(String type) {
        return cfg.getInt("stackers." + type + ".maxStorage");
    }

    public boolean getDoTeamStacking(String type) {
        return cfg.getBoolean("stackers." + type + ".teamStacking");
    }

    public List<Material> getAvailableMaterials(String type) {
        List<String> materials = cfg.getStringList("stackers." + type + ".availableBlocks");
        Validate.notNull(materials, "Could not parse available materials for stacker of type: " + type);

        List<Material> output = new ArrayList<>();
        materials.forEach(e -> output.add(Material.valueOf(e.toUpperCase())));

        return output;
    }

    public String getLinkedGUI(String type)
    {
        if (cfg.isSet("stackers."+type+".gui"))
        {
            return cfg.getString("stackers."+type+".gui");
        }
        return "NA";
    }

    public double getWorth(Material mat)
    {
        if (worthFile.contains("levelValues."+mat.toString()))
        {
            return worthFile.getDouble("levelValues."+mat.toString());
        }
        return 0;
    }

    public ItemBuilder getStackerItem(StackerBlock type) {
        Material material = type.getStackMaterial();
        Validate.notNull(material, "Material from stacker of type: " + type.getType() + " is invalid!");
        ItemBuilder output = new ItemBuilder(material);

        String display = FancyMessage.color(cfg.getString("stackers." + type.getType() + ".stackerItem.displayName"));
        Validate.notNull(display, "DisplayName from stackerITEM of type: " + type.getType() + " is invalid");
        output.displayname(display);

        List<String> lore = cfg.getStringList("stackers." + type.getType() + ".stackerItem.lore");
        Validate.notNull(lore, "Lore from stacker of type: " + type.getType() + " is invalid");
        lore.forEach(e -> output.lore(e
                .replace("%1", String.valueOf(type.getValue()))
                .replace("%2", Bukkit.getOfflinePlayer(type.getOwner()).getName())));

        output.addStringTag("stackerItemType", type.getType());
        output.addStringTag("StackerMaterial", material.toString());
        output.addIntTag("stackerValue", type.getValue());

        return output;
    }


}
