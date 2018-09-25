package benchmarkgame;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import benchmarkgame.gameutils.Command;
import benchmarkgame.gameutils.LocPair;
import benchmarkgame.gameutils.Move;
import benchmarkgame.gameutils.PositionState;
import benchmarkgame.gameutils.Status;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;


/**
 * Implements the game server.
 * @author juniocezar
 */
public class Server {
    /**
     * Maximum number of threads the server may launch.
     */
    private final int threadLimit = 16;
    /**
     * Maximum number of players may be connected to the server.
     */
    private final int maxPlayers  = 30;
    /**
     * Fixed game map size.
     */
    private final int boardSide   = maxPlayers / 2;
    private String[][] MAP = new String[boardSide][boardSide];
    private Map<String, LocPair> currentPosition; 
    private NetworkManager nm;
    private static Server ref = null;
    private int nextFreePos = 0;
    Random r = new Random();

    /**
     * Returns reference to the server single instance.
     * @return Reference to the server single instance
     */
    public static Server getServerRef() {
        return ref;
    }
    
    /**
     * Constructor: Initializes server by stablishing a connection with the network,
     * initializing the currentPosition and ref variables and initializing the map 
     * of the game with all positions being free, that is, without players.
     *
     * @param ipAddr IP address of the server
     * @throws Exception
     */
    private Server(String ipAddr) throws Exception {
        nm = new NetworkManager(this, ipAddr);
        currentPosition = new HashMap<String, LocPair>();
        ref = this;
        for(int i = 0 ; i < boardSide; i++) {
            for(int j = 0 ; j < boardSide; j++) {
                MAP[i][j] = "free";
            }
        }
    }
    
    /**
     * Returns a reference to the server or creates a new one and returns.
     * @param ipAddr IP address of the server
     * @return Reference to the server single instance
     * @throws Exception 
     */
    public static Server v(String ipAddr) throws Exception {
        if (ref != null)
            return ref;
        
        return new Server(ipAddr);
    }
    
    /**
     * Listen to client new requests through the NetworkManager.
     * @throws Exception 
     */
    private void init() throws Exception {
        System.out.println("\r\nRunning Game Server: " + 
                "Host=" + nm.getSocketAddress().getHostAddress() + 
                " Port=" + nm.getPort());
        
        nm.listen();
    }
    
    // toDo: replace use of random (is is required?)
    /**
     * Chooses a random initial position for a new player.
     *
     * @param clientID New player ID
     * @return Status Enum status describing the success or fail of operation
     */
    public Status randomPosition(String clientID) {
        //if (nextFreePos >= boardSide) 
        //  this way ^^^^^^^^^^^^^, with the nextFreePos variable starting from 0, 
        //we allow only 15 map positions to be used
        if (nextFreePos >= (boardSide*boardSide)) {
            return Status.FAILED;
        }
        
        if (! currentPosition.containsKey(clientID)) {
            synchronized(MAP) {
                // math.floor is redundant, just to use in explanation
                int x = (int)Math.floor(nextFreePos / boardSide); 
                int y = nextFreePos % boardSide;
                LocPair pos = new LocPair(x,y);
                currentPosition.put(clientID, pos);
                nextFreePos++;
            }
        }
        
        return Status.OK;
    }
    
    /**
     * Updates the player position in the game map.
     *
     * @param clientID Player identification.
     * @param x Change in the direction x.
     * @param y Change in the direction y.
     * @return Status Enum status describing the success or fail of operation.
     */
    public Status updatePosition(String clientID, int x, int y) {
        LocPair pos = currentPosition.get(clientID);
        // checking future position
        x += pos.x;
        y += pos.y;
        
        if (x >= boardSide || y >= boardSide)
            return Status.FAILED;
        
        if (x <= 0 || y <= 0)
            return Status.FAILED;
        
        synchronized (MAP) {
            switch(MAP[x][y]) {
                case "free":
                    // update game map
                    MAP[x][y] = clientID;
                    MAP[pos.x][pos.y] = "free";

                    // update hashmap pos reference
                    pos.x = x;
                    pos.y = y;
                    return Status.OK;
                default:
                    return Status.FAILED;
            }
        }
    }
    
    @Override
    public String toString(){
        String out = "";
        for(int i = 0 ; i < boardSide; i++) {
            for(int j = 0 ; j < boardSide; j++) {
                if(MAP[j][i] == "free")
                    out += "0 ";
                else
                    out += "X ";
                //out += MAP[i][j] + "\t";
            }
            out += "\n";
        }
        return out;
    }
    
    public static void main(String[] args) throws Exception {
        final Server app = Server.v(args[0]); // a reference to the server
        
        //creating a thread by implementing its run method 
        //thread function: create a file that stores the game state every second
        Thread printer = new Thread() {              
                public void run(){
                    BufferedWriter writer = null;
                    try {
                        writer = new BufferedWriter(new FileWriter("state.txt"));
                        writer.write("Game State:\n");
                        while(true) {                        
                            writer.write(app.toString());
                            writer.write("\n\n");
                            Thread.sleep(500);
                        }
                        
                    } catch (Exception e) {
                        // do something
                    }
                    // writer.close();
                }
            };
        
        printer.start();
        
        app.init();
    }
}
