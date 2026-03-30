package server;

import common.Protocol;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final BattleshipServer server;
    private final Socket socket;
    private final int playerId;

    private final BufferedReader in;
    private final PrintWriter out;

    public ClientHandler(BattleshipServer server, Socket socket, int playerId) throws IOException {
        this.server = server;
        this.socket = socket;
        this.playerId = playerId;

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void send(String msg) {
        out.println(msg);
    }

    public void close() throws IOException {
        socket.close();
    }

    @Override
    public void run() {
        try {
            String line;

            while ((line = in.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                String cmd = parts[0];

                if (Protocol.PLACED.equals(cmd)) {
                    if (parts.length != 1 + GameState.SHIPS * 2) {
                        send(Protocol.INFO + " Invalid PLACED format. Use: PLACED r1 c1 r2 c2 r3 c3");
                        continue;
                    }

                    int[] coords = new int[GameState.SHIPS * 2];

                    try {
                        for (int i = 0; i < coords.length; i++) {
                            coords[i] = Integer.parseInt(parts[i + 1]);
                        }
                    } catch (NumberFormatException e) {
                        send(Protocol.INFO + " PLACED values must be numbers.");
                        continue;
                    }

                    server.onPlaced(playerId, coords, this);

                } else if (Protocol.ATTACK.equals(cmd)) {
                    if (parts.length != 3) {
                        send(Protocol.INFO + " Invalid ATTACK format. Use: ATTACK row col");
                        continue;
                    }

                    int r, c;

                    try {
                        r = Integer.parseInt(parts[1]);
                        c = Integer.parseInt(parts[2]);
                    } catch (NumberFormatException e) {
                        send(Protocol.INFO + " ATTACK values must be numbers.");
                        continue;
                    }

                    server.onAttack(playerId, r, c, this);

                } else {
                    send(Protocol.INFO + " Unknown command.");
                }
            }

        } catch (Exception e) {
            System.out.println("Player " + playerId + " disconnected.");
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}