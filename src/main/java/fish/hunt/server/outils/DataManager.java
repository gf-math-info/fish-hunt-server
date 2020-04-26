package fish.hunt.server.outils;

import fish.hunt.server.metier.Utilisateur;

import java.nio.file.Files;
import java.nio.file.Path;

public class DataManager {

    private final String CHEMIN = "data/";
    private final String UTILISATEURS_CHEMIN = "data/utilisateurs/";

    private final static DataManager instance = new DataManager();

    private DataManager() {};

    public static DataManager getInstance() {
        return instance;
    }

    public synchronized boolean pseudoDisponible(String pseudo) {
        if(pseudo.matches("\\W") && pseudo.strip().length() == 0)
            //On accepte que les symboles alphanumériques et non vide.
            return false;

        if(!Files.exists(Path.of(UTILISATEURS_CHEMIN + pseudo)))
            return true;

        return false;
    }

    public synchronized void enregistrerUtilisateur(Utilisateur utilisateur) {
        //TODO
    }

    public synchronized Utilisateur chargerUtilisateur(String pseudo, String password) {
        //TODO
        return null;
    }

    //TODO
}
