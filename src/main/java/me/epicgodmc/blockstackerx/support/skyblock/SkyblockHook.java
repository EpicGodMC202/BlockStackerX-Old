package me.epicgodmc.blockstackerx.support.skyblock;

import me.epicgodmc.blockstackerx.StackerBlock;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public interface SkyblockHook
{

    Collection<UUID> getTeam(UUID uuid);

    boolean hasSameTeam(UUID p1, UUID p2);

    boolean canModifyStacker(StackerBlock stacker, Player player);

}
