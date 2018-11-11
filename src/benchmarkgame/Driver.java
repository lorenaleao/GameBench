package benchmarkgame;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Vector;

public class Driver {
	public static void main (String[] args) throws Exception{

		if(args.length < 1 ){
			System.err.println("Hey, you must set:\n\n\t* the number of players, \n\t* the number of movements per player,\n\t* the IP address and\n\t* the port number for the server!");
            System.err.println("\nSyntax example: java Driver 10000000 5000 208.80.152.130 4444");
			System.exit(1);
		}

		
		int numPlayers = Integer.parseInt(args[0]); //Number of players may be connected to the server.
		
		final int movesPerPlayer = Integer.parseInt(args[1]); //Number of moves per player. 

		final Clients[] clients = new Clients[numPlayers]; 	// Array of players in the game.

		Vector<Thread> threads = new Vector<Thread>(); //Threads vector, each thread will be associated with a single player.
		
		//establishing the connection between players and the server
		for(int i=0; i<numPlayers; i++){
			try{
				clients[i] = new Clients(
				    InetAddress.getByName(args[2]), 
				    Integer.parseInt(args[3]));
			} catch (NumberFormatException nfe) {
				System.err.println("The server's IP address and port number must be supplied correctly");
				System.err.println("Syntax example: 208.80.152.130 34727");
			}	
			System.out.println("\r\nConnected to Server: " + clients[i].socket.getInetAddress());
		}
		
		//creating threads for each player/client 
		for(int i=0; i<numPlayers; i++){
			final int index = i;
			threads.add(new Thread() {
				public void run(){
					try {
						clients[index].startToMove(movesPerPlayer);
					} catch (IOException e) {
						System.err.println("IO exception!");
					} catch (InterruptedException e) {
						System.err.println("Interrupt exception!");
					}
				}
			});
		}
        
        //initializing the created threads
        for (Thread t : threads) {
            t.start();
		}

		
		//waiting for all threads to finish running
		try{
			for(Thread t : threads){
				t.join();
			}

		} catch(InterruptedException ie) {
			System.err.println("Interrupt exception!");
		}
        
        return;
	}
}