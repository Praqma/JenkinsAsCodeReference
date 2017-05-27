import java.lang.System
import java.io.File
import hudson.model.*
import jenkins.model.*

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())

properties.gerrit.each() { configName, serverConfig ->
  if (serverConfig.enabled) {
    println "--> Configure Gerrit Server: ${serverConfig.hostName}"

    def gerritServer = new com.sonyericsson.hudson.plugins.gerrit.trigger.GerritServer(serverConfig.hostName)
    def config = new com.sonyericsson.hudson.plugins.gerrit.trigger.config.Config()
    config.setGerritHostName(serverConfig.hostName)
    config.setGerritSshPort(serverConfig.sshPort)
    config.setGerritFrontEndURL(serverConfig.frontendURL)
    config.setGerritProxy(serverConfig.proxy)
    config.setGerritUserName(serverConfig.userName)
    config.setGerritAuthKeyFile(new File(serverConfig.sshKeyFile))
    config.setGerritEMail(serverConfig.email)
    gerritServer.setConfig(config)

    com.sonyericsson.hudson.plugins.gerrit.trigger.PluginImpl.getInstance().addServer(gerritServer)
    gerritServer.start()
  }
}
