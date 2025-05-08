package dev.akarah.dungeons.commands;

import org.bukkit.entity.Player;

import dev.akarah.dungeons.inventory.MainMenu;
import io.papermc.paper.command.brigadier.Commands;

public class MenuCommand {
    public static void register(Commands commands) {
        commands.register(
                Commands.literal("menu").executes(ctx -> {
                            if (ctx.getSource().getExecutor() instanceof Player p) {
                                p.openInventory(new MainMenu(p).getInventory());
                            }
                            return 0;
                        })
                        .build());
    }
}
