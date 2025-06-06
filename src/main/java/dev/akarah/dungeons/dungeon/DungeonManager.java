package dev.akarah.dungeons.dungeon;

import dev.akarah.dungeons.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class DungeonManager {
    List<DungeonRun> dungeonRuns = new ArrayList<>();

    public DungeonRun getRunFor(UUID uuid) {
        for(var run : dungeonRuns) {
            if(run.members().contains(uuid)) {
                return run;
            }
        }
        return null;
    }

    public DungeonRun createRun(List<Player> p) {
        var randX = (int) (Math.random() * 100000);
        var randZ = (int) (Math.random() * 100000);

        var locX = (randX * 128) + (128 * 5);
        var locZ = (randZ * 128) + (128 * 5);

        var run = new DungeonRun(
                new Location(
                        Main.getInstance().dungeonWorld(),
                        locX,
                        64,
                        locZ
                ),
                p.stream().map(Player::getUniqueId).toList(),
                new AtomicInteger(0)
        );
        this.dungeonRuns.add(run);
        run.start();
        return run;
    }

    public void tickDungeons() {
        this.autoClearDungeons();

        for(var run : dungeonRuns) {
            run.tick();
        }
    }

    public void autoClearDungeons() {
        var toRemove = new ArrayList<DungeonRun>();
        for(var run : dungeonRuns) {
            for(var member : run.members()) {
                var player = Bukkit.getPlayer(member);

                if(player == null) {
                    continue;
                }

                try {
                    if(player.getLocation().distance(run.origin()) <= 500) {
                        break;
                    }
                } catch (Exception e) {

                }
            }
            if(run.failures().incrementAndGet() == 10) {
                run.end();
                toRemove.add(run);
            } else {
                run.failures().set(0);
            }
        }

        for(var remove : toRemove) {
            this.dungeonRuns.remove(remove);
        }
    }

}
