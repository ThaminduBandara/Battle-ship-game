package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class BattleshipClient {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket(HOST, PORT);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

        // 🔥 START LISTENER THREAD
        ServerListener listener = new ServerListener(in);
        Thread thread = new Thread(listener);
        thread.start();

        // MAIN THREAD → handles input
        Scanner sc = new Scanner(System.in);

        while (true) {
            String input = sc.nextLine();
            out.println(input);
        }
    }
}