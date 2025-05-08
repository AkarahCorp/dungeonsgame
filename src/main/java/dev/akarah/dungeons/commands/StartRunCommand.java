package dev.akarah.dungeons.commands;

import java.util.List;

import org.bukkit.entity.Player;

import com.mojang.brigadier.CommandDispatcher;

import dev.akarah.dungeons.Main;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class StartRunCommand {
    public static CommandDispatcher<CommandSourceStack> dispatcher;

    public static void register(Commands commands) {
        commands.register(
                Commands.literal("startrun")
                        .executes(ctx -> {
                            if (ctx.getSource().getExecutor() instanceof Player p) {
                                Main.getInstance().dungeonManager().createRun(List.of(p));
                            }
                            return 0;
                        })
                        .build());
        StartRunCommand.dispatcher = commands.getDispatcher();
    }
}
