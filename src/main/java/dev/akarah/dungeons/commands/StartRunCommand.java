package dev.akarah.dungeons.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.akarah.dungeons.Main;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

import java.util.List;

public class StartRunCommand {
    public static CommandDispatcher<CommandSourceStack> dispatcher;

    public static void register(Commands commands) {
        commands.register(
                Commands.literal("startrun")
                        .executes(ctx -> {
                            if(ctx.getSource().getExecutor() instanceof Player p) {
                                var run = Main.getInstance().dungeonManager().createRun(List.of(p));
                            }
                            return 0;
                        })
                        .build()
        );
        StartRunCommand.dispatcher = commands.getDispatcher();
    }
}
