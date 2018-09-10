#!/bin/bash

java -cp . benchmarkgame.Client 127.0.0.1 $1 &
sleep 1 && java -cp . benchmarkgame.Client 127.0.0.1 $1 &
sleep 10 && java -cp . benchmarkgame.Client 127.0.0.1 $1 &
sleep 2 && java -cp . benchmarkgame.Client 127.0.0.1 $1 &
sleep 5 && java -cp . benchmarkgame.Client 127.0.0.1 $1 &
sleep 8 && java -cp . benchmarkgame.Client 127.0.0.1 $1 &
sleep 0.1 && java -cp . benchmarkgame.Client 127.0.0.1 $1 &
sleep 4 && java -cp . benchmarkgame.Client 127.0.0.1 $1 &
