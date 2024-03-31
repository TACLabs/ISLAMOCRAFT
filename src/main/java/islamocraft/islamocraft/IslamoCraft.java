package islamocraft.islamocraft;

import islamocraft.islamocraft.handlers.HallalHandler;
import islamocraft.islamocraft.handlers.HaramHandler;
import islamocraft.islamocraft.util.RoleplayManager;
import islamocraft.islamocraft.util.Scoreboard;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class IslamoCraft extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info("suce");

        new HallalHandler(this);
        new HaramHandler(this);
        new RoleplayManager(this);
        new Scoreboard(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("ma bite");

        //Sauvegarder toute la DATA
        RoleplayManager.sauvegarderTousLesJoueurs();
    }
}
