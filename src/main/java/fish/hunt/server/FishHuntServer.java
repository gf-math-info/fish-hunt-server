package fish.hunt.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.WeakHashMap;

public class FishHuntServer {

    private static final int PORT = 1337;
    private static final int MAX_JOUEURS_ATTENTES = 25;

    private static final int MAX_RECORD = 3;

    private static final int ATTAQUE_POISSON_ENVOIE = 150;
    private static final int MISE_A_JOUR_SCORE_ENVOIE = 160;
    private static final int MISE_A_JOUR_RECORD_ENVOIE = 161;
    private static final int AJOUT_POISSON_RECU = 50;
    private static final int SCORE_A_JOUR_RECU = 60;
    private static final int PSEUDO_ACCEPTE = 10;
    private static final int PSEUDO_REFUSE = 11;

    private static final Object cadenas = new Object();
    private static ServerSocket serverSocket;

    private static final ArrayList<PrintWriter> utilisateurs =
            new ArrayList<>();
    private static final WeakHashMap<PrintWriter, String> pseudos =
            new WeakHashMap<>();
    private static final ArrayList<Record> records =
            new ArrayList<>(MAX_RECORD);

    public static void main(String[] args) {
        try {

            serverSocket = new ServerSocket(PORT, MAX_JOUEURS_ATTENTES);
            System.out.println("Serveur fonctionnel sur le port " + PORT);

            while(true) {

                Socket client = serverSocket.accept();
                System.out.println("Nouveau client.");

                new Thread(() -> {

                    BufferedReader input;
                    PrintWriter output;

                    try {

                        input = new BufferedReader(
                                new InputStreamReader(client.getInputStream()));
                        output = new PrintWriter(client.getOutputStream(),
                                true);

                        //On attends un pseudo non vide et qui n'est pas
                        //utilisé, sinon on redemande.
                        boolean pseudoAccepte = false;
                        String pseudo = null;
                        System.out.println("Attente du pseudo du client...");
                        while (!pseudoAccepte && client.isConnected()) {

                            pseudo = input.readLine();

                            pseudoAccepte = pseudo != null &&
                                    pseudo.strip().length() != 0 &&
                                    !pseudos.containsValue(pseudo);

                            if(!pseudoAccepte) {
                                output.println(PSEUDO_REFUSE);
                                System.out.println("Pseudo, " + pseudo +
                                        " refusé");
                            }
                        }
                        //En sortant de la boucle, le pseudo est valide.
                        System.out.println("Pseudo, " + pseudo + " accepté.");
                        output.write(PSEUDO_ACCEPTE);
                        output.flush();
                        synchronized (cadenas) {
                            utilisateurs.add(output);
                            pseudos.put(output, pseudo);
                        }

                        //Entre dans la partie.
                        int requete;
                        while((requete = input.read()) != -1) {

                            switch (requete) {

                                case AJOUT_POISSON_RECU://Signal reçu.

                                    synchronized (cadenas) {

                                        System.out.println("Ajout de poisson par " +
                                                pseudos.get(output));

                                        for(PrintWriter out : utilisateurs) {
                                            if(!out.equals(output)) {
                                                /*Pour tous les autres joueurs,
                                                on leur envoie un signal*/
                                                out.println(ATTAQUE_POISSON_ENVOIE);
                                                out.println(pseudos.get(output));
                                            }
                                        }

                                    }

                                    break;

                                case SCORE_A_JOUR_RECU:
                                    int score = input.read();

                                    if(score != -1) {
                                        /*Si la communication continue...*/
                                        synchronized (cadenas) {

                                            System.out.println(
                                                    "Mise à jour du score de " +
                                                    pseudos.get(output) +
                                                            " avec " + score);

                                            boolean nouveauRecord = records.size() != MAX_RECORD ||
                                                    score > records.get(MAX_RECORD - 1).getScore();

                                            if(nouveauRecord) {
                                                if(records.size() == MAX_RECORD)
                                                    records.remove(MAX_RECORD - 1);
                                                records.add(new Record(pseudos.get(output), score));
                                            }

                                            utilisateurs
                                                    .forEach(out -> {

                                                        out.println(MISE_A_JOUR_SCORE_ENVOIE);
                                                        out.println(pseudos.get(output));
                                                        out.println(score);

                                                        if(nouveauRecord) {
                                                            output.println(MISE_A_JOUR_RECORD_ENVOIE);
                                                            output.println(records.size());
                                                            records.forEach(record -> {
                                                                output.println(record.getPseudo());
                                                                output.println(record.getScore());
                                                            });
                                                        }

                                                    });
                                        }

                                    }
                                    break;

                                default:
                                    System.err.println("Mauvaise requête : " +
                                            requete);
                                    break;

                            }

                        }

                        System.out.println("Déconnexion de " + pseudo + "...");
                        pseudos.remove(output);

                        input.close();
                        output.close();
                        client.close();
                        System.out.println("Connexion fermée");
                    } catch(IOException ioException) {
                        System.err.println("Erreur de connexion.");
                    }

                }).start();

            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private synchronized static void miseAJourScore(PrintWriter printWriter,
                                                    int score) {

    }

    private static class Record implements Comparable<Record>{

        private String pseudo;
        private int score;

        public Record(String pseudo, int score) {
            this.pseudo = pseudo;
            this.score = score;
        }

        public String getPseudo() {
            return pseudo;
        }

        public int getScore() {
            return score;
        }

        @Override
        public int compareTo(Record record) {
            return record.score - score;
        }
    }
}
