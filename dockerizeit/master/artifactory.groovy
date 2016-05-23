import java.lang.System
import hudson.model.*
import jenkins.model.*
import org.jfrog.*
import org.jfrog.hudson.*
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())

def getPasswordCredentials(String id) {
  def all = CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class)
  return all.findResult { it.id == id ? it : null }
}

properties.artifactory.each() { configName, serverConfig ->
  if(serverConfig.enabled) {
    println "--> Configure Artifactory: Server ${serverConfig.serverName}"
    def inst = Jenkins.getInstance()
    def desc = inst.getDescriptor("org.jfrog.hudson.ArtifactoryBuilder")
    CredentialsConfig deployerCredentials = new CredentialsConfig(getPasswordCredentials(serverConfig.deployerCredentialsId),
                                                                  serverConfig.deployerCredentialsId,
                                                                  serverConfig.overridingCredentials)
    CredentialsConfig resolverCredentials = new CredentialsConfig(getPasswordCredentials(serverConfig.deployerCredentialsId),
                                                                  serverConfig.deployerCredentialsId,
                                                                  serverConfig.overridingCredentials)
    List<ArtifactoryServer> servers =  desc.getArtifactoryServers()
    ArtifactoryServer server = new ArtifactoryServer(serverConfig.serverName,
                                                     serverConfig.serverUrl,
                                                     deployerCredentials,
                                                     resolverCredentials,
                                                     serverConfig.connectionTimeOut,
                                                     serverConfig.bypassProxy)
    if (servers == null || servers.empty) {
      servers = [server]
    } else {
      servers.push(server)
    }
    desc.setUseCredentialsPlugin(true)
    desc.setArtifactoryServers(servers)
    desc.save()
  }
}