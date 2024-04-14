package islamocraft.islamocraft.handlers;

import islamocraft.islamocraft.IslamoCraft;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.w3c.dom.Text;

import java.util.*;

import static islamocraft.islamocraft.util.RoleplayManager.*;

public class HallalHandler implements Listener
{
    private IslamoCraft pluginRef;
    private Location MecqueLocation;

    public static final List<String> MobsPourAbattageRituel = Arrays.asList("COW","SHEEP","CHICKEN","RABBIT","MOOSHROOM");
    private Map<Player, BukkitTask> boussolesMecque = new HashMap<>();

    private static boolean ramadanActif = false;
    private Random random = new Random();

    public HallalHandler(IslamoCraft plugin)
    {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        this.pluginRef = plugin;
        this.MecqueLocation = Bukkit.getWorlds().get(0).getSpawnLocation();

        // Planifier le lancement de l'événement toutes les 10 secondes
        Bukkit.getScheduler().runTaskTimer(pluginRef, () -> {
            if (!ramadanActif && random.nextInt(100) < 5) { // 5% de chance
                ramadanActif = true;
                commencerLeRamadan();

            }
        }, 0, 10 * 20); // 10 secondes en ticks
    }

    public static String cestLeRamadanOuPas() {
        if(ramadanActif)
        {
            return "OUI";
        }
        else
        {
            return "NON";
        }
    }

    private void commencerLeRamadan() {

        // Afficher un message de titre à tous les joueurs
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(ChatColor.GOLD + "C'est le Ramadan !", ChatColor.BOLD + "Plus le droit de bouffer en journée !", 10, 70, 20);
        }

        // Mettre en pause le thread pendant 40 minutes
        Bukkit.getScheduler().runTaskLater(pluginRef, () -> {
            ramadanActif = false;
            Bukkit.broadcastMessage(ChatColor.GOLD + "L'event Ramadan est terminé.");
        }, 40 * 20 * 60); // 40 minutes en ticks
    }

    private boolean cestLaNuit(World world) {
        long time = world.getTime();
        return time > 12541 && time < 23458; // Plage de temps correspondant à la nuit
    }

    @EventHandler
    public void leJoueurMange(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (ramadanActif && !cestLaNuit(player.getWorld())) {
            player.getWorld().strikeLightning(player.getLocation()); // Frapper le joueur avec la foudre
            Bukkit.broadcastMessage(ChatColor.RED + "Le kouffar "+ player.getName() + "a cassé son Ramadan ! (+50 Péchés)");

            ajouterPechesAuJoueur(player.getUniqueId(), 50);
        }
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
        if (lAttaquant != null && pechesDeVictime > 0 && (lAttaquant != laVictime))
        {
            Bukkit.broadcastMessage(ChatColor.GREEN + "Le pieux " + lAttaquant.getName() + " a pourfendu le kouffar " + laVictime.getName() + " et a récupéré ses " + pechesDeVictime + " points!");

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


    //Un animal comestible doit toujours être abattu en direction de La Mecque
    //La Mecque, c'est le SPAWN, easy as that
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        envoyerLaDirectionDeLaMecque(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        BukkitTask boussole = boussolesMecque.remove(event.getPlayer());
        if(boussole != null)
        {
            boussole.cancel();
        }
    }

    @EventHandler
    public void onKillUneBestioleComestible(EntityDeathEvent event) {
        Player leJoueur = event.getEntity().getKiller();

        //Le joueur bute un animal qu'il s'apprête à bouffer
        if(MobsPourAbattageRituel.contains(event.getEntity().getType().toString()))
        {
            //le joueur est tourné vers La Mecque
            if(leJoueurEstTourneVersLaMecque(leJoueur))
            {
                ajouterHassanatesAuJoueur(leJoueur.getUniqueId(), 5);
            }
            else
            {
                leJoueur.getWorld().strikeLightning(leJoueur.getLocation()); // Frapper le joueur avec la foudre
                Bukkit.broadcastMessage(ChatColor.RED + "Le kouffar "+ leJoueur.getName() + " a tué un animal sans respecter le rite sacré ! (+20 Péchés)");

                ajouterPechesAuJoueur(leJoueur.getUniqueId(), 20);
            }
        }
    }

    public void envoyerLaDirectionDeLaMecque(Player player)
    {
        BukkitTask boussole = new BukkitRunnable() {
            @Override
            public void run() {
                Location targetLocation = new Location(player.getWorld(), 0, 0, 0);
                String direction = getDirectionText(player.getLocation(), targetLocation);
                TextComponent textComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', direction));
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);
            }
        }.runTaskTimer(pluginRef, 0, 7);

        boussolesMecque.put(player, boussole);
    }

    public Boolean leJoueurEstTourneVersLaMecque(Player leJoueur)
    {

        Location laMecque = new Location(leJoueur.getWorld(), 0, 0, 0);
        Location leJoueurPos = leJoueur.getLocation();
        Vector directionVector = leJoueurPos.toVector().subtract(laMecque.toVector()).normalize();

        // Calculer la différence de coordonnées entre la position du joueur et le point cible
        double dx = laMecque.getX() - leJoueurPos.getX();
        double dz = laMecque.getZ() - leJoueurPos.getZ();

        // Calculer l'angle horizontal entre la direction actuelle du joueur et la direction vers le point cible
        double angle = Math.toDegrees(Math.atan2(dz, dx)) - leJoueurPos.getYaw();

        // Ajuster l'angle pour être dans la plage de 0 à 360 degrés
        if (angle < 0) {
            angle += 360;
        }

        if (angle >= 337.5 || angle < 22.5) {
            return true;
        }
        else {
            return false;
        }
    }

    public String getDirectionText(Location playerLocation, Location targetLocation) {
        // Calculer le vecteur de direction vers le point cible
        Vector directionVector = targetLocation.toVector().subtract(playerLocation.toVector()).normalize();

        // Calculer la différence de coordonnées entre la position du joueur et le point cible
        double dx = targetLocation.getX() - playerLocation.getX();
        double dz = targetLocation.getZ() - playerLocation.getZ();

        // Calculer l'angle horizontal entre la direction actuelle du joueur et la direction vers le point cible
        double angle = Math.toDegrees(Math.atan2(dz, dx)) - playerLocation.getYaw();

        // Ajuster l'angle pour être dans la plage de 0 à 360 degrés
        if (angle < 0) {
            angle += 360;
        }

        ChatColor couleur = ChatColor.RED;
        String fleche = "";

        // Convertir l'angle en direction cardinale
        if (angle >= 337.5 || angle < 22.5) {
            couleur = ChatColor.GREEN;
            fleche = "↑";
        } else if (angle >= 22.5 && angle < 67.5) {
            fleche = "↗";
        } else if (angle >= 67.5 && angle < 112.5) {
            fleche = "→";
        } else if (angle >= 112.5 && angle < 157.5) {
            fleche = "↘";
        } else if (angle >= 157.5 && angle < 202.5) {
            fleche = "↓";
        } else if (angle >= 202.5 && angle < 247.5) {
            fleche = "↙";
        } else if (angle >= 247.5 && angle < 292.5) {
            fleche = "←";
        } else if (angle >= 292.5 && angle < 337.5) {
            fleche = "↖";
        } else {
            return null;
        }

        String direction = couleur + "Direction de la Mecque : " + fleche;

        return direction;
    }

}
