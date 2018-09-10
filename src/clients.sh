#!/bin/bash

for i in $(seq 1 70); do
  java -cp . benchmarkgame.Client 150.164.10.137 $1 &
  sleep 0.2
done
