compile:
	javac benchmarkgame/*java
server:
	java -cp . benchmarkgame.Server 127.0.0.1
client:
	echo "Insira a porta do servidor: "
	read port
	java -cp . benchmarkgame.Client 127.0.0.1 \${port} & 
