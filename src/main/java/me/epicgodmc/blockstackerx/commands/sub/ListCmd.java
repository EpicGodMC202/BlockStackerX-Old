package me.epicgodmc.blockstackerx.commands.sub;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.enumerators.Permission;
import me.epicgodmc.epicapi.chat.Message;
import me.epicgodmc.epicapi.command.SubCommand;
import org.bukkit.command.CommandSender;

public class ListCmd extends SubCommand {

    private final BlockStackerX plugin;

    public ListCmd(BlockStackerX plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSender commandSender, String[] strings) {
        new Message("&d&l&m------------------------------\n").send(commandSender);
        plugin.getStackerSettings().getLoadedStackers().forEach(e ->
        {
            new Message("&f&l* &d" + e.split("\\.")[0]).send(commandSender);
        });
        new Message("&d&l&m------------------------------").send(commandSender);
    }

    @Override
    public String name() {
        return "list";
    }

    @Override
    public String requiredPermission() {
        return Permission.STACKER_LIST.getNode();
    }

    @Override
    public boolean isPlayerCmd() {
        return false;
    }

    @Override
    public String info() {
        return null;
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
