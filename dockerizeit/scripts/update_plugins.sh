#!/usr/bin/env bash

set -e

PLUGINS_TXT=$(git rev-parse --show-toplevel)/dockerizeit/master/plugins.txt
docker exec dockerizeit_jmaster_1 java -jar /var/jenkins_home/war/WEB-INF/jenkins-cli.jar -remoting -s http://localhost:8080/ groovy /var/jenkins_home/get_plugins.groovy > ${PLUGINS_TXT}

echo "${PLUGINS_TXT} updated. Diff:"

git diff ${PLUGINS_TXT}
