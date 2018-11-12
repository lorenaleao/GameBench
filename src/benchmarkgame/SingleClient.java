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
public class SingleClient {
    private Socket socket;
    private Scanner scanner;
    private static Random random = new Random();
    
    private SingleClient(InetAddress serverAddress, int serverPort) throws Exception {
        this.socket = new Socket(serverAddress, serverPort);
        this.scanner = new Scanner(System.in);
    }
    
    public static <T extends Enum<?>> T randomEnum(Class<T> clazz){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
    
    private void startToMove() throws IOException, InterruptedException {        
        while (true) {
            Move m = randomEnum(Move.class);
            String send = "MOVE " + m;
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            out.println(send);
            out.flush();
            Thread.sleep(1000);
        }
    }
    private void sendShow() throws IOException {
        String send = "SHOW";
        PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
        out.println(send);
        out.flush();
    }
    
    public static void main(String[] args) throws Exception {
        SingleClient client = new SingleClient(
                InetAddress.getByName(args[0]), //IP address
                Integer.parseInt(args[1]));     //port number
        
        System.out.println("\r\nConnected to Server: " + client.socket.getInetAddress());
        
        if(args.length > 2)
            client.sendShow();           
        else
            client.startToMove();                
    }
}