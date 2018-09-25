    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package benchmarkgame;

import benchmarkgame.gameutils.Move;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

/**
 * Implements clients for our simple game benchmark.
 * @author juniocezar
 */
public class Clients {
    /**
     * An endpoint for communication between two machines, 
     * establishes the client-server connection.
     */
    public Socket socket;
    private Scanner scanner;
    /**
     * An instance of the Random class that generates a stream of pseudorandom 
     * numbers that is used to generate movements for the players ramdomly. 
     */
    private static Random random = new Random();
    
    /**
     * Constructor: Creates a stream socket for the client/player
     * and connects it to the specified port number at the specified IP address.
     *
     * @param serverAddress IP address of the server
     * @param serverPort server port number
     * @throws Exception
     */
    public Clients(InetAddress serverAddress, int serverPort) throws Exception {
        this.socket = new Socket(serverAddress, serverPort);
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Chooses a movement ramdomly among the possible ones.
     *
     * @param clazz the Move enum defined in benchmarkgame.gameutils.Move
     * @return a movement
     */
    public static <T extends Enum<?>> T randomEnum(Class<T> clazz){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
    /**
     * Tries to move the player movesPerClient times through the game map following 
     * the directions that are obtained randomly by the function {@link randomEnum}. 
     * The client sends a message to the server with the desired/chosen command 
     * through the created socket. 
     * Command syntax: "MOVE (some direction)" - e.g. "MOVE UP"
     *
     * @param movesPerClient number of movements per player
     * @throws IOException
     * @throws InterruptedException
     */
    public void startToMove(int movesPerClient) throws IOException, InterruptedException {       
        for(int i=0; i<movesPerClient; i++) {
            Move m = randomEnum(Move.class);
            String send = "MOVE " + m;
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            out.println(send);
            out.flush();
        }
    }

    /**
     * Sends the SHOW command to the server through the created socket, that is, 
     * asks the server to show the current state of the game map. 
     *
     * @throws IOException
     */
    private void sendShow() throws IOException {
        String send = "SHOW";
        PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
        out.println(send);
        out.flush();
    }
}