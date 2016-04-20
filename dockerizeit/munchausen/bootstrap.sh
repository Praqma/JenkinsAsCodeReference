#!/usr/bin/env bash

set -ex

# This variable is part of docker_compose.yml to specify what version of the image to start
export VERSION=$1

echo "Sleep 15 sec to let pipeline to finish"
sleep 15

echo "Read info about current mounts to recreate them"
# Expected output from docker inspect is something like this [/Users/andrey9kin/jenkins-backup/jobs:/var/jenkins_home/jobs:rw /Users/andrey9kin/.ssh:/var/jenkins_home/.ssh:rw]
# then we cut off brackets using tr, split string in two using awk and then print it in src:dst format to cut off :rw part
# Those env variables used in docker_compose.yml to define correct mounts
export BACKUP_VOLUME=$(docker inspect --format '{{ .HostConfig.Binds }}' dockerizeit_jmaster_1 | tr -d [ | tr -d ] | awk '{print $1}' | awk -F : '{print $1":"$2}')
export SSH_VOLUME=$(docker inspect --format '{{ .HostConfig.Binds }}' dockerizeit_jmaster_1 | tr -d [ | tr -d ] | awk '{print $2}' | awk -F : '{print $1":"$2}')
export GRADLE_VOLUME=$(docker inspect --format '{{ .HostConfig.Binds }}' dockerizeit_jslave_1 | tr -d [ | tr -d ] | awk '{print $3}' | awk -F : '{print $1":"$2}')
# Sanity check
if [[ $BACKUP_VOLUME != *":/var/jenkins_home/jobs" ]]
then
  echo "Something was changed in the jmaster config and that change wasn't reflected to muchhausen!"
  echo "BACKUP_VOLUME is $BACKUP_VOLUME, expected *:/var/jenkins_home/jobs"
  exit 1
fi
if [[ $SSH_VOLUME != *":/var/jenkins_home/.ssh" ]]
then
  echo "Something was changed in the jmaster config and that change wasn't reflected to muchhausen!";
  echo "SSH_VOLUME is $SSH_VOLUME, expected *:/var/jenkins_home/.ssh"
  exit 1
fi
if [[ $GRADLE_VOLUME != *":/root/.gradle" ]]
then
  echo "Something was changed in the jmaster config and that change wasn't reflected to muchhausen!";
  echo "GRADLE_VOLUME is $GRADLE_VOLUME, expected *:/root/.gradle"
  exit 1
fi

echo "Stop master and slave"
docker stop dockerizeit_jmaster_1 dockerizeit_jslave_1

echo "Start new master and slave container using images with version $VERSION"
docker-compose -p dockerizeit up -d
