package me.epicgodmc.blockstackerx.commands.sub;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.enumerators.Permission;
import me.epicgodmc.epicframework.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class TopCmd extends SubCommand {


    private final BlockStackerX plugin;

    public TopCmd(BlockStackerX plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSender commandSender, String[] strings) {
        sendTopTen(commandSender, plugin.getStackerStore().sort());
    }

    private void sendTopTen(CommandSender sender, ArrayList<StackerBlock> sorted)
    {
        plugin.getLangManager().getText("stackerTopHeader").send(sender, false);
        for (int i = 0; i < 10; i++) {
            if (i <= sorted.size()) {
                plugin.getLangManager()
                        .getText("stackerTopFormat")
                        .replaceInteger("%1", i+1)
                        .replaceText("%2", Bukkit.getOfflinePlayer(sorted.get(i).getOwner()).getName())
                        .replaceInteger("%3", sorted.get(i).getValue()).send(sender, false);
            }else break;
        }
        plugin.getLangManager().getText("stackerTopFooter").send(sender, false);
    }

    @Override
    public String name() {
        return "top";
    }

    @Override
    public String requiredPermission() {
        return Permission.TOP_STACKERS.getNode();
    }

    @Override
    public String info() {
        return "&f&l* &d/BlockStackerX top";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }
}
