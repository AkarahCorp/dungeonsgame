package dev.akarah.dungeons.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.akarah.dungeons.Main;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class CMobCommand {
    public static CommandDispatcher<CommandSourceStack> dispatcher;

    public static void register(Commands commands) {
        commands.register(
                Commands.literal("cmob")
                        .then(Commands.argument("mob_id", ArgumentTypes.namespacedKey()).executes(ctx -> {
                            if(ctx.getSource().getExecutor() instanceof Player p) {
                                var item = Main.getInstance().data().mobs().get(
                                        ctx.getArgument("mob_id", NamespacedKey.class)
                                );
                                if(item == null) {
                                    System.out.println(Main.getInstance().data().items());
                                    p.sendMessage(Component.text("Not a valid item id D: (" + ctx.getArgument("item_id", NamespacedKey.class) + ")"));
                                    return 1;
                                }
                                item.spawn(p.getLocation());
                            }
                            return 0;
                        }))
                        .requires(x -> x.getSender().isOp())
                        .build()
        );
        CMobCommand.dispatcher = commands.getDispatcher();
    }
}
