# Jenkins master setup explained

Jenkins configured on startup using Groovy scripts from the dockerizeit/master directory. All scripts read configuration parameters from properties files. So if you would like to adjust your instance parameters then only change them in the corresponding properties file - no Groovy hacking needed. If you are missing something, then you are very welcome to contribute support for whatever you need.

## Dockerfile

TBD

## Upgrading plugins, removing/installing plugins

Plugins configuration managed through the master/plugins.txt. To update its content first go to Manage Jenkins -> Manage Plugins and install necessary updates, uninstall plugins and etc.
When ready run the following in Manage Jenkins -> Script console and then copy output to plugins.txt

```
plugins = [:]
jenkins.model.Jenkins.instance.getPluginManager().getPlugins().each {plugins << ["${it.getShortName()}":"${it.getVersion()}"]}
plugins.sort().each() { println "${it.key}:${it.value}"}
```
### Installation of custom plugin
If you have a custom .hpi / .jpi plugin that the master needs to run with copy the file into the
```dockerizeit/master/``` and add the following line to the end of ```dockerizeit/master/Dockerfile```:
 
```
COPY *.hpi /usr/share/jenkins/ref/plugins/
```

or if you plugin is available in Nexus/Artifactory/JitPack then you could download it to corresponding directory.
See example below

```
RUN wget https://jitpack.io/com/github/praqma/tracey-jenkins-trigger-plugin/1.0.0/tracey-jenkins-trigger-plugin-1.0.0.hpi \
         -O /usr/share/jenkins/ref/plugins/tracey.hpi
```

## Configuration through the Groovy files

### Credentials

Credentials created by the [credentials.groovy](credentials.groovy)
The script will read the following section from the [jenkins.properties](jenkins.properties) and create corresponding data structures

```
credentials {
  // Default credentials used to download Jenkins repo
  base {
    type = "ssh"
    userId = "jenkins"
    credentialsId = "jenkins"
    description = ""
    passphrase = ""
    path = "/var/jenknis_home/.ssh/id_rsa" // by default docker-compose.yml mounts ~/.ssh to /var/jenkins_home/.ssh Make sure it contains correct ssh key
  }
}
```

It is possible to create password-based credentials

```
credentials {
  base {
    type = "ssh"
    userId = "jenkins"
    credentialsId = "jenkins"
    description = ""
    passphrase = ""
    path = "/var/jenknis_home/.ssh/id_rsa"
  }
  pass {
    type = "password"
    userId = "test"
    credentialsId = "test"
    description = ""
    // Store password in the file and specify the path. Suggestion is to use .password inside .ssh directory.
    // If this level of security is enough for ssh keys then it will do for the password.
    // Later on we might implement HashiCorp Vault support
    path = "/var/jenkins_home/.ssh/.password"
  }
}
```
### Global section

Some global properties in [jenkins.properties](jenkins.properties) are set through [globalconfig](globalconfig.groovy).

```
global {
  // How many executors you want on master
  numExecutorsOnMaster = 0
  // Which address is used to axxess your Jenkins instance
  // http://jenkins.my.company:8080
  // left empty: http://$ip:8080
  jenkinsRootUrl = ""
  // Specify the administrator email
  / used as return address in email notofications
  jenkinsAdminEmail = "Jenkins <no-reply@yourcompany.com>"
  scmQuietPeriod = 3
  scmCheckoutRetryCount = 3
  git {
    // Jenkins in git
    name = "Jenkins Jenkinsson"
    email = "no-reply@yourcompany.com"
  }
  // Some default variables to use in job configurations.
  variables {
    default_credentials = "${credentials.base.credentialsId}"
    default_repo = "git@github.com:Praqma/JenkinsAsCodeReference.git"
    default_branch = "master"
    utility_slave = "utility-slave"
    master_image_name = "${images.masterImageName}"
    slave_image_name = "${images.slaveImageName}"
  }
}
```

Configuration of [Groovy Shared Library plugin](https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Shared+Groovy+Libraries+Plugin) implemented in [globalPipelineLibraries.groovy](globalPipelineLibraries.groovy).
Use the following block in jenkins.properties to define your libraries

```
libraries{
  library1 {
      enabled = true
      name = "name-of-library"
      version = "master"
      implicitly = true
      allow_overridden = false
      scm_path = "https://github.com/Andrey9kin/shared-groovy-lib-test.git"
      credentialsId = ""
      branch = "master"
  }
}
```

