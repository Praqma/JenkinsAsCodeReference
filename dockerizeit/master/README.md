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

### Slaves

Slaves created by the [credentials.groovy](slaves.groovy)
The script will read [slaves.properties](slaves.properties) and create corresponding slaves.
ssh slaves configuration

```
slaves {
  // Long version
  sshTest {
   type = "ssh"
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
  }
  // short version
  sshTest2 {
   type = "ssh"
   description = ""
   remoteFS = "/var/jenkins/slave"
   executors = "5"
   mode = hudson.model.Node.Mode.NORMAL
   labels = "ssh ssh-test2"
   retention = hudson.slaves.RetentionStrategy.INSTANCE
   // ssh slave specific values
   host = "localhost"
   credentialsId = "jenkins"
  }
}
```

JNLP slaves configuration example

```
slaves {
  jnlpTest {
   type = "jnlp"
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

### Artifactory

Configured by the [artifactory.groovy](artifactory.groovy)
The script will read the following section from the [jenkins.properties](jenkins.properties) and create corresponding data structure

```
artifactory {
  enabled = true \\ Skip configuration if set to false
  logInUser = "admin"
  logInPassw = "password"
  deployerName = "jenkins"
  overridingCredentials = false
  resolverUser =
  resolverPwd =
  resolverName = "resolver"
  connectionTimeOut = 300
  serverName = "do-server"
  serverUrl = "http://188.166.117.16:8081/artifactory"
  bypassProxy = false
}
```

### Proxy

Configured by the [proxy.groovy](proxy.groovy)
The script will read http_proxy, https_proxy, no_proxy environment variables (set during the container build. See [Dockerfile](Dockerfile) for more details) and define then as Jenkins global variables. Also, it will set GRADLE_OPTS environment variable to something like

```
GRADLE_OPTS="-Dhttp.nonProxyHosts=... -Dhttp.proxyHost=... -Dhttp.proxyPort=... -Dhttps.proxyHost=... -Dhttps.proxyPort=..."
```

If proxy environment variables are set to empty string, then GRADLE_OPTS won't be set

### LDAP

TBD

### Matrix-based security

TBD
