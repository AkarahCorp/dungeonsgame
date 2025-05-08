package dev.akarah.dungeons.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import dev.akarah.dungeons.Main;
import dev.akarah.dungeons.config.item.CustomItem;

public record ScrapMenu(Player player) implements InventoryHolder {
    @Override
    public @NotNull Inventory getInventory() {
        var inv = Bukkit.getServer().createInventory(this, 54);
        return inv;
    }

    public static class Handler implements Listener {
        @EventHandler
        public void closeInv(InventoryCloseEvent event) {
            if(event.getInventory().getHolder() instanceof ScrapMenu) {
                var data = Main.getInstance().data();
                var clickedInventory = event.getInventory();
                var player = Bukkit.getPlayer(event.getPlayer().getUniqueId());
    
                var contents = clickedInventory.getContents();
                if(contents == null) {
                    return;
                }
    
                for(var item : contents) {
                    if(item == null) {
                        continue;
                    }
    
                    var id = CustomItem.getItemId(item);
                    if(id == null) {
                        continue;
                    }
    
                    var citem = data.items().get(id);
                    if(citem == null) {
                        continue;
                    }
    
                    data.statsHolder().setXP(player, 
                        data.statsHolder().getXP(player) + citem.scrapAwards().xp());
                    data.statsHolder().setEssence(player, 
                        data.statsHolder().getEssence(player) + citem.scrapAwards().essence());
                }
            }
            
        }
    }
}
