package dev.akarah.dungeons;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import dev.akarah.dungeons.config.item.CustomItem;
import io.papermc.paper.datacomponent.DataComponentTypes;

public class GameEvents implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            var playerResult = Database.conn().prepareStatement("""
                    select * from players
                        where uuid = '{}'
                    """
                    .replaceFirst("\\{}", event.getPlayer().getUniqueId().toString()))
                    .executeQuery();

            playerResult.next();

            var inventoryString = playerResult.getString("inventory");

            var items = GameEvents.fromInventoryString(inventoryString);

            event.getPlayer().getInventory().clear();

            var idx = 0;
            for (var item : items) {
                if (item == null) {
                    idx += 1;
                    continue;
                }
                event.getPlayer().getInventory().setItem(idx, item);
                idx += 1;
            }

            try {
                var experience = playerResult.getInt("experience");
                Main.getInstance().data().statsHolder()
                    .setXP(event.getPlayer(), experience);

                var essence = playerResult.getInt("essence");
                Main.getInstance().data().statsHolder()
                    .setEssence(event.getPlayer(), essence);
            } catch(SQLException ignored) {

            }
        } catch (SQLException e) {
            // failed to get player, just insert a default inventory
            event.getPlayer().getInventory().clear();
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        try {
            var inventory = GameEvents.getInventoryString(event.getPlayer().getInventory());
            var sh = Main.getInstance().data().statsHolder();

            var q = """
                    delete from players
                        where uuid = '{0}';

                    insert into players (uuid, username, inventory, experience, essence) values (
                        '{1}',
                        '{2}',
                        '{3}',
                        {4},
                        {5}
                    );
                    """
                    .replaceFirst("\\{0}", event.getPlayer().getUniqueId().toString())
                    .replaceFirst("\\{1}", event.getPlayer().getUniqueId().toString())
                    .replaceFirst("\\{2}", event.getPlayer().getName())
                    .replaceFirst("\\{3}", inventory)
                    .replaceFirst("\\{4}", Integer.toString(sh.getXP(event.getPlayer())))
                    .replaceFirst("\\{5}", Integer.toString(sh.getEssence(event.getPlayer())));

            System.out.println(q);
            Database.conn().prepareStatement(q).execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getInventoryString(Inventory inventory) {
        var sb = new StringBuilder();

        var idx = 0;

        if (inventory == null) {
            return ";";
        }

        var contents = inventory.getContents();
        if (contents == null) {
            return ";";
        }

        for (var item : contents) {
            idx += 1;

            if (item == null) {
                continue;
            }

            var id = CustomItem.getItemId(item);
            if (id == null) {
                continue;
            }

            sb.append("slot=").append(idx - 1);
            sb.append(",id=").append(id);

            if (item.hasData(DataComponentTypes.DAMAGE)) {
                var dmg = item.getData(DataComponentTypes.DAMAGE);
                sb.append(",damage=").append(dmg);
            }

            sb.append(",amount=").append(item.getAmount());

            sb.append(";");
        }

        sb.append(";");

        return sb.toString();
    }

    public static List<ItemStack> fromInventoryString(String string) {
        var is = new ArrayList<ItemStack>();
        for (int i = 0; i < 41; i++) {
            is.add(null);
        }
        for (String item : string.split(";")) {
            if (item.isEmpty()) {
                continue;
            }

            Map<String, String> map = new HashMap<>();

            for (String entries : item.split(",")) {
                var key = entries.split("=")[0];
                var value = entries.split("=")[1];

                map.put(key, value);
            }

            var ci = Main.getInstance().data().items()
                    .get(Objects.requireNonNull(NamespacedKey.fromString(map.get("id"))));
            if (ci == null) {
                System.out.println("?! " + map.get("id"));
                continue;
            }

            var itemStack = ci.toItemStack();
            if (map.containsKey("damage")) {
                var damageNumber = Integer.parseInt(map.get("damage"));
                itemStack.setData(DataComponentTypes.DAMAGE, damageNumber);
            }

            itemStack.setAmount(Integer.parseInt(map.getOrDefault("amount", "1")));

            is.set(Integer.parseInt(map.get("slot")), itemStack);
        }

        return is;
    }
}
