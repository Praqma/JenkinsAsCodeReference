FROM jenkins:1.642.4

# Add proxy env
ENV HTTP_PROXY=$http_proxy
ENV HTTPS_PROXY=$https_proxy
ENV NO_PROXY=$no_proxy

# Install plugins
COPY plugins.txt /usr/share/jenkins/plugins.txt
RUN /usr/local/bin/plugins.sh /usr/share/jenkins/plugins.txt

# Copy configuration scripts that will be executed by groovy
COPY *.groovy /usr/share/jenkins/ref/init.groovy.d/
COPY *.properties $JENKINS_HOME/
