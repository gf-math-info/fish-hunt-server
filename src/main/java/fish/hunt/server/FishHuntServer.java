package fish.hunt.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Cette classe représente le serveur de jeu.
 * @author Fortin-Leblanc, Gabriel
 * @author Colson-Ratelle, Antoine
 */
public class FishHuntServer {

    private static final int PORT = 1337;
    private static final int MAX_JOUEURS_ATTENTES = 25;

    private static final int PSEUDO_ACCEPTE = 110;
    private static final int PSEUDO_REFUSE = 111;
    private static final int ATTAQUE_POISSON_NORMAL_ENVOIE = 150;
    private static final int ATTAQUE_POISSON_SPECIAL_ENVOIE = 151;
    private static final int MISE_A_JOUR_SCORE_ENVOIE = 160;
    private static final int DECONNEXION_JOUEUR_ENVOIE = 190;
    private static final int CONNEXION_JOUEUR_ENVOIE = 191;

    private static final int ATTAQUE_POISSON_NORMAL_RECU = 50;
    private static final int ATTAQUE_POISSON_SPECIAL_RECU = 51;
    private static final int MISE_A_JOUR_SCORE_RECU = 60;

    private static final Object cadenas = new Object();

    private static final ArrayList<PrintWriter> utilisateurs = new ArrayList<>();
    private static final HashMap<PrintWriter, String> pseudos = new HashMap<>();
    private static final HashMap<PrintWriter, Integer> scores = new HashMap<>();

