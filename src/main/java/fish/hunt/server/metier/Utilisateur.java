package fish.hunt.server.metier;

import fish.hunt.server.outils.EloSysteme;

import java.io.Serializable;

public class Utilisateur implements Serializable {

    private String pseudo;
    private String password;
    private int points;

    public Utilisateur(String pseudo, String password) {
        this(pseudo, password, EloSysteme.NB_POINTS_INIT);
    }

    public Utilisateur(String pseudo, String password, int points) {
        this.pseudo = pseudo;
        this.password = password;
        this.points = points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getPseudo() {
        return pseudo;
    }

    public String getPassword() {
        return password;
    }

    public int getPoints() {
        return points;
    }
}
