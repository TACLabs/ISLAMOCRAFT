package islamocraft.islamocraft.util;

import islamocraft.islamocraft.IslamoCraft;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RoleplayManager implements Listener {

    private IslamoCraft pluginRef;
    private static Map<UUID, PointsDuJoueur> joueurs = new HashMap<>();

    private static Map<Location, ObjetDeVente> marche = new HashMap<>();

    private static Map<UUID, String> nomsRP = new HashMap<>();
    private static Map<UUID, String> vraisPseudos = new HashMap<>();

    private static int dernierID = 0;
    private static int ajouts = 0;

    public RoleplayManager(IslamoCraft plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        pluginRef = plugin;

        try {
            recupererItemsDeLaBDD(Databaise.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getMontantHassanatesDuJoueur(Player joueur) {
        PointsDuJoueur points = joueurs.get(joueur.getUniqueId());
        if (points != null) {
            return points.getHassanates();
        } else {
            return 0;
        }
    }

    public static int getMontantPechesDuJoueur(Player joueur) {
        PointsDuJoueur points = joueurs.get(joueur.getUniqueId());
        if (points != null) {
            return points.getPeches();
        } else {
            return 0;
        }
    }

    public static void ajouterPechesAuJoueur(UUID identifiant, int montant) {
        joueurs.get(identifiant).addPeches(montant);
    }

    public static void supprimerPechesDuJoueur(UUID identifiant) {
        joueurs.get(identifiant).addHassanates(joueurs.get(identifiant).getPeches() * -1);
    }

    public static void ajouterHassanatesAuJoueur(UUID identifiant, int montant) {
        joueurs.get(identifiant).addHassanates(montant);
    }

    public static void retirerHassanatesAuJoueur(UUID identifiant, int montant) {
        joueurs.get(identifiant).addHassanates(montant * -1);
    }

    public static void retirerPechesAuJoueur(UUID identifiant, int montant) {
        joueurs.get(identifiant).addHassanates(montant * -1);
    }

    private static boolean leJoueurEtaitDejaLa(Connection connexion, UUID identifiant) {
        String query = "SELECT COUNT(*) FROM joueurs WHERE uuid = ?";

        try (PreparedStatement preparedStatement = connexion.prepareStatement(query)) {
            preparedStatement.setString(1, identifiant.toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private static PointsDuJoueur getPointsDuJoueur(Connection connexion, UUID identifiant) {
        String query = "SELECT hassanates, peches FROM joueurs WHERE uuid = ?";

        try (PreparedStatement preparedStatement = connexion.prepareStatement(query)) {
            preparedStatement.setString(1, identifiant.toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int hassanates = resultSet.getInt("hassanates");
                    int peches = resultSet.getInt("peches");
                    return new PointsDuJoueur(hassanates, peches);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private static void sauvegardeDuJoueur(Connection connexion, Player joueur) {
        String query = "";

        if (leJoueurEtaitDejaLa(connexion, joueur.getUniqueId())) {
            query = "UPDATE joueurs SET pseudo = ?, nom_rp = ?, hassanates = ?, peches = ? WHERE uuid = ?";

            try (PreparedStatement preparedStatement = connexion.prepareStatement(query)) {
                preparedStatement.setString(1, vraisPseudos.get(joueur.getUniqueId()));
                preparedStatement.setString(2, nomsRP.get(joueur.getUniqueId()));
                preparedStatement.setInt(3, getMontantHassanatesDuJoueur(joueur));
                preparedStatement.setInt(4, getMontantPechesDuJoueur(joueur));
                preparedStatement.setString(5, joueur.getUniqueId().toString());

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            query = "INSERT INTO joueurs (uuid, pseudo, nom_rp, hassanates, peches) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connexion.prepareStatement(query)) {
                preparedStatement.setString(1, joueur.getUniqueId().toString());
                preparedStatement.setString(2, vraisPseudos.get(joueur.getUniqueId()));
                preparedStatement.setString(3, nomsRP.get(joueur.getUniqueId()));
                preparedStatement.setInt(4, getMontantHassanatesDuJoueur(joueur));
                preparedStatement.setInt(5, getMontantPechesDuJoueur(joueur));

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void recupererItemsDeLaBDD(Connection connexion) {
        String query = "SELECT * FROM marche";
        int compteur = 0;

        try (PreparedStatement preparedStatement = connexion.prepareStatement(query)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    dernierID = resultSet.getInt("id");

                    Material item = Material.matchMaterial(resultSet.getString("item"));
                    int valeur = resultSet.getInt("valeur");
                    int quantite = resultSet.getInt("quantite");

                    ObjetDeVente objet = new ObjetDeVente(dernierID, item, valeur, quantite);

                    double coord_x = resultSet.getDouble("coord_x");
                    double coord_y = resultSet.getDouble("coord_y");
                    double coord_z = resultSet.getDouble("coord_z");

                    Location localisation = new Location(Bukkit.getWorld("world"),coord_x,coord_y,coord_z,0.0f,0.0f);

                    marche.put(localisation,objet);
                    compteur++;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Bukkit.getLogger().info("Récupération de " + compteur + " objets du marché dans la BDD");
    }


    public static void sauvegarderTousLesJoueurs()
    {
        for (UUID identifiant : joueurs.keySet())
        {
            try {
                sauvegardeDuJoueur(Databaise.getConnection(), Bukkit.getPlayer(identifiant));
                Bukkit.getLogger().info("Sauvegarde d'un joueur : " + Bukkit.getPlayer(identifiant).getDisplayName());

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        joueurs.clear();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        try {
            Connection connexion = Databaise.getConnection();
            Player player = event.getPlayer();

            if(leJoueurEtaitDejaLa(connexion,player.getUniqueId()))
            {
                //Récupérer ses peches et ses hassanates
                joueurs.put(player.getUniqueId(), getPointsDuJoueur(connexion,player.getUniqueId()));
                nomsRP.put(player.getUniqueId(), "suce-bite");
                vraisPseudos.put(player.getUniqueId(), player.getDisplayName());
                player.setDisplayName(nomsRP.get(player.getUniqueId()));
                player.setPlayerListName(nomsRP.get(player.getUniqueId()));
                Bukkit.getLogger().info("Retour d'un joueur : " + vraisPseudos.get(player.getUniqueId()) + " (" + nomsRP.get(player.getUniqueId()) + ")");
            }
            else
            {
                //L'ajouter en tant que tout nouveau membre, il sera sauvegardé plus tard
                joueurs.put(player.getUniqueId(), new PointsDuJoueur(100, 0));
                nomsRP.put(player.getUniqueId(), NomArabeGenerator.genererNomArabe());
                vraisPseudos.put(player.getUniqueId(), player.getDisplayName());
                player.setDisplayName(nomsRP.get(player.getUniqueId()));
                player.setPlayerListName(nomsRP.get(player.getUniqueId()));
                Bukkit.getLogger().info("Nouveau joueur : " + vraisPseudos.get(player.getUniqueId()) + " (" + nomsRP.get(player.getUniqueId()) + ")");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        event.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        try {
            Connection connexion = Databaise.getConnection();
            Player player = event.getPlayer();

            Bukkit.getLogger().info("Sauvegarde d'un joueur : " + player.getDisplayName());

            sauvegardeDuJoueur(connexion,player);
            joueurs.remove(player.getUniqueId());
            nomsRP.remove(player.getUniqueId());
            vraisPseudos.remove(player.getUniqueId());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        event.setQuitMessage(null);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Vérifiez si le joueur a la permission d'utiliser des panneaux (facultatif)
        if (!player.hasPermission("votreplugin.panneaux")) {
            player.sendMessage("Qui a dit que tu avais le droit de t'exprimer ?");
            block.breakNaturally();
            return;
        }

        String[] lines = event.getLines();

        if(lines[0].contains("NIGGER"))
        {
            try {
                int value = Integer.parseInt(lines[1]);
            } catch (NumberFormatException e) {
                event.setCancelled(true);
                player.sendMessage("Faut SAISIR un montant, un nombre ENTIER, donc pas \"" + lines[1] + "\".");
                block.breakNaturally();
                return;
            }

        }
    }

    @EventHandler
    public void onJoueurClicSurLePanneau(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Vérifiez si le joueur a cliqué sur un bloc de type panneau et a effectué un clic droit
        if (clickedBlock != null && clickedBlock.getType().toString().contains("SIGN") && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Sign sign = (Sign) clickedBlock.getState();

            String[] lines = sign.getLines();

            //C'est un ADMINISTRATEUR, et il a la ferme attention d'ajouter un nouvel item au marché
            if(player.hasPermission("votreplugin.panneaux") && lines[0].contains("NIGGER"))
            {
                if(itemInHand == null || itemInHand.getType().isAir())
                {
                    player.sendMessage("Pour vendre un objet, y te faut un objet, dans la main...");
                    return;
                }

                //Ajouter l'objet dans le Map du marché
                int valeur = Integer.parseInt(lines[1]);
                Material item = itemInHand.getType();

                ObjetDeVente objet = new ObjetDeVente(dernierID+ajouts+1,item,valeur,0);
                ajouts++;
                marche.put(clickedBlock.getLocation(),objet);

                //Modifier le panneau

                sign.setLine(0, "§l§cACHAT/VENTE");
                sign.setLine(1, item.name());
                sign.setLine(2, "Valeur : "+valeur);
                sign.setLine(3, "Quantité : 0");
                sign.update();

                event.setCancelled(true);
            }
            //Check si le panneau est enregistré dans LES COORDZ
            else if(marche.containsKey(clickedBlock.getLocation()))
            {
                ObjetDeVente objetEnQuestion = marche.get(clickedBlock.getLocation());

                //S'il a l'item en question en main, il veut VENDRE, sinon, il veut ACHETER
                if(itemInHand.getType() == objetEnQuestion.getObjet())
                {

                }
                else
                {

                }

                event.setCancelled(true);
            }

        }
    }

}
