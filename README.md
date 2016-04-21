# JenkinsAsCodeReference

### Description
This repository is intended for the reference Jenkins configuration as code as well as JobDSL library

### Getting started
#### Requirements
* Create backup directory to store jobs history and generate ssh keys. 
If you take a look at the docker-compose.yml file at the dockerizeit/ directory, you will see there are two directories mount from docker host to containers. They are $HOME/.ssh and $HOME/jenkins-backup/jobs
For running up Docker containers, you will need to mount the directories from your host. $HOME/.ssh mounts because of authorization between Jenkins and Git repo goes by ssh keys. 
You need to generate ssh keys for a user who will have access to Jenkins and Git repositories if it has not done before.
$HOME/jenkins-backup/jobs is the directory there all jobs build history will be stored. For instance, if you reboot Jenkins master container all jobs history will be available after the restart. Make sure that backup directory exists before start up and owned by user 1000. Jenkins master container is running by user jenkins, group 1000.

### Detailed description
TBD

### Roadmap and contributions
TBD

### Project progress
You can see project status on [its Waffle board](https://waffle.io/Praqma/JenkinsAsCodeReference)
