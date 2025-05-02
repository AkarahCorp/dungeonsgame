package dev.akarah.dungeons;

import dev.akarah.dungeons.commands.StartRunCommand;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.jetbrains.annotations.NotNull;

public class Bootstrapper implements PluginBootstrap {
    @Override
    public void bootstrap(@NotNull BootstrapContext bootstrapContext) {
        bootstrapContext.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            StartRunCommand.register(event.registrar());
        });
    }
}
