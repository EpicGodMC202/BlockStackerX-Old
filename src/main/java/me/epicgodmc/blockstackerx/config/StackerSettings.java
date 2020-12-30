package me.epicgodmc.blockstackerx.config;

import lombok.Getter;
import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.epicapi.item.ItemBuilder;
import me.epicgodmc.epicapi.item.XMaterial;
import me.epicgodmc.epicapi.storage.Config;
import me.epicgodmc.epicapi.storage.LightningBuilder;
import me.epicgodmc.epicapi.storage.internal.settings.ConfigSettings;
import me.epicgodmc.epicapi.storage.internal.settings.ReloadSettings;
import me.epicgodmc.epicapi.util.ColorAPI;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.block.Action;

import javax.naming.spi.DirObjectFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Getter
public class StackerSettings {

    private Config config;

    public StackerSettings(BlockStackerX plugin) {
        config = LightningBuilder.fromPath("config", plugin.getDataFolder().getPath())
                .addInputStream(plugin.getResource("config.yml"))
                .setReloadSettings(ReloadSettings.INTELLIGENT)
                .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                .createConfig();
    }

    public void setLang(String lang) {
        config.set("langFile", lang);
    }

    public String getLang() {
        return config.getString("langFile");
    }

    public boolean getAutoSave() {
        return config.getBoolean("AutoSave");
    }

    public long getAutosaveInterval() {
        return (config.getLong("AutoSaveInterval") * 20) * 60;
    }

    public Action getAddAction() {
        return config.getEnum("global.addAction", Action.class);
    }

    public Action getSubtractAction() {
        return config.getEnum("global.subtractAction", Action.class);
    }

    public boolean stackerExists(String type) {
        return config.contains("stackers." + type);
    }

    public Material getDefaultMaterial(String type) {
        Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(config.getString("stackers." + type + ".item.material").toUpperCase());
        if (xMaterial.isPresent())
        {
            return xMaterial.get().parseMaterial();
        }
        return Material.IRON_BLOCK;
    }

    public float[] getDisplayOffset(String type) {
        String[] data = config.getString("stackers." + type + ".displayOffset").split(",");

        float[] offset = new float[3];

        for (int i = 0; i < data.length; i++) {
            float f = Float.parseFloat(data[i]);
            offset[i] = f;
        }

        return offset;
    }

    public ItemBuilder getStacker(String type) {
        ItemBuilder builder = new ItemBuilder();
        return builder.fromConfig(config, "stackers." + type + ".item");
    }

    public Object[] getMysqlCredentials() {
        Object[] objects = new Object[]
                {
                        config.getString("storage.mysql.address"),
                        config.getString("storage.mysql.name"),
                        config.getString("storage.mysql.username"),
                        config.getString("storage.mysql.password"),
                        config.getInt("storage.mysql.port"),
                };
        return objects;
    }

    public ItemBuilder getStackerItem(StackerBlock block)
    {
        ItemBuilder builder = new ItemBuilder();
        ItemBuilder stack = builder.fromConfig(config, "stackers."+block.getType()+".stackerItem");
        stack.setMaterial(block.getStackMaterial());
        stack.addStringTag("stackerItemType", block.getType());
        stack.addStringTag("StackerMaterial", block.getStackMaterial().toString());
        stack.addIntTag("stackerValue", block.getValue());
        stack.addPlaceHolder("%1", String.valueOf(block.getValue()));
        stack.addPlaceHolder("%2", Bukkit.getOfflinePlayer(block.getOwner()).getName());

        return stack;
    }

//    public ItemBuilder getStackerItem(StackerBlock type) {
//        Material material = type.getStackMaterial();
//        Validate.notNull(material, "Material from stacker of type: " + type.getType() + " is invalid!");
//        ItemBuilder output = new ItemBuilder();
//
//        String display = ColorAPI.color(config.getString("stackers." + type.getType() + ".stackerItem.displayName"));
//        Validate.notNull(display, "DisplayName from stackerITEM of type: " + type.getType() + " is invalid");
//        output.setName(display);
//
//        List<String> lore = config.getStringList("stackers." + type.getType() + ".stackerItem.lore");
//        Validate.notNull(lore, "Lore from stacker of type: " + type.getType() + " is invalid");
//        lore.forEach(e -> output.setLore(ColorAPI.color(e
//                .replace("%1", String.valueOf(type.getValue()))
//                .replace("%2", Bukkit.getOfflinePlayer(type.getOwner()).getName()))));
//
//        output.addStringTag("stackerItemType", type.getType());
//        output.addStringTag("StackerMaterial", material.toString());
//        output.addIntTag("stackerValue", type.getValue());
//
//        return output;
//    }

    public String getValueFormat(String type, int value) {
        String format = config.getString("stackers." + type + ".valueFormat").replaceAll("%value%", String.valueOf(value));
        Validate.notNull(format, "Could not parse valueFormat for stacker of type: " + type);
        return ColorAPI.color(format);
    }

    public int getMaxStorage(String type) {
        return config.getInt("stackers." + type + ".maxStorage");
    }

    public boolean getDoTeamStacking(String type) {
        return config.getBoolean("stackers." + type + ".teamStacking");
    }

    public List<Material> getAvailableMaterials(String type) {
        List<String> materials = config.getStringList("stackers." + type + ".availableBlocks");
        Validate.notNull(materials, "Could not parse available materials for stacker of type: " + type);

        List<Material> output = new ArrayList<>();
        materials.forEach(e -> output.add(Material.valueOf(e.toUpperCase())));

        return output;
    }

    public String getLinkedGUI(String type) {
        if (config.contains("stackers." + type + ".gui")) {
            return config.getString("stackers." + type + ".gui");
        }
        return "NA";
    }

    public Set<String> getLoadedStackers() {
        return config.getSection("stackers").singleLayerKeySet();
    }
}
