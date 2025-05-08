package dev.akarah.dungeons.commands;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import com.mojang.brigadier.CommandDispatcher;

import dev.akarah.dungeons.Main;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;

public class CItemCommand {
    public static CommandDispatcher<CommandSourceStack> dispatcher;

    public static void register(Commands commands) {
        commands.register(
                Commands.literal("citem")
                        .then(Commands.argument("item_id", ArgumentTypes.namespacedKey()).executes(ctx -> {
                            if (ctx.getSource().getExecutor() instanceof Player p) {
                                var item = Main.getInstance().data().items().get(
                                        ctx.getArgument("item_id", NamespacedKey.class));
                                if (item == null) {
                                    System.out.println(Main.getInstance().data().items());
                                    p.sendMessage(Component.text("Not a valid item id D: ("
                                            + ctx.getArgument("item_id", NamespacedKey.class) + ")"));
                                    return 1;
                                }
                                p.getInventory().addItem(item.toItemStack());
                            }
                            return 0;
                        }))
                        .requires(x -> x.getSender().isOp())
                        .build());
        CItemCommand.dispatcher = commands.getDispatcher();
    }
}
