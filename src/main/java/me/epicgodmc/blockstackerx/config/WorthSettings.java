package me.epicgodmc.blockstackerx.config;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.epicapi.storage.Config;
import me.epicgodmc.epicapi.storage.LightningBuilder;
import me.epicgodmc.epicapi.storage.internal.settings.ConfigSettings;
import me.epicgodmc.epicapi.storage.internal.settings.ReloadSettings;
import org.bukkit.Material;

public class WorthSettings
{

    private Config config;


    public WorthSettings(BlockStackerX plugin) {
        config = LightningBuilder.fromPath("worth", plugin.getDataFolder().getPath())
                .addInputStream(plugin.getResource("worth.yml"))
                .setReloadSettings(ReloadSettings.INTELLIGENT)
                .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                .createConfig();
    }

    public double getWorth(Material mat)
    {
        if (config.contains("levelValues."+mat.toString()))
        {
            return config.getDouble("levelValues."+mat.toString());
        }
        return 0;
    }

}
