package dev.akarah.dungeons.config.item;

import dev.akarah.dungeons.Main;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class InventoryEvents implements Listener {
    public static void updateAllInventories() {
        for(var p : Bukkit.getOnlinePlayers()) {
            updateInvFor(p);
        }
    }

    public static void updateInvFor(Player p) {
        for(int i = 0; i<41; i++) {
            var item = p.getInventory().getItem(i);
            if(item == null) {
                continue;
            }

            var id = CustomItem.getItemId(item);
            if(id == null) {
                continue;
            }

            var customItem = Main.getInstance().data().items().get(id);
            if(customItem == null) {
                continue;
            }

            var finishedItem = customItem.toItemStack();
            finishedItem.setAmount(item.getAmount());

            customItem.durability().ifPresent(durability -> {
                var dmg = item.getData(DataComponentTypes.DAMAGE);
                if(dmg == null) {
                    return;
                }
                finishedItem.setData(DataComponentTypes.DAMAGE, dmg);
            });

            p.getInventory().setItem(i, finishedItem);
        }
    }
}
