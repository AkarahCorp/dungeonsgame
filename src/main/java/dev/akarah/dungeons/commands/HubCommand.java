package dev.akarah.dungeons.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.mojang.brigadier.CommandDispatcher;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class HubCommand {
    public static CommandDispatcher<CommandSourceStack> dispatcher;

    public static void register(Commands commands) {
        commands.register(
                Commands.literal("hub")
                        .executes(ctx -> {
                            if (ctx.getSource().getExecutor() instanceof Player p) {
                                p.teleportAsync(new Location(
                                        Bukkit.getWorld("world"),
                                        0,
                                        100,
                                        0));
                            }
                            return 0;
                        })
                        .build());
        HubCommand.dispatcher = commands.getDispatcher();
    }
}
