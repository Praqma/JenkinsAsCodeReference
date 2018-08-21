[![Build Status](https://api.travis-ci.org/Praqma/JenkinsAsCodeReference.svg?branch=master)](https://travis-ci.org/Praqma/JenkinsAsCodeReference)
---
maintainer: ewelinawilkosz
---

# Jenkins as Code template

### Description
The intention of this project is to create the easily configurable template, summarize the current best thinking and create unification for the stateless Jenkins deployments.

### Getting started

#### Recommended setup

* Linux host that supports Docker
* Docker 17.05.0-ce (minimal tested version is 1.11.0)
* Docker Compose 1.13.0 (minimal tested version is 1.7.0)
* Make sure that you are using umask 022 or similar since during the build process configuration files will be copied to the Jenkins container as a root user but Jenkins runs by another user, so we need to make sure that those files are readable for group and others.

#### Preparations

* Clone this repository

```
git clone https://github.com/Praqma/JenkinsAsCodeReference.git
```

* Set proxy variables. If you are still using docker-compose 1.7.0 then you have to do it even if you are not using a proxy because there is [a bug in docker-compose](https://github.com/docker/compose/issues/3281) which makes args return None instead of empty string. Because of that, if you don't use proxy, you have to define empty environment variables http_proxy, https_proxy, no_proxy. Otherwise you would have those variables set to point out your proxy settings

```
export http_proxy=<empty or proxy address>
export https_proxy=<empty or proxy address>
export no_proxy=<empty or proxy address>
export JAVA_PROXY=<empty or -Dhttps.proxyHost=<proxy address> -Dhttps.proxyPort=<proxy port> -Dhttp.nonProxyHosts=\"localhost,127.0.0.1|*.whatever.com\" -Dhttp.proxyHost=<proxy address> -Dhttp.proxyPort=<proxy port>
```

or

```
cat > ~/.bashrc <<- EOM
export http_proxy=<empty or proxy address>
export https_proxy=<empty or proxy address>
export no_proxy=<empty or proxy address>
export JAVA_PROXY=<empty or -Dhttps.proxyHost=<proxy address> -Dhttps.proxyPort=<proxy port> -Dhttp.nonProxyHosts=\"localhost,127.0.0.1|*.whatever.com\" -Dhttp.proxyHost=<proxy address> -Dhttp.proxyPort=<proxy port>
EOM
source ~/.bashrc
```
Important! We are using Alpine Linux and apk (package manager) requires proxy address to include schema, i.e. http_proxy=http://my.proxy.com not just http_proxy=my.proxy.com. This only affects http_proxy, https_proxy variables. More details [here](https://github.com/gliderlabs/docker-alpine/issues/171)

* Create backup directories - they will be used to store build history, user content, Gradle cache and Docker images from the local registry. Also we will use jenkins-backup/workspace directory for mapping to /root/workspace inside slave docker container - it needs to be done for working docker pipeline plugin properly. See [JENKINS-35217](https://issues.jenkins-ci.org/browse/JENKINS-35217) for details. You can find the list of all volumes used by this setup inside [dockerizeit/docker-compose.yml](dockerizeit/docker-compose.yml)

```
mkdir -p $HOME/jenkins-backup/jobs
mkdir -p $HOME/jenkins-backup/userContent
mkdir -p $HOME/jenkins-backup/slave/gradle
mkdir -p $HOME/jenkins-backup/registry
mkdir -p $HOME/jenkins-backup/workspace
# We are running Jenkins as user id 1000 so let him own backup directory to avoid conflicts
chown -R 1000:1000 $HOME/jenkins-backup
```

* Make sure that you have `$HOME/.ssh` directory with the ssh keys for the user that can access GitHub or your own Git hosting. Docker compose will mount `$HOME/.ssh` to the Jenkins master container so it can create default credentials from it. If you don't want that to happen then remove `credentials` closures that takes info from `/var/jenkins_home/.ssh` from [jenkins.properties](https://github.com/Praqma/JenkinsAsCodeReference/blob/master/dockerizeit/master/jenkins.properties) file.

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

#### Workflow
Issues labeling follows Pragmatic workflow described [here](http://www.praqma.com/stories/a-pragmatic-workflow/)
Describe your idea as ticket, make sure to put `Action - needs grooming` label and let's discuss it together

#### Contributions verification
We do have [Travis CI job](https://travis-ci.org/Praqma/JenkinsAsCodeReference) running for all branches so make sure it goes green for all your contributions.
You can also use review job created on the startup. This job relies on principals described in [this article](http://www.josra.org/blog/An-automated-git-branching-strategy.html)

#### Project progress
You can see project status on [its Waffle board](https://waffle.io/Praqma/JenkinsAsCodeReference).
At some point of time, we will kick off maintainers meetings. Stay tuned
