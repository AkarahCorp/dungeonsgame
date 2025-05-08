package dev.akarah.dungeons;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import org.bukkit.Keyed;
import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import dev.akarah.dungeons.commands.CItemCommand;
import dev.akarah.dungeons.commands.HubCommand;
import dev.akarah.dungeons.commands.KitCommand;
import dev.akarah.dungeons.commands.MenuCommand;
import dev.akarah.dungeons.commands.StartRunCommand;
import dev.akarah.dungeons.config.DataRegistry;
import dev.akarah.dungeons.config.GlobalData;
import dev.akarah.dungeons.config.item.CustomItem;
import dev.akarah.dungeons.config.mob.CustomMob;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

public class Bootstrapper implements PluginBootstrap {
    GlobalData data = GlobalData.create();

    public static Bootstrapper INSTANCE;

    @Override
    public void bootstrap(@NotNull BootstrapContext bootstrapContext) {
        INSTANCE = this;

        Database.tryConnectDb();
        Database.tryInitializeDb();

        searchForRegistry("items", CustomItem.CODEC, this.data.items());
        searchForRegistry("mobs", CustomMob.CODEC, this.data.mobs());

        bootstrapContext.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            StartRunCommand.register(event.registrar());
            HubCommand.register(event.registrar());
            CItemCommand.register(event.registrar());
            KitCommand.register(event.registrar());
            MenuCommand.register(event.registrar());
        });
    }

    public static <T extends Keyed> void searchForRegistry(String folderName, Codec<T> codec, DataRegistry<T> registry) {
        try {
            var uri = Objects.requireNonNull(Bootstrapper.class.getClassLoader().getResource(folderName + "/")).toURI();

            var env = new HashMap<String, Object>();
            try(var fs = FileSystems.newFileSystem(uri, env)) {
                var itemsPath = fs.getPath("/" + folderName + "/");
                try(var files = Files.list(itemsPath)) {
                    files.forEach(path -> {
                        try {
                            var json = new Gson().fromJson(Files.readString(path), JsonElement.class);
                            try {
                                codec.listOf().decode(JsonOps.INSTANCE, json).ifSuccess(pair -> {
                                    for(var item : pair.getFirst()) {
                                        System.out.println("Registering id " + item.getKey() + " to registry " + folderName);
                                        registry.register(item.getKey(), item);
                                    }
                                }).ifError(System.out::println);
                            } catch (Exception e) {
                                System.out.println("failed at " + path);
                                System.err.println(Arrays.toString(e.getStackTrace()));
                            }    
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
