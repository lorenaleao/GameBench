#!/bin/bash

for i in $(seq 1 70); do
  java -cp . benchmarkgame.Client 127.0.0.1 $1 &
  sleep 0.2
done
