package me.epicgodmc.blockstackerx.commands;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.commands.sub.GiveCmd;
import me.epicgodmc.blockstackerx.commands.sub.ListCmd;
import me.epicgodmc.blockstackerx.commands.sub.SyncCmd;
import me.epicgodmc.blockstackerx.commands.sub.TopCmd;
import me.epicgodmc.blockstackerx.enumerators.Permission;
import me.epicgodmc.blockstackerx.utils.Utils;
import me.epicgodmc.epicapi.command.BaseCommand;
import me.epicgodmc.epicapi.command.CommandResult;
import me.epicgodmc.epicapi.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


public class CommandRoot extends BaseCommand {

    private final BlockStackerX plugin;

    public CommandRoot(BlockStackerX plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(CommandSender commandSender, String[] strings) {
        commandSender.sendMessage(Utils.getCmdUsage());
    }

    @Override
    public String name() {
        return "blockstackerx";
    }

    @Override
    public String requiredPermission() {
        return Permission.BASECOMMAND_USE.getNode();
    }

    @Override
    public void callBack(CommandSender commandSender, CommandResult commandResult, Exception e) {
        switch (commandResult)
        {
            case INVALID_SENDER:
                System.out.println("This command can only be ran as a player");
                break;
            case ERROR:
                commandSender.sendMessage(ChatColor.RED+"An unexpected error has occurred");
                BlockStackerX.logger.log(Level.SEVERE, "an unexpected error occurred when "+commandSender+" ran basecommand", e);
                break;
            case NO_PERMISSION:
                plugin.getLangSettings().sendText(commandSender, "cmdNoPermission", true);
                break;
        }
    }

    @Override
    public boolean isPlayerCmd() {
        return false;
    }


    @Override
    public String[] aliases() {
        return new String[]{"blockstacker", "bs"};
    }

    @Override
    public List<SubCommand> subCommands() {
        List<SubCommand> subCommands = new ArrayList<>();

        subCommands.add(new GiveCmd(plugin));
        subCommands.add(new TopCmd(plugin));
        subCommands.add(new SyncCmd(plugin));
        subCommands.add(new ListCmd(plugin));

        return subCommands;
    }
}
