#!/bin/bash

for i in $(seq 1 10); do
  java -cp . benchmarkgame.SingleClient 127.0.0.1 $1 &
  sleep 0.2
done
