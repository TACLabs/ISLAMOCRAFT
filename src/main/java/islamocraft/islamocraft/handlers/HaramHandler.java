package islamocraft.islamocraft.handlers;

import islamocraft.islamocraft.IslamoCraft;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import static islamocraft.islamocraft.util.RoleplayManager.ajouterPechesAuJoueur;

public class HaramHandler implements Listener {

    private IslamoCraft pluginRef;
    public HaramHandler(IslamoCraft plugin)
    {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        pluginRef = plugin;
    }

    //Quand un joueur bouffe du cochon
    @EventHandler
    public void onMangerDuCochon(PlayerItemConsumeEvent event)
    {
        Player player = event.getPlayer();

        if(event.getItem().getType().toString().contains("PORKCHOP"))
        {
            player.getWorld().strikeLightning(player.getLocation());

            Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "Le kouffar " + player.getName() + " a mangé du porc ! (+25 Pêchés)");
            ajouterPechesAuJoueur(player.getUniqueId(), 25);
        }
    }

    //Quand un joueur interagit d'une quelconque manière avec un Jukebox ou un bloc de musique
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            Material blockType = clickedBlock.getType();

            if (blockType == Material.JUKEBOX || blockType == Material.NOTE_BLOCK) {
                // Remplacer le bloc par une TNT allumée

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        clickedBlock.setType(Material.AIR);
                    }
                }.runTaskLater(pluginRef, 1);

                TNTPrimed tnt = (TNTPrimed) clickedBlock.getWorld().spawnEntity(clickedBlock.getLocation().add(0.5, 0.0, 0.5), EntityType.PRIMED_TNT);
                //tnt.setFuseTicks(80); // Réglez le temps de détonation de la TNT (facultatif)

                Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "Le kouffar " + event.getPlayer().getName() + " a voulu écouté de la musique ! (+5 Pêchés)");
                ajouterPechesAuJoueur(event.getPlayer().getUniqueId(), 5);
            }
        }
    }

}
