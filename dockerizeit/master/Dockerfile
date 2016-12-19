FROM jenkins:2.7.3

USER jenkins
# Values should be passed as build args
# Add support for proxies.
# http://docs.docker.com/engine/reference/builder/#arg
ENV http_proxy ${http_proxy:-}
ENV https_proxy ${https_proxy:-}
ENV no_proxy ${no_proxy:-}
ARG JAVA_OPTS="-Djenkins.install.runSetupWizard=false, -Djenkins.slaves.JnlpSlaveAgentProtocol3.enabled=true"
ENV JAVA_OPTS ${JAVA_OPTS:-}
# Define build argument and env variable master_image_version
# Use to pass info about Jenkins version so it can be represented
# for the users.
ARG master_image_version="local build"
ENV master_image_version $master_image_version

# Install plugins
COPY plugins.txt /usr/share/jenkins/plugins.txt
#COPY install-plugins.sh /usr/local/bin/install-plugins.sh
#RUN /usr/local/bin/plugins.sh /usr/share/jenkins/plugins.txt
RUN /usr/local/bin/install-plugins.sh $(cat /usr/share/jenkins/plugins.txt | tr '\n' ' ')

# Copy configuration scripts that will be executed by groovy
COPY *.groovy /usr/share/jenkins/ref/init.groovy.d/
COPY basic-security.groovy $JENKINS_HOME/init.groovy.d/basic-security.groovy
COPY *.properties $JENKINS_HOME/

# Indicate not to run wizard
RUN echo 2.7.3 > /usr/share/jenkins/ref/jenkins.install.UpgradeWizard.state

#USER root
#USER jenkins
#RUN ["chown", "jenkins:jenkins", "/var/jenkins_home","-R"]
