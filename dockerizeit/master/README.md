# Jenkins master setup explained

Jenkins configured on startup using Groovy scripts from the dockerizeit/master directory. All those scripts read configuration parameters from [dockerizeit/master/jenkins.properties](dockerizeit/master/jenkins.properties) file. So if you would like to adjust your instance parameters then only change them in jenkins.properties - no Groovy hacking needed. If you are missing something, then you are very welcome to contribute support for whatever you need.

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

### Artifactory

Configured by the [artifactory.groovy](dockerizeit/master/artifactory.groovy)
The script will read the following section from the [jenkins.properties](dockerizeit/master/jenkins.properties) and create corresponding data structure

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

### Credentials

Credentials creared by the [credentials.groovy](dockerizeit/master/credentials.groovy)
The script will read the following section from the [jenkins.properties](dockerizeit/master/jenkins.properties) and create corresponding data structures

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
    // Store passoword in the file and specify the path. Suggestion is to use .password inside .ssh directory.
    // If this level of security is enough for ssh keys then it will do for the password.
    // Later on we might implement HashiCorp Vault support
    path = "/var/jenkins_home/.ssh/.password"
  }
}
```

### Add more seed jobs

TBD

### Proxy

Configured by the [proxy.groovy](dockerizeit/master/proxy.groovy)
The script will read http_proxy, https_proxy, no_proxy environment variables (set during the container build. See [Dockerfile](dockerizeit/master/Dockerfile) for more details) and define then as Jenkins global variables. Also, it will set GRADLE_OPTS environment variable to something like

```
GRADLE_OPTS="-Dhttp.nonProxyHosts=... -Dhttp.proxyHost=... -Dhttp.proxyPort=... -Dhttps.proxyHost=... -Dhttps.proxyPort=..."
```

If proxy environment variables are set to empty string, then GRADLE_OPTS won't be set

### LDAP

TBD

### Matrix-based security

TBD
