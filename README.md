# Jenkins Infrastructure as Code template

### Description
The intention of this project is to create the easily configurable template, summarize the current best thinking and create unification for the stateless Jenkins deployments.

### Getting started

#### Requirements

* Linux host
* Docker 1.11 
* Docker Compose 1.8
* Make sure that you are using umask 022 or similar since during the build process configuration files will be copied to the Jenkins container as a root user but Jenkins runs by another user, so we need to make sure that those files are readable for group and others.

#### Preparations

* Clone this repository

```
git clone <<change>>
```

* Create backup directories - they will be used to store build history, user content, Gradle cache and Docker images from the local registry. Also we will use jenkins-backup/workspace directory for mapping to /root/workspace inside slave docker container - it needs to be done for working docker pipeline plugin properly. See [JENKINS-35217](https://issues.jenkins-ci.org/browse/JENKINS-35217) for details. You can find the list of all volumes used by this setup inside [dockerizeit/docker-compose.yml](dockerizeit/docker-compose.yml)

```
mkdir -p $HOME/jenkins-backup/jobs
mkdir -p $HOME/jenkins-backup/userContent
mkdir -p $HOME/jenkins-backup/slave/gradle
mkdir -p $HOME/jenkins-backup/registry
mkdir -p $HOME/jenkins-backup/workspace
chmod -R 777 $HOME/jenkins-backup
```

* Make sure that you have `$HOME/.ssh` directory with the ssh keys for the user that can access GitHub or your own Git hosting. Docker compose will mount `$HOME/.ssh` to the Jenkins master container so it can create default credentials from it.

#### First start
Step into the dockerizeit directory and run docker compose. Important! If you run docker compose from the different directory then make sure to use -p dockerizeit option for the docker compose. There are scripts that rely on  services to be called dockerizeit_jmaster_1 and etc.

```
cd dockerizeit
docker-compose up -d --build
```

#### Restart/Start

Download docker-compose.yml attached to the latest deployment pipeline execution and run it using docker-compose

```
wget <docker compose file url>
docker-compose -p dockerizeit up -d
```

or pick it from the backup directory

```
docker-compose -f $HOME/jenkins-backup/jobs/jenkins_as_a_code-pipeline/builds/lastSuccessfulBuild/archive/docker-compose.yml \
-p dockerizeit \
up -d
```

### Configuration

Find detailed description of configuration scripts and configuration file [here](dockerizeit/master/README.md)

### Roadmap and contributions

