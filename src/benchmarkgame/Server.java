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
     * @return 
     */
    public static Server getServerRef() {
        return ref;
    }
    
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
     * @param ipAddr: IP address of the server.
     * @return: Reference to the server single instance.
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
     * @param clientID: New player id.
     */
    public Status randomPosition(String clientID) {
        if (nextFreePos >= boardSide) {
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
     * @param clientID: Player identification.
     * @param x: Change in the direction x.
     * @param y: Change in the direction y.
     * @return Status: Enum status describing the success or fail of operation.
     */
    public Status updatePosition(String clientID, int x, int y) {
        LocPair pos = currentPosition.get(clientID);
        // cheching future position
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
                if(MAP[i][j] == "free")
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
        Server app = Server.v(args[0]);
        
        Thread printer = new Thread() {              
                public void run(){
                    BufferedWriter writer = null;
                    try {
                        writer = new BufferedWriter(new FileWriter("state.txt"));
                        writer.write("Game State:\n");
                        while(true) {                        
                            writer.write(app.toString());
                            writer.write("\n\n");
                            Thread.sleep(1000);
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

/**
 * Implements a network manager that handles new user requests, as well user 
 * operations.
 * @author juniocezar
 */
class NetworkManager {
    private ServerSocket server;
    private int threadLimit = 16;
    private Server s;
    
    
    /**
     * Initializes server, opening socket on input IP address.
     * @param ipAddress: IP address to be used be the server.
     * @throws Exception 
     */
    public NetworkManager(Server _s, String ipAddress) throws Exception {
        if (ipAddress != null && !ipAddress.isEmpty()) 
          this.server = new ServerSocket(0, 1, InetAddress.getByName(ipAddress));
        else 
          this.server = new ServerSocket(0, 1, InetAddress.getLocalHost());
        s = _s;
    }
    
    /**
     * Listen for client connections, launching threads for each new connection
     * @throws Exception 
     */
    public void listen() throws Exception {
        System.out.println("Waiting for new players ...");
        while (true) {                        
            Socket client = this.server.accept();
            Thread t = new Thread(new Handler(client));
            t.start();
        }        
    }
    public InetAddress getSocketAddress() {
        return this.server.getInetAddress();
    }
    
    public int getPort() {
        return this.server.getLocalPort();
    }
    
}

/**
 * Implements a runnable handler to each user.
 * @author juniocezar
 */
class Handler implements Runnable {
    Socket client;
    String clientID;
    Server s;
    
    public Handler(Socket c) {
        client = c;
    }
    
    public void cmdMove(Move direction) {
        Status ret;
        switch(direction) {
            case UP:
                ret = s.updatePosition(clientID, 0, 1);
                if (ret == Status.FAILED)
                    cmdMove(Move.valueOf("DOWN"));
                break;
            case DOWN:
                ret = s.updatePosition(clientID, 0, -1);
                if (ret == Status.FAILED)
                    cmdMove(Move.valueOf("RIGHT"));
                break;
            case RIGHT:
                ret = s.updatePosition(clientID, 1, 0);
                if (ret == Status.FAILED)
                    cmdMove(Move.valueOf("LEFT"));
                break;
            case LEFT:
                ret = s.updatePosition(clientID, -1, 0);
                if (ret == Status.FAILED)
                    cmdMove(Move.valueOf("UR"));
                break;
            case UR:
                ret = s.updatePosition(clientID, 1, 1);
                if (ret == Status.FAILED)
                    cmdMove(Move.valueOf("UL"));
                break;
            case UL:
                ret = s.updatePosition(clientID, -1, 1);
                if (ret == Status.FAILED)
                    cmdMove(Move.valueOf("DR"));
                break;
            case DR:
                ret = s.updatePosition(clientID, 1, -1);
                if (ret == Status.FAILED)
                    cmdMove(Move.valueOf("DL"));
                break;
            case DL:
                ret = s.updatePosition(clientID, -1, -1);
                if (ret == Status.FAILED)
                    cmdMove(Move.valueOf("UP"));
                break;
            default:
                break;
        }
    }
    
    private void cmdMove(String a) {
        cmdMove(Move.valueOf(a));
    }
        
    /**
     * Handles clients commands, like movements and attacks.
     * @param command: Command sent by client.
     */
    public void handleCommand(String[] command) {
        String op = command[0];
        switch(Command.valueOf(op)) {
            case MOVE:
                cmdMove(command[1]);
                break;
            case SHOW:
                System.out.println(s.toString());
                System.exit(0);
                break;
            default:
                break;
        }
    }
    
    
    /**
     * Handle client connection while buffer is being used. Receives and 
     * re-passes commands from players.
     */
    public void run() {
        String data = null;
        String clientAddress = client.getInetAddress().getHostAddress();
        String clientPort = ((Integer)client.getPort()).toString();
        clientID = clientAddress + clientPort;        
        s = Server.getServerRef();
        
        if(s.randomPosition(clientID) == Status.FAILED) {
            // not using this thread.
            return;
        }
        
        System.out.println("Client <" + clientID + "> logged in!");
        
        // receives messages from the player with commands to be executed on the
        // server, like movements or attacks
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream()));
            while ( (data = in.readLine()) != null ) {
                String[] command = data.split(" ");
                System.out.println("\r\nMessage from " + clientPort + ": " + data + " -- " + command);
                handleCommand(command);
            }
        } catch (IOException ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}