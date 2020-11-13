package me.epicgodmc.blockstackerx.commands;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.commands.sub.GiveCmd;
import me.epicgodmc.blockstackerx.enumerators.Permission;
import me.epicgodmc.blockstackerx.utils.Utils;
import me.epicgodmc.epicframework.command.ECommand;
import me.epicgodmc.epicframework.command.SubCommand;

import java.util.ArrayList;
import java.util.List;


public class BaseCommand extends ECommand {

    private final BlockStackerX plugin;

    public BaseCommand(BlockStackerX plugin)
    {
        this.plugin = plugin;
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
    public String noPermissionMessage() {
        return plugin.getLangManager().getText("cmdNoPermission").get(true);
    }

    @Override
    public String info() {

        return "BlockStackerX base command";
    }

    @Override
    public String noArgsFound() {
        return Utils.getCmdUsage();
    }

    @Override
    public String subCmdNotFound() {
        return plugin.getLangManager().getText("subCommandNotFound").get(true);
    }

    @Override
    public String error() {
        return plugin.getLangManager().getText("error").get(true);
    }

    @Override
    public String[] aliases() {
        return new String[]{"blockstacker", "bs"};
    }

    @Override
    public List<SubCommand> subCommands() {
        List<SubCommand> subCommands = new ArrayList<>();

        subCommands.add(new GiveCmd(plugin));

        return subCommands;
    }
}