### Slaves

Slaves created by the [slaves.groovy](slaves.groovy)
The script will read [slaves.properties](slaves.properties) and create corresponding slaves.
Consider using [generate_slaves_config.groovy](../scripts/generate_slaves_config.groovy) to generate configuration for your existing slaves.
Simply run the script in Jenkins Script Console (Manage Jenkins -> Script Console). Please note that there are limitations (see comments inside script).

SSH slaves configuration

```
slaves {
  // Long version
  sshTest {
   type = "ssh"
   name = "ssh-test"
   description = ""
   remoteFS = "/var/jenkins/slave"
   executors = "5" // Have to be string because of constructor
   mode = hudson.model.Node.Mode.NORMAL
   labels = "ssh ssh-test"
   retention = hudson.slaves.RetentionStrategy.INSTANCE
   // ssh slave specific values
   host = "localhost"
   port = 22
   credentialsId = "jenkins"
   jvmOptions = ""
   javaPath = ""
   prefixStartSlaveCmd = ""
   suffixStartSlaveCmd = ""
   launchTimeoutSeconds = 5
   maxNumRetries = 3
   retryWaitTime = 3
   env {
     key = "value"
   }
  }
  // short version
  sshTest2 {
   type = "ssh"
   name	= "ssh-test2"
   description = ""
   remoteFS = "/var/jenkins/slave"
   executors = "5"
   mode = hudson.model.Node.Mode.NORMAL
   labels = "ssh ssh-test2"
   retention = hudson.slaves.RetentionStrategy.INSTANCE
   // ssh slave specific values
   host = "localhost"
   credentialsId = "jenkins"
   env {
     key = "value"
   }
  }
}
```

JNLP slaves configuration example

```
slaves {
  jnlpTest {
   type = "jnlp"
   name	= "jnlp-test"
   description = ""
   remoteFS = "/var/jenkins/slave"
   executors = "5"
   mode = hudson.model.Node.Mode.NORMAL
   labels = "jnlp jnlp-test"
   retention = hudson.slaves.RetentionStrategy.INSTANCE
  }
}
```
Going forward we are planning to move slaves definition to the user repository and read them in the same way as we do with the JobDSL files for the seedjobs.

### Seed jobs

Seed jobs created on the startup by the [initialjobs.groovy](initialjobs.groovy)
The script will read the following section from the [jenkins.properties](jenkins.properties) and create corresponding jobs

```
seedjobs {
  jenkins {
    // Name of the job
    name = "jenkins_as_a_code-seedjob"
    // Repo to clone
    repo = "git@github.com:Praqma/JenkinsAsCodeReference.git"
    // Branch to checkout
    branch = "master"
    // Credentials to use when cloning
    credentials = "jenkins"
    // Where to find job dsl specification in the repository
    path = "jobdsl-gradle/src/jobs/groovy/*.groovy"
    // Additional classes if any
    classpath = "jobdsl-gradle/src/main/groovy"
    // Job parameters if needed
    parameters {
    }
  }
}
```

To plugin your repo just add one more section like the one above - script will iterate over all sections, create jobs, and trigger them.
See example below

```
seedjobs {
  jenkins {
    name = "jenkins_as_a_code-seedjob"
    repo = "git@github.com:Praqma/JenkinsAsCodeReference.git"
    branch = "master"
    credentials = "jenkins"
    path = "jobdsl-gradle/src/jobs/groovy/*.groovy"
    classpath = "jobdsl-gradle/src/main/groovy"
    parameters {
    }
  }
  roadshow {
    name = "praqma-training.roadshow.seed"
    repo = "git@github.com:praqma-training/roadshow-dsl.git"
    branch = "master"
    credentials = "jenkins"
    path = "*.groovy"
    classpath = ""
    parameters {
      GITHUB_USER = "praqma-training"
    }
  }
}
```

### Artifactory

Configured by the [artifactory.groovy](artifactory.groovy)
The script will read the following section from the [jenkins.properties](jenkins.properties) and create corresponding data structure.
Current implementation relies on the credentials, i.e. you have to create credentials instead of specifying login and password in Artifactory config.
In this way, we are trying to avoid having plain passwords in the configuration files.
You have a possibility to configure multiple servers by adding more closures.

