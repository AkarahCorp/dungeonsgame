package dev.akarah.dungeons.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.akarah.dungeons.Main;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Objects;

public class KitCommand {
    public static CommandDispatcher<CommandSourceStack> dispatcher;

    public static void register(Commands commands) {
        commands.register(
                Commands.literal("kit")
                        .executes(ctx -> {
                            if(ctx.getSource().getExecutor() instanceof Player p) {
                                p.getInventory().addItem(
                                        Objects.requireNonNull(
                                                Main.getInstance().data().items().get(NamespacedKey.minecraft("adventurers/blade"))
                                        ).toItemStack()
                                );
                                p.getInventory().addItem(
                                        Objects.requireNonNull(
                                                Main.getInstance().data().items().get(NamespacedKey.minecraft("adventurers/helmet"))
                                        ).toItemStack()
                                );
                            }
                            return 0;
                        })
                        .build()
        );
        KitCommand.dispatcher = commands.getDispatcher();
    }
}
