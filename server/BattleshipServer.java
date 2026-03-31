//package com.battleship.server;
package server;
import common.Protocol;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BattleshipServer {

    private static final int PORT = 5000;
        
        private ClientHandler p1;
        private ClientHandler p2;

        private final GameState game= new GameState();
        private boolean started=false;
        private int turn=1;

        
        public static void main(String[] args) throws IOException{
            new BattleshipServer().start();
        }

        public void start() throws IOException{
            try(ServerSocket serverSocket = new ServerSocket(PORT)){
                System.out.println("Server is listening on port " + PORT);
                System.out.println("Waiting for player 1 to connect...");
                Socket p1Socket = serverSocket.accept();
                p1 = new ClientHandler(this,p1Socket,1);
                new Thread(p1, "P1-Handler").start();

                System.out.println("waiting for player 2 to connect...");
                Socket p2Socket = serverSocket.accept();
                p2 = new ClientHandler(this,p2Socket,2);
                new Thread(p2, "P2-Handler").start();

                p1.send(Protocol.WELCOME + "P1");
                p2.send(Protocol.WELCOME + "P2");

                p1.send(Protocol.PLACE);
                p2.send(Protocol.PLACE);

                System.out.println("Both players connected. waiting for ship placement.");


                
            }
        }
        // ClientHandler will call this when a player places ships
        public synchronized void  onPlaced(int playerID, int[] coords, ClientHandler from){
            boolean ok = game.placeShips(playerID, coords);
            if(!ok){
                from.send(Protocol.INFO + "Invalid ship placement. Try again.");
                from.send(Protocol.PLACE);
                return;
            }

            from.send(Protocol.INFO + "Ships placed successfully. Waiting for opponent.");

            if(!started && game.bothPlaced()){
                started = true;
                p1.send(Protocol.START);
                p2.send(Protocol.START);
                updateTurnMessages();
                System.out.println("Game started");

            }

        }
        // ClientHandler will call this when a player attacks
        public synchronized void onAttack(int playerId, int r, int c, ClientHandler from){
            if(!started){
                from.send(Protocol.INFO + "Game has not started yet. Wait for both players to place ships.");
                return;
            }
            if(playerId != turn){
                from.send(Protocol.INFO + "It's not your turn. Please wait.");
                return;
            }

            String result = game.attack(playerId,r,c);
            from.send(Protocol.RESULT + " "+result);

            ClientHandler opponent = (playerId == 1) ? p2 : p1;
            opponent.send(Protocol.INFO + "Opponent attacked (" + r + "," + c + "): " + result);

            if(game.isGameOver()){
                int winner = game.winnerIdOr0();
                if (winner == 1){
                    p1.send(Protocol.WIN);
                    p2.send(Protocol.LOSE);
                }
                else {
                    p2.send(Protocol.WIN);
                    p1.send(Protocol.LOSE);
                }
                safeClose();
                System.out.println("Game over. Player " + winner + " wins.");
                return;
            }
            // switch turn (don’t switch if REPEAT is used as “invalid shot”)
            if(!"REPEAT".equals(result)) turn = (turn == 1) ? 2 : 1;
            updateTurnMessages();

        }
       private void updateTurnMessages() {
        if (turn == 1) {
            p1.send(Protocol.YOUR_TURN);
            p2.send(Protocol.OPPONENT_TURN);
        } else {
            p2.send(Protocol.YOUR_TURN);
            p1.send(Protocol.OPPONENT_TURN);
        }
    }

    private void safeClose() {
        try { if (p1 != null) p1.close(); } catch (Exception ignored) {}
        try { if (p2 != null) p2.close(); } catch (Exception ignored) {}
    }
    
    
}

