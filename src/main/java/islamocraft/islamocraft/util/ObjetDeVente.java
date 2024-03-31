package islamocraft.islamocraft.util;

import org.bukkit.Location;
import org.bukkit.Material;

public class ObjetDeVente {

    private int id;
    private Material objet;
    private int valeur;
    private int quantite;

    public ObjetDeVente(int id, Material objet, int valeur, int quantite) {
        this.id = id;
        this.valeur = valeur;
        this.quantite = quantite;
    }

    public Material getObjet() {
        return objet;
    }

    public void setObjet(Material objet) {
        this.objet = objet;
    }

    public int getValeur() {
        return valeur;
    }

    public void setValeur(int valeur) {
        this.valeur = valeur;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
