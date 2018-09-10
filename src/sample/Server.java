package sample;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Implements the game server
 * @author juniocezar
 */
public class Server {
    private ServerSocket server;
    private int threadLimit = 16;
    
    
    /**
     * Initializes server, opening socket on input IP address.
     * @param ipAddress: IP address to be used be the server.
     * @throws Exception 
     */
    public Server(String ipAddress) throws Exception {
        if (ipAddress != null && !ipAddress.isEmpty()) 
          this.server = new ServerSocket(0, 1, InetAddress.getByName(ipAddress));
        else 
          this.server = new ServerSocket(0, 1, InetAddress.getLocalHost());
    }
    
    /**
     * Listen for client connections, launching threads for each new connection
     * @throws Exception 
     */
    private void listen() throws Exception {
        while (true) {            
            System.out.println("Waiting new connection");
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
    public static void main(String[] args) throws Exception {
        Server app = new Server(args[0]);
        System.out.println("\r\nRunning Server: " + 
                "Host=" + app.getSocketAddress().getHostAddress() + 
                " Port=" + app.getPort());
        
        app.listen();
    }
}


class Handler implements Runnable {
    Socket client;
    
    public Handler(Socket c) {
        client = c;
    }
        
    public void run() {
        String data = null;
        String clientAddress = client.getInetAddress().getHostAddress();
        String clientPort = ((Integer)client.getPort()).toString();
        System.out.println("\r\nNew connection from " + clientAddress + ":" + clientPort);

        BufferedReader in = null;
        try {        
            in = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));
            while ( (data = in.readLine()) != null ) {
            System.out.println("\r\nMessage from " + clientAddress + ": " + data);
        }
        } catch (IOException ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}