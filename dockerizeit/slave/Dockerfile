FROM java:openjdk-8u77-alpine

ENV JENKINS_SWARM_VERSION 2.0
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk

# Add support for proxies.
# Values should be passed as build args
# http://docs.docker.com/engine/reference/builder/#arg
ENV http_proxy ${http_proxy:-}
ENV https_proxy ${https_proxy:-}
ENV no_proxy ${no_proxy:-}

# install docker
RUN apk update && apk add curl && curl -o docker-1.12.1.tgz https://get.docker.com/builds/Linux/x86_64/docker-1.12.1.tgz && tar zxvf docker-1.12.1.tgz && mv docker/* /usr/bin/ && rm -rf docker-*
# install git, curl, ssh, bash
RUN apk update && apk add git curl openssh bash

# install docker compose and other python deps
RUN apk update && apk add py-pip perl && pip install docker-compose==1.8.0 docopt==0.6.2 pyyaml==3.11

RUN curl --create-dirs -sSLo /usr/share/jenkins/swarm-client-$JENKINS_SWARM_VERSION-jar-with-dependencies.jar http://repo.jenkins-ci.org/releases/org/jenkins-ci/plugins/swarm-client/$JENKINS_SWARM_VERSION/swarm-client-$JENKINS_SWARM_VERSION-jar-with-dependencies.jar \
  && chmod 755 /usr/share/jenkins

#Install tool to do clean up of all unused docker layers and images
RUN curl -s https://raw.githubusercontent.com/Praqma/docker-clean/master/docker-clean | tee /usr/local/bin/docker-clean > /dev/null && chmod +x /usr/local/bin/docker-clean

COPY start.sh /usr/local/bin/start.sh
RUN chmod 755 /usr/local/bin/start.sh

ENTRYPOINT ["/usr/local/bin/start.sh"]