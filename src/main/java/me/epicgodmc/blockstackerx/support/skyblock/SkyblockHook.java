package me.epicgodmc.blockstackerx.support.skyblock;

import me.epicgodmc.blockstackerx.StackerBlock;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public interface SkyblockHook
{

    Collection<UUID> getTeam(World world, UUID uuid);

    boolean hasSameTeam(World world, UUID p1, UUID p2);

    boolean canModifyStacker(StackerBlock stacker, Player player);

}
