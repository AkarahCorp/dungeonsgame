package dev.akarah.dungeons;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.akarah.dungeons.commands.CItemCommand;
import dev.akarah.dungeons.commands.HubCommand;
import dev.akarah.dungeons.commands.KitCommand;
import dev.akarah.dungeons.commands.StartRunCommand;
import dev.akarah.dungeons.config.DataRegistry;
import dev.akarah.dungeons.config.GlobalData;
import dev.akarah.dungeons.config.item.CustomItem;
import dev.akarah.dungeons.config.mob.CustomMob;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Keyed;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Objects;

public class Bootstrapper implements PluginBootstrap {
    GlobalData data = GlobalData.create();

    public static Bootstrapper INSTANCE;

    @Override
    public void bootstrap(@NotNull BootstrapContext bootstrapContext) {
        INSTANCE = this;

        searchForRegistry("items", CustomItem.CODEC, this.data.items());
        searchForRegistry("mobs", CustomMob.CODEC, this.data.mobs());

        bootstrapContext.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            StartRunCommand.register(event.registrar());
            HubCommand.register(event.registrar());
            CItemCommand.register(event.registrar());
            KitCommand.register(event.registrar());
        });
    }

    public static <T extends Keyed> void searchForRegistry(String folderName, Codec<T> codec, DataRegistry<T> registry) {
        try {
            var uri = Objects.requireNonNull(Bootstrapper.class.getClassLoader().getResource(folderName + "/")).toURI();

            var env = new HashMap<String, Object>();
            var fs = FileSystems.newFileSystem(uri, env);
            var itemsPath = fs.getPath("/" + folderName + "/");

            try(var files = Files.list(itemsPath)) {
                files.forEach(path -> {
                    try {
                        var json = new Gson().fromJson(Files.readString(path), JsonElement.class);
                        var items = codec.listOf().decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
                        for(var item : items) {
                            System.out.println("Registering id " + item.getKey() + " to registry " + folderName);
                            registry.register(item.getKey(), item);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            fs.close();

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