```
artifactory {
  server1 {
    enabled = true
    deployerCredentialsId = "artifactory"
    resolverCredentialsId = "artifactory"
    overridingCredentials = false
    connectionTimeOut = 300
    serverName = "my-lovely-artifactory"
    serverUrl = "http://1.1.1.1:8081/artifactory"
    bypassProxy = false
  }
}
```

### Proxy

Configured by the [proxy.groovy](proxy.groovy)
The script will read http_proxy, https_proxy, no_proxy, JAVA_OPTS environment variables (set during the container build. See [Dockerfile](Dockerfile) for more details) and define then as Jenkins global variables.

### Security configuration

Security configuration stored in the separate file - [security.properties](security.properies)

#### LDAP

LDAP configuration example. Fields name match [LDAPSecurityRealm](https://github.com/jenkinsci/ldap-plugin/blob/0da2fef8feb9e480e303a7dbd03241f880c82235/src/main/java/hudson/security/LDAPSecurityRealm.java) constructor.

ldap {
  enabled = true
  server = "ldap://1.2.3.4"
  rootDN = "dc=foo,dc=com"
  userSearchBase = "cn=users,cn=accounts"
  userSearch =
  groupSearchBase =
  managerDN = "uid=serviceaccount,cn=users,cn=accounts,dc=foo,dc=com"
  managerPassword = "password"
  inhibitInferRootDN = false
}

#### AD

AD configuration example. Fields name match [ActiveDirectorySecurityRealm](https://github.com/jenkinsci/active-directory-plugin/blob/3862baf8bcd3a88703c9ea98b7b947d9ee34d16d/src/main/java/hudson/plugins/active_directory/ActiveDirectorySecurityRealm.java) constructor.

```
ad {
  enabled = true
  domain = "domain.com"
  site = null
  bindName = null
  bindPassword = null
  server = null
  // See [GroupLookupStrategy.java](https://github.com/jenkinsci/active-directory-plugin/blob/8817f035e15d33e390367aa7c664e0cfbb04d7cf/src/main/java/hudson/plugins/active_directory/GroupLookupStrategy.java) for the list of available options.
  // We can't use direct class resolution like hudson.plugins.active_directory.GroupLookupStrategy.RECURSIVE
  // because GroupLookupStrategy is a private enum
  groupLookupStrategy = RECURSIVE
}
```

#### Jenkins own database

Add the following snippet to [security.properties](security.properties) in order to tell Jenkins to use its own database for the user management.
There is no support for sign up in UI because Jenkins configuration will be wiped out during redeployment, i.e. user database is not preserved.
Preferably avoid this type of security realm and use 3rd party service for that - think LDAP, Unix users database, GitHub, Google etc.
Also, consider a need for those users - do you really need them if you anyway control configuration through the git repo. Well, unless this is a test instance facing public network

```
owndb {
  enabled = true
  users {
    user1 {
      userId = "jenkins"
      path = "/var/jenkins_home/.ssh/.password"
    }
    user2 {
      userId = "hudson"
      path = "/var/jenkins_home/.ssh/.password"
    }
  }
}
```

#### Google Login

[Google Login plugin](https://wiki.jenkins-ci.org/display/JENKINS/Google+Login+Plugin) allows to login using Google domain credentials.
Make sure to create file with the secret before enabling corresponding section

```
googlelogin {
  enabled = false
  clientId = "someId"
  clientSecret = "/var/jenkins_home/.ssh/.googlesecret"
  domain = "domain.com"
}
```

#### Matrix-based security

Add the following snippet to [security.properties](security.properties) to appoint users access rights for matrix-based security model. For example, to allow anonymous users create a slave connection Jenkins master, or give authenticated user administrators rights.

```
matrixbasedsecurity {
  enabled = true
  users {
    anonymous {
      userId = hudson.security.ACL.ANONYMOUS_USERNAME
      permissions = [
                      hudson.model.Computer.CREATE,
                      hudson.model.Computer.CONNECT,
                      hudson.model.Hudson.READ,
                      hudson.model.Job.READ]
    }
    authenticated {
      userId = "authenticated"
      permissions = [
                       hudson.model.Hudson.ADMINISTER]
    }
  }
}
```
