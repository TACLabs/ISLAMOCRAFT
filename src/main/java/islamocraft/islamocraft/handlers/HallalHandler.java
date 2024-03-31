package islamocraft.islamocraft.handlers;

import islamocraft.islamocraft.IslamoCraft;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import static islamocraft.islamocraft.util.RoleplayManager.*;

public class HallalHandler implements Listener
{
    private IslamoCraft pluginRef;

    public HallalHandler(IslamoCraft plugin)
    {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.pluginRef = plugin;
    }

    //Pour récuperer des pierres afin de lapider les mécréants
    @EventHandler
    public void onClicDroitSurLeGravier(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        ItemStack itemEnMain = player.getInventory().getItemInMainHand();

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType() == Material.GRAVEL && (itemEnMain == null || itemEnMain.getType() == Material.AIR)) {

            ItemStack item = new ItemStack(Material.SNOWBALL, 10);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Pierre de lapidation");
            meta.setCustomModelData(69);
            item.setItemMeta(meta);

            player.getInventory().setItemInMainHand(item);
        }
    }

    //Pour récupérer la pisse du chameau qui possède des vertus extraordinaires selon les écrits coraniques
    @EventHandler
    public void onClicDroitSurLeChameau(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        PotionEffect fatigueApresLaTraite = new PotionEffect(PotionEffectType.SLOW, 20*60*10, 1);
        LivingEntity siChameau = (LivingEntity)entity;

        if(entity.getType() == EntityType.CAMEL && !(siChameau.hasPotionEffect(PotionEffectType.SLOW)))
        {

            if(player.getInventory().getItemInMainHand().getType() == Material.BUCKET)
            {
                ItemStack item = new ItemStack(Material.MILK_BUCKET, 1);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("Seau de pisse");
                meta.setCustomModelData(69);
                item.setItemMeta(meta);

                player.getInventory().setItemInMainHand(item);

                fatigueApresLaTraite.apply((LivingEntity)entity);

                siChameau.setPassenger(null);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onJoueurQuiEnButeUnAutre(PlayerDeathEvent event)
    {
        Player laVictime = event.getEntity();
        Player lAttaquant = laVictime.getKiller();

        int pechesDeVictime = getMontantPechesDuJoueur(laVictime);

        //Si le joueur a été buté et que c'était un kouffar
        if (lAttaquant != null && pechesDeVictime > 0)
        {
            Bukkit.getLogger().info("Le pieux " + lAttaquant.getName() + " a pourfendu le kouffar " + laVictime.getName() + " et a récupéré ses " + pechesDeVictime + " points!");

            //Retirer les péchés de la victime
            supprimerPechesDuJoueur(laVictime.getUniqueId());

            //Les redonne sous forme d'hassanates à l'attaquant
            ajouterHassanatesAuJoueur(lAttaquant.getUniqueId(), pechesDeVictime);
        }
    }



    //Quand le joueur fini par boire la pisse
    @EventHandler
    public void onBoireLaPisseDuChameau(PlayerItemConsumeEvent event)
    {
        Player player = event.getPlayer();

        if(event.getItem().getType() == Material.MILK_BUCKET && event.getItem().getItemMeta() != null && event.getItem().getItemMeta().getCustomModelData() == 69)
        {
            new BukkitRunnable() {
                @Override
                public void run() {
                    PotionEffect goldenAppleEffect = new PotionEffect(PotionEffectType.REGENERATION, 20*60*2, 1);
                    player.addPotionEffect(goldenAppleEffect);
                }
            }.runTaskLater(pluginRef, 1);
        }
    }
}
