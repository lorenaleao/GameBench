compile:
	mkdir -p build
	javac -d build benchmarkgame/gameutils/*java 
	javac -d build benchmarkgame/*java 
server:
	java -cp build benchmarkgame.Server 127.0.0.1
client:
	echo "Insira a porta do servidor: "; \
	read port; \
	java -cp build benchmarkgame.SingleClient 127.0.0.1 \${port} & 
driver:
	echo "Insira a porta do servidor: "; \
	read port; \
	java -cp build benchmarkgame.Driver 20 5 127.0.0.1 38687
