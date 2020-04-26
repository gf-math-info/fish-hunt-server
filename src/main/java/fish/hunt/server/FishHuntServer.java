package fish.hunt.server;

import java.io.IOException;

public class FishHuntServer {


    public static void main(String[] args) {

        try {

            Serveur serveur = new Serveur(1337, 100);
            serveur.ecoute();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }
}
