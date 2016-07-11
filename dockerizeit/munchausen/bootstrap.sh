#!/bin/sh

set -ex

echo "Sleep 15 sec to let pipeline to finish"
sleep 15

echo "Provided docker-compose.yml:"
cat docker-compose.yml

echo "Stop master and slave"
docker-compose -p dockerizeit stop jmaster jslave

echo "Remove all stopped containers"
docker-compose -p dockerizeit rm -fva

echo "Start new master and slave container using provided docker-compose.yml"
docker-compose -p dockerizeit up -d
