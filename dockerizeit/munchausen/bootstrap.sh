#!/usr/bin/env sh

export VERSION=$1

echo "Sleep 15 sec to let pipeline to finish"
sleep 15

echo "Kill master"
docker rm -f dockerizeit_jmaster_1 dockerizeit_jslave1

echo "Start new master container"
docker-compose -p dockerizeit up -d
