package dev.akarah.dungeons.inventory;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import dev.akarah.dungeons.Main;
import dev.akarah.dungeons.config.item.CustomItem;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public record MainMenu(Player player) implements InventoryHolder {
    @Override
    public @NotNull Inventory getInventory() {
        var inv = Bukkit.getServer().createInventory(this, 18);
        inv.setItem(4, getMainProfile());
        inv.setItem(8, getTrashCan());
        return inv;
    }

    public ItemStack getTrashCan() {
        var trashCan = ItemStack.of(Material.LAVA_BUCKET);

        return trashCan;
    }

    public ItemStack getMainProfile() {
        var head = ItemStack.of(Material.PLAYER_HEAD);
        head.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(player.getPlayerProfile()));

        head.unsetData(DataComponentTypes.ITEM_NAME);
        head.setData(DataComponentTypes.ITEM_NAME, Component.text(player.getName() + "'s Stats"));

        var sh = Main.getInstance().data().statsHolder();
        var experience = sh.getXP(this.player);
        var essence = sh.getEssence(this.player);

        var lore = new ArrayList<Component>();

        lore.add(Component.text("Progression")
            .color(TextColor.color(255, 255, 0))
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));

        lore.add(Component.text("XP: ")
            .color(TextColor.color(133, 133, 133))
            .append(
                Component.text(experience)
                    .color(TextColor.color(255, 0, 255))
            )
            .decoration(TextDecoration.ITALIC, false));

        lore.add(Component.text("Essence: ")
            .color(TextColor.color(133, 133, 133))
            .append(
                Component.text(essence)
                    .color(TextColor.color(255, 0, 255))
            )
            .decoration(TextDecoration.ITALIC, false));

        lore.add(Component.empty());

        lore.add(Component.text("Stats")
            .color(TextColor.color(255, 255, 0))
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));

        for(var entry : sh.statsFor(this.player).innerMap().entrySet()) {
            lore.add(
                Component.text(entry.getValue())
                    .color(TextColor.color(133, 133, 255))
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.space())
                    .append(
                        Component.text(CustomItem.titleCase(entry.getKey().replace("_", " ")))
                            .color(TextColor.color(133, 133, 133))
                    )
            );
        }

        head.setData(DataComponentTypes.LORE, ItemLore.lore(lore));

        return head;
    }

    public static class Handler implements Listener {
        @EventHandler
        public void clickSlot(InventoryClickEvent event) {
            var clickedInventory = event.getClickedInventory();

            if(clickedInventory == null) {
                return;
            }

            if(clickedInventory.getHolder() instanceof MainMenu) {
                event.setCancelled(true);

                if(event.getSlot() == 8) {
                    event.getWhoClicked().openInventory(new ScrapMenu(
                        Bukkit.getPlayer(event.getWhoClicked().getUniqueId())
                    ).getInventory());
                }
            }

            
        }
    }
}
