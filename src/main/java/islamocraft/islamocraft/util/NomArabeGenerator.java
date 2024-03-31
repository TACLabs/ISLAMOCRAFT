package islamocraft.islamocraft.util;

import java.util.Random;

public class NomArabeGenerator {
    private static final String[] prenoms = {"Mahmud", "Ya'qub", "Khalid", "Ismail", "Ahmad", "Hassan", "Ali", "Mustafa", "Abdullah", "Omar", "Muhammad", "Osama", "Khidir", "Usman"};
    private static final String[] prefixes = {"Ibn", "Abu", "Al", "Bin", "As", "Ash", "At", "An"};
    private static final String[] nomsDeFamille = {"Baz", "Qamar Ad-Din", "Abdul-Jalil", "Kashmiri", "Kanabawi", "Hussein", "Nasrallah", "Al-Farouq", "Al-Masri", "Dibiazah", "Karawita", "Sisha", "Laden"};

    public static String genererNomArabe() {
        Random random = new Random();
        String prenom = prenoms[random.nextInt(prenoms.length)];
        String prefixe = prefixes[random.nextInt(prefixes.length)];
        String nomDeFamille = nomsDeFamille[random.nextInt(nomsDeFamille.length)];

        String elementIntermediaire = (random.nextBoolean()) ? nomsDeFamille[random.nextInt(nomsDeFamille.length)] : "";

        return prenom + " " + prefixe + " " + elementIntermediaire + " " + nomDeFamille;
    }

}
