package benchmarkgame;

public class Driver{
	public stattic void main(String[] args){

		if(args.length < 1 ){
			System.err.println("Hey, you must set the number of players and movements!");
            System.err.println("Syntax: java benchmarkgame 10000000 5000");
			System.exit(1);
		}

		int numPlayers = Integer.parseInt(args[0]);
		int movesPerPlayer = Integer.parseInt(args[1]);


		Clients[] clients = new Clients[numPlayers];
		Vector<Thread> threads = new Vector<Thread>();
		
		for(int i=0; i<numPlayers; i++){
			clients[i] = new Clients(
                InetAddress.getByName(args[2]), 
                Integer.parseInt(args[3]));	
			System.out.println("\r\nConnected to Server: " + client.socket.getInetAddress());
		}
		
		for(int i=0; i<numPlayers; i++){
			threads.add(new Thread() {
				public void run(){
					clients[i].start(movesPerPlayer);
				}
			});
		}
        
        for (Thread t : threads) {
            t.start();
		}

		try{
			for(Thread t : threads){
				t.join();
			}

		} catch(InterruptedException ie) {
			System.err.println("Interrupt exception!");
		}
        
        
	}
}