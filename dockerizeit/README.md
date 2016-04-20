#Jenkins in Docker

### Running

Simply build and kick off setup using docker-compose

```
docker-compose up -d
```

Make sure that user you use to run master container has ~/.ssh directory with the relevant ssh key. Jenkins will automatically create credentials called jenkins using key from that directory. You will need to create jenkins user in your Git hosting system

### Configuration

Jenkins configured on startup using groovy scripts from the master directory

* artifactory.groovy. TBD
* credentials.groovy. TBD
* globalconfig.groovy. TBD
* initialjobs.groovy. TBD
* secutiryLDAP.groovy. TBD

### Upgrading plugins, removing/installing plugins

Plugins configuration managed through the master/plugins.txt. To update its content first go to Manage Jenkins -> Manage Plugins and install necessary upldates, uninstall plugins and etc.
When ready run the following in Manage Jenkins -> Script console and then copy output to plugins.txt

```
plugins = [:]
jenkins.model.Jenkins.instance.getPluginManager().getPlugins().each {plugins << ["${it.getShortName()}":"${it.getVersion()}"]}
plugins.sort().each() { println "${it.key}:${it.value}"}
```
