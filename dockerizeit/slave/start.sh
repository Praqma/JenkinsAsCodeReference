#!/usr/bin/env bash

# if `docker run` first argument start with `-` the user is passing jenkins swarm launcher arguments
if [[ $# -lt 1 ]] || [[ "$1" == "-"* ]]; then

  # jenkins swarm slave
  JAR=`ls -1 /usr/share/jenkins/swarm-client-*.jar | tail -n 1`
  
  PARAMS="-master http://jmaster:8080"

  # Set default number of executors (2 by default)
  PARAMS="$PARAMS -executors ${NUM_OF_EXECUTORS:-2}"

  # Set labels to slave
  PARAMS="$PARAMS -labels \"linux\" -labels \"alpine\" -labels \"3.3\" -labels \"java\" -labels \"docker\" -labels \"swarm\" -labels \"utility-slave\""

  echo Running java $JAVA_OPTS -jar $JAR -fsroot $HOME/slave $PARAMS "$@"
  exec java $JAVA_OPTS -jar $JAR -fsroot $HOME $PARAMS "$@"
fi

# As argument is not jenkins, assume user want to run his own process, for sample a `bash` shell to explore this image
exec "$@"
