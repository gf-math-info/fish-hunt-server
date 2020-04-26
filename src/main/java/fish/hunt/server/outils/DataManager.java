package fish.hunt.server.outils;

import fish.hunt.server.metier.Utilisateur;

import java.nio.file.Files;
import java.nio.file.Path;

public class DataManager {

    private static final String CHEMIN = "data/";
    private static final String UTILISATEURS_CHEMIN = "data/utilisateurs/";

    public synchronized static boolean pseudoDisponible(String pseudo) {
        if(pseudo.matches("\\W") && pseudo.strip().length() == 0)
            //On accepte que les symboles alphanumériques et non vide.
            return false;

        if(!Files.exists(Path.of(UTILISATEURS_CHEMIN + pseudo)))
            return true;

        return false;
    }

    public synchronized static void enregistrerUtilisateur(Utilisateur utilisateur) {
        //TODO
    }

    public synchronized static Utilisateur chargerUtilisateur(String pseudo, String password) {
        //TODO
    }

    //TODO
}
