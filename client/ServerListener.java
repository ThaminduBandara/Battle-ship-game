package client;

import java.io.BufferedReader;
import java.io.IOException;

public class ServerListener implements Runnable {

    private BufferedReader in;

    public ServerListener(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String response;

            while ((response = in.readLine()) != null) {
                System.out.println("SERVER: " + response);

                
            }

        } catch (IOException e) {
            System.out.println("Connection closed.");
        }
    }
}