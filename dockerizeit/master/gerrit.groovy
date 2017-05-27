import java.lang.System
import java.io.File
import hudson.model.*
import jenkins.model.*

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())

properties.gerrit.each() { configName, serverConfig ->
  if (serverConfig.enabled) {
    println "--> Configure Gerrit Server: ${serverConfig.hostName}"

    // Dynamically load classes to avoid dependency to Gerrit Trigger plugin
    try {
      def GerritServer = Class.forName("com.sonyericsson.hudson.plugins.gerrit.trigger.GerritServer")
      def Config = Class.forName("com.sonyericsson.hudson.plugins.gerrit.trigger.config.Config")
      def PluginImpl = Class.forName("com.sonyericsson.hudson.plugins.gerrit.trigger.PluginImpl")
    } catch (ClassNotFoundException ex) {
      println "ERROR: Can not configure Gerrit Trigger no plugin installed"
      return
    }

    def gerritServer = GerritServer.newInstance(serverConfig.hostName)
    def config = Config.newInstance()
    config.setGerritHostName(serverConfig.hostName)
    config.setGerritSshPort(serverConfig.sshPort)
    config.setGerritFrontEndURL(serverConfig.frontendURL)
    config.setGerritProxy(serverConfig.proxy)
    config.setGerritUserName(serverConfig.userName)
    config.setGerritAuthKeyFile(new File(serverConfig.sshKeyFile))
    config.setGerritEMail(serverConfig.email)
    gerritServer.setConfig(config)

    PluginImpl.getInstance().addServer(gerritServer)
    gerritServer.start()
  }
}
