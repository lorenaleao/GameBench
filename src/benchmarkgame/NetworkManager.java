/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package benchmarkgame;

import benchmarkgame.gameutils.Command;
import benchmarkgame.gameutils.Move;
import benchmarkgame.gameutils.Status;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Implements a network manager that handles new user requests, as well user 
 * operations.
 * @author juniocezar
 */
public class NetworkManager {
    private ServerSocket server;
    private int threadLimit = 16;
    private Server s;
    
    
    /**
     * Initializes server, opening socket on input IP address.
     * @param ipAddress: IP address to be used by the server.
     * @throws Exception 
     */
    public NetworkManager(Server _s, String ipAddress) throws Exception {
        if (ipAddress != null && !ipAddress.isEmpty()) 
          this.server = new ServerSocket(0, 1, InetAddress.getByName(ipAddress));
        else 
          this.server = new ServerSocket(0, 1, InetAddress.getLocalHost());
        this.s = _s;
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
        this.client = c;
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
     * Handles clients commands like movements and attacks.
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