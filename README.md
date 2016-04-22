# JenkinsAsCodeReference

### Description
This repository is intended for the reference Jenkins configuration as code as well as JobDSL library

### Getting started
#### Requirements
* It is important to have similar versions of Docker on your host and in containers. Running Docker containers will have 1.11.0 version of Docker. Check the version of Docker on your host by "docker version".
If you have the different versions of docker, do upgrade, or install 1.11.0 version on the host. Otherwise, if the versions are not the same you will get: "Error response from daemon: client and server don't have the same version".
If it is tricky to upgrade/downgrade docker host, since Docker 1.10.0, there's an option for overriding the API Version used for Docker client communication with Docker engine. Just by using the DOCKER_API_VERSION environment variable. Set DOCKER_API_VERSION be the same in client and server, and they will communicate. For example: DOCKER_API_VERSION=1.23


### Detailed description
TBD

### Roadmap and contributions
TBD

### Project progress
You can see project status on [its Waffle board](https://waffle.io/Praqma/JenkinsAsCodeReference)
