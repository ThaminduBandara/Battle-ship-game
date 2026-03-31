package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class BattleshipClient {

    private static final int PORT = 5000;

    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);

       
        System.out.print("Enter server IP: ");
        String host = sc.nextLine();

       
        Socket socket = new Socket(host, PORT);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true);

        
        ServerListener listener = new ServerListener(in);
        new Thread(listener).start();

        
        while (true) {
            String input = sc.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            out.println(input);
        }

        socket.close();
        sc.close();
    }
}