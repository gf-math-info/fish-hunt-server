package fish.hunt.server.metier;

import java.util.ArrayList;

public class Partie {

    private ArrayList<Utilisateur> utilisateurs;
    private Utilisateur hote;
    private PartieEtat etat;

    public Partie(Utilisateur hote) {
        this.hote = hote;

        utilisateurs = new ArrayList<Utilisateur>();
        utilisateurs.add(hote);

        etat = PartieEtat.EN_CREATION;
    }

}
