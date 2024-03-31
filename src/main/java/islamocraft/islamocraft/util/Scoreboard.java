package islamocraft.islamocraft.util;

import islamocraft.islamocraft.IslamoCraft;
import islamocraft.islamocraft.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Scoreboard implements Listener {
    private final Map<UUID, FastBoard> boards = new HashMap<>();

    public Scoreboard(IslamoCraft plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Map.Entry<UUID, FastBoard> board : boards.entrySet()) {
                updateBoard(board.getValue(), "", "&aPêchés:", "&c" + RoleplayManager.getMontantPechesDuJoueur(Bukkit.getPlayer(board.getKey())), "", "&aHassanates:", "&c" + RoleplayManager.getMontantHassanatesDuJoueur(Bukkit.getPlayer(board.getKey())));
            }
        }, 0L, 10L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        FastBoard board = new FastBoard(player);

        String title = "&c&lISLAMOCRAFT";
        board.updateTitle(ChatColor.translateAlternateColorCodes('&', title));

        boards.put(player.getUniqueId(), board);

        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Starfoullah ! Bienvenue sur IslamoCraft !");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();

        FastBoard board = boards.remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }
    }

    private void updateBoard(FastBoard board, String ... lines)
    {
        for (int a = 0; a < lines.length; ++a)
        {
            lines[a] = ChatColor.translateAlternateColorCodes('&', lines[a]);
        }

        board.updateLines(lines);
    }

}
