import java.lang.System
import hudson.model.*
import jenkins.model.*
import org.jfrog.*
import org.jfrog.hudson.*

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())

if(properties.artifactory.enabled) {
    println "--> Configure Artifactory"
    def inst = Jenkins.getInstance()
    def desc = inst.getDescriptor("org.jfrog.hudson.ArtifactoryBuilder")
    def deployerCredentials = new CredentialsConfig(properties.artifactory.logInUser,
                                                    properties.artifactory.logInPassw,
                                                    properties.artifactory.deployerName,
                                                    properties.artifactory.overridingCredentials)
    def resolverCredentials = new CredentialsConfig(properties.artifactory.resolverUser,
                                                    properties.artifactory.resolverPwd,
                                                    properties.artifactory.resolverName,
                                                    properties.artifactory.overridingCredentials)
    def server = [new ArtifactoryServer(
            properties.artifactory.serverName,
            properties.artifactory.serverUrl,
            deployerCredentials,
            resolverCredentials,
            300,
            properties.artifactory.bypassProxy)
    ]
    desc.setArtifactoryServers(server)
    desc.save()
}