package fish.hunt.server;

import fish.hunt.server.metier.Partie;
import fish.hunt.server.metier.Utilisateur;
import fish.hunt.server.runnable.ConnexionUtilisateur;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Serveur {

    private int port;
    private int maxClient;
    private ServerSocket serverSocket;

    private ArrayList<Partie> parties;
    private ArrayList<Utilisateur> utilisateurs;

    public Serveur(int port, int maxClient) throws IOException {
        parties = new ArrayList<>();
        utilisateurs = new ArrayList<>();

        this.port = port;
        this.maxClient = maxClient;
        serverSocket = new ServerSocket(port, maxClient);
    }

    public void ecoute() throws IOException {
        while(true) {

            System.out.println("Écoute sur le port " + port + ".");
            new Thread(new ConnexionUtilisateur(serverSocket.accept(), this))
                    .start();
            System.out.println("Nouvelle connexion.");
        }
    }

    public synchronized void nouvellePartie(Partie partie) {
        parties.add(partie);
    }

    public synchronized void nouvelleConnexion(Utilisateur utilisateur) {
        utilisateurs.add(utilisateur);
    }

}
