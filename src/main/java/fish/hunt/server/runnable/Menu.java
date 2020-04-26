package fish.hunt.server.runnable;

import fish.hunt.server.Serveur;
import fish.hunt.server.metier.Utilisateur;

import java.net.Socket;

public class Menu implements Runnable{

    private Serveur serveur;
    private Socket socket;
    private Utilisateur utilisateur;

    public Menu(Serveur serveur, Socket socket, Utilisateur utilisateur) {
        this.serveur = serveur;
        this.socket = socket;
        this.utilisateur = utilisateur;
    }

    @Override
    public void run() {

    }
}
