FROM java:8-jdk

ENV JENKINS_SWARM_VERSION 2.0
ENV HOME /home/jenkins-slave
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
RUN useradd -c "Jenkins Slave user" -d $HOME -m jenkins-slave
# install netstat to allow connection health check with
RUN apt-get update && apt-get install -y net-tools && rm -rf /var/lib/apt/lists/*
# Install tools
RUN apt-get update
RUN apt-get install -y maven
RUN apt-get install -y git
RUN curl -sL https://deb.nodesource.com/setup_0.10 | bash -
RUN apt-get install -y nodejs
RUN apt-get install -y sudo

RUN curl --create-dirs -sSLo /usr/share/jenkins/swarm-client-$JENKINS_SWARM_VERSION-jar-with-dependencies.jar http://maven.jenkins-ci.org/content/repositories/releases/org/jenkins-ci/plugins/swarm-client/$JENKINS_SWARM_VERSION/swarm-client-$JENKINS_SWARM_VERSION-jar-with-dependencies.jar \
  && chmod 755 /usr/share/jenkins
RUN sudo usermod -a -G sudo jenkins-slave

COPY start.sh /usr/local/bin/start.sh
RUN chown jenkins-slave /usr/local/bin/start.sh
RUN chmod 755 /usr/local/bin/start.sh

USER jenkins-slave
VOLUME /home/jenkins-slave

ENTRYPOINT ["/usr/local/bin/start.sh"]