    /**
     * L'entrée du programme.
     * @param args  Aucun argument n'est évalué.
     */
    public static void main(String[] args) {
        try {

            ServerSocket serverSocket = new ServerSocket(PORT, MAX_JOUEURS_ATTENTES);
            System.out.println("Serveur fonctionnel sur le port " + PORT);

            //On lance un joker qui va s'amuser à attaquer les joueurs lorsqu'il y a moins de 4 joueurs.
            new Thread(() -> {

                final long TEMPS_MIN_APPARITION_JOKER = 5000;//En milliseconde.
                final long TEMPS_MAX_APPARITION_JOKER = 10000;//En milliseconde.

                while(true) {

                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextLong(TEMPS_MIN_APPARITION_JOKER,
                                TEMPS_MAX_APPARITION_JOKER));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    synchronized (cadenas) {
                        if(utilisateurs.size() < 4) {
                            if(ThreadLocalRandom.current().nextBoolean()) {
                                for(PrintWriter utilisateur : utilisateurs) {
                                    utilisateur.write(ATTAQUE_POISSON_NORMAL_ENVOIE);
                                    utilisateur.println("Joker");
                                    utilisateur.flush();
                                }
                            } else {
                                for(PrintWriter utilisateur : utilisateurs) {
                                    utilisateur.write(ATTAQUE_POISSON_SPECIAL_ENVOIE);
                                    utilisateur.println("Joker");
                                    utilisateur.flush();
                                }
                            }
                        }
                    }
                }

            }).start();

            while(true) {

                Socket client = serverSocket.accept();
                System.out.println("Nouveau client.");

                new Thread(() -> {

                    BufferedReader input;
                    PrintWriter output = null;
                    String pseudo = null;

                    try {

                        input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        output = new PrintWriter(client.getOutputStream(), true);

                        //On attend un pseudo non vide et qui n'est pas
                        //utilisé, sinon on redemande.
                        boolean pseudoAccepte = false;
                        System.out.println("Attente du pseudo du client...");
                        while (!pseudoAccepte && client.isConnected()) {

                            pseudo = input.readLine();
                            if(pseudo == null)
                                throw new IOException();

                            pseudoAccepte =
                                    pseudo.strip().length() != 0 &&
                                    pseudo.length() < 11 &&
                                    !pseudos.containsValue(pseudo) &&
                                    !pseudo.equals("Joker");

                            if(!pseudoAccepte) {
                                System.out.println("Pseudo, " + pseudo + " refusé");
                                output.write(PSEUDO_REFUSE);
                                output.flush();
                            }
                        }
                        //En sortant de la boucle, le pseudo est valide.
                        System.out.println("Pseudo, " + pseudo + " accepté.");
                        output.write(PSEUDO_ACCEPTE);
                        output.flush();

                        synchronized (cadenas) {
                            for(PrintWriter utilisateur : utilisateurs) {
                                utilisateur.write(CONNEXION_JOUEUR_ENVOIE);
                                utilisateur.println(pseudo);
                                utilisateur.flush();
                            }

                            //On garde le pseudo en mémoire.
                            utilisateurs.add(output);
                            pseudos.put(output, pseudo);
                            scores.put(output, 0);

                            //On envoie le score de tous les joueurs dans la partie.
                            output.write(utilisateurs.size());
                            for(PrintWriter utilisateur : utilisateurs) {
                                output.println(pseudos.get(utilisateur));
                                output.write(scores.get(output));
                            }
                            output.flush();
                        }

                        //Entre dans la partie.
                        int requete;
                        while((requete = input.read()) != -1) {

                            switch (requete) {

                                case ATTAQUE_POISSON_NORMAL_RECU:

                                    synchronized (cadenas) {

                                        System.out.println("Attaque de poisson normal par " + pseudo);

                                        for(PrintWriter utilisateur : utilisateurs) {
                                            if(!utilisateur.equals(output)) {
                                                /*Pour tous les autres joueurs,
                                                on leur envoie un signal*/
                                                utilisateur.write(ATTAQUE_POISSON_NORMAL_ENVOIE);
                                                utilisateur.println(pseudo);
                                                utilisateur.flush();
                                            }
                                        }

                                    }

                                    break;

                                case ATTAQUE_POISSON_SPECIAL_RECU:

                                    synchronized (cadenas) {

                                        System.out.println("Attaque de poisson spécial par " + pseudo);

                                        for(PrintWriter utilisateur : utilisateurs) {
                                            if(!utilisateur.equals(output)) {
                                                utilisateur.write(ATTAQUE_POISSON_SPECIAL_ENVOIE);
                                                utilisateur.println(pseudo);
                                                utilisateur.flush();
                                            }
                                        }

                                    }

                                    break;

                                case MISE_A_JOUR_SCORE_RECU:
                                    int score = input.read();
                                    if(score == -1)//Le joueur est déconnecté.
                                        throw new IOException();

                                    else {

                                        synchronized (cadenas) {

                                            System.out.println("Mise à jour du score de " + pseudo +
                                                    " avec " + score + " points.");
                                            for(PrintWriter utilisateur : utilisateurs) {
                                                utilisateur.write(MISE_A_JOUR_SCORE_ENVOIE);
                                                utilisateur.println(pseudo);
                                                utilisateur.write(score);
                                                utilisateur.flush();
                                            }

                                            //On met à jour le score à l'interne.
                                            scores.replace(output, score);

                                        }

                                    }

                                    break;

                                default:
                                    System.err.println("Mauvaise requête : " + requete);

                            }

                        }

                        System.out.println("Déconnexion de " + pseudo + "...");
                        utilisateurs.remove(output);
                        for(PrintWriter utilisateur : utilisateurs) {
                            utilisateur.write(DECONNEXION_JOUEUR_ENVOIE);
                            utilisateur.println(pseudos.get(output));
                            utilisateur.flush();
                        }
                        pseudos.remove(output);
                        scores.remove(output);

                        input.close();
                        output.close();
                        client.close();
                        System.out.println("Connexion avec " + pseudo + " terminée.");
                    } catch(IOException ioException) {

                        System.err.println("Erreur de connexion ou déconnexion anticipée de " + pseudo + ".");

                        utilisateurs.remove(output);
                        pseudos.remove(output);
                        scores.remove(output);
                    }

                }).start();

            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
