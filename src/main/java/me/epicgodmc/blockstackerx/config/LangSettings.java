package me.epicgodmc.blockstackerx.config;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.epicapi.chat.Message;
import me.epicgodmc.epicapi.storage.Config;
import me.epicgodmc.epicapi.storage.LightningBuilder;
import me.epicgodmc.epicapi.storage.internal.settings.ConfigSettings;
import me.epicgodmc.epicapi.storage.internal.settings.ReloadSettings;
import me.epicgodmc.epicapi.util.ColorAPI;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;

public class LangSettings {

    private Config config;
    private boolean success = true;
    private final String prefix;

    public LangSettings(BlockStackerX plugin, String lang) {
        File langFile = new File(plugin.getDataFolder() + File.separator + "lang");
        if (langFile.exists()) {
            config = LightningBuilder.fromPath("en", langFile.getPath())
                    .setReloadSettings(ReloadSettings.INTELLIGENT)
                    .setConfigSettings(ConfigSettings.SKIP_COMMENTS)
                    .createConfig();
            BlockStackerX.logger.log(Level.INFO, "Loaded langfile " + lang + " from server files");
        } else {
            try {
                InputStream inputStream = plugin.getResource(lang);
                if (inputStream != null) {
                    config = LightningBuilder.fromPath(lang, plugin.getDataFolder() + File.separator + "lang")
                            .addInputStream(inputStream)
                            .setReloadSettings(ReloadSettings.INTELLIGENT)
                            .setConfigSettings(ConfigSettings.SKIP_COMMENTS)
                            .createConfig();
                    BlockStackerX.logger.log(Level.INFO, "Loaded langfile " + lang + ".yml from jar file");
                } else {
                    success = false;
                }
            } catch (IllegalArgumentException e) {
                BlockStackerX.logger.log(Level.SEVERE, "Could not find langfile: " + lang, e);
                success = false;
            }
        }
        if (!success) {
            plugin.getStackerSettings().setLang("en.yml");
            config = LightningBuilder.fromPath("en", new File(plugin.getDataFolder() + File.separator + "lang").getPath())
                    .addInputStream(plugin.getResource("en.yml"))
                    .setReloadSettings(ReloadSettings.INTELLIGENT)
                    .setConfigSettings(ConfigSettings.SKIP_COMMENTS)
                    .createConfig();
            BlockStackerX.logger.log(Level.SEVERE, "LangFile " + lang + " could not be found, lang has been set to default 'en.yml'");
        }
        this.prefix = ColorAPI.color(config.getString("prefix"));
    }

    public void sendText(CommandSender recipient, String key, boolean prefix) {
        recipient.sendMessage(getText(key, prefix).getText());
    }

    public Message getText(String key, boolean prefix) {
        return new Message(prefix ? ColorAPI.color(this.prefix + config.getString("messages." + key)) : config.getString("messages." + key));
    }
}
