package fish.hunt.server.runnable;

import fish.hunt.server.Serveur;
import fish.hunt.server.metier.Utilisateur;
import fish.hunt.server.outils.DataManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnexionUtilisateur implements Runnable{

    private Serveur serveur;
    private Socket socket;
    private DataManager dataManager;

    private final int CONNECTE = 0;
    private final int CONNEXION_REFUSE = 1;

    private final int NOUVEAU_UTILISATEUR = 100;
    private final int CONNEXION = 101;

    private final int PSEUDO_ACCEPTE = 10;
    private final int PSEUDO_REFUSE = 11;

    public ConnexionUtilisateur(Socket socket, Serveur serveur) {
        this(socket, serveur, DataManager.getInstance());
    }

    public ConnexionUtilisateur(Socket socket, Serveur serveur, DataManager dataManager) {
        this.socket = socket;
        this.serveur = serveur;
        this.dataManager = dataManager;
    }

    @Override
    public void run() {
        BufferedReader input;
        PrintWriter outPut;

        try {
            input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            outPut = new PrintWriter(socket.getOutputStream(), true);

            boolean connecte = false;
            String pseudo = null, password = null;
            Utilisateur utilisateur = null;
            while (!connecte && socket.isConnected()) {

                switch (input.read()) {

                    case NOUVEAU_UTILISATEUR:

                        //On choisit le pseudo.
                        boolean pseudoChoisi = false;
                        while(!pseudoChoisi && socket.isConnected()) {
                            pseudo = input.readLine();
                            if (pseudo == null)
                                return;

                            if(dataManager.pseudoDisponible(pseudo)) {
                                pseudoChoisi = true;
                            }else
                                outPut.println(PSEUDO_REFUSE);

                        }
                        outPut.println(PSEUDO_ACCEPTE);

                        //On choisit le mot de passe.
                        password = input.readLine();
                        if(password == null)
                            return;

                        utilisateur = new Utilisateur(pseudo, password);
                        dataManager.enregistrerUtilisateur(utilisateur);

                        connecte = true;

                        break;

                    case CONNEXION:

                        boolean identifie = false;
                        while(!identifie && socket.isConnected()) {
                            pseudo = input.readLine();
                            if(pseudo == null)
                                return;

                            password = input.readLine();
                            if(password == null)
                                return;

                            //Si le pseudo ou le mot de passe est erroné, alors
                            //utilisateur = null.
                            utilisateur = dataManager.chargerUtilisateur(pseudo, password);
                            if(utilisateur != null)
                                identifie = true;
                            else
                                outPut.println(CONNEXION_REFUSE);
                        }

                        connecte = true;
                }

            }
            serveur.nouvelleConnexion(utilisateur);
            outPut.println(CONNECTE);

            new Thread(new Menu(serveur, socket, utilisateur)).start();

        } catch (IOException ioException) {
            System.out.println("Erreur de connexion");
        }

    }
}
