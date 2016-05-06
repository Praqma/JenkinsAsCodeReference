import java.util.Properties
import java.lang.System
import hudson.model.*
import jenkins.model.*
import org.jfrog.*
import org.jfrog.hudson.*

def home_dir = System.getenv("JENKINS_HOME")
GroovyShell shell = new GroovyShell()
def helpers = shell.parse(new File("$home_dir/init.groovy.d/helpers.groovy"))
Properties properties = helpers.readProperties("$home_dir/jenkins.properties")

println "--> Configure Artifactory"

if(properties.isArtifactory.toBoolean()) {
    def inst = Jenkins.getInstance()
    def desc = inst.getDescriptor("org.jfrog.hudson.ArtifactoryBuilder")
    def deployerCredentials = new CredentialsConfig(properties.artifactoryLogInUser,
                                                    properties.artifactoryLogInPassw,
                                                    properties.artifactoryDeployerName,
                                                    properties.artifactoryOverridingCredentials.toBoolean())
    def resolverCredentials = new CredentialsConfig(properties.artifactoryResolverUser,
                                                    properties.artifactoryResolverPwd,
                                                    properties.artifactoryResolverName,
                                                    properties.artifactoryOverridingCredentials.toBoolean())
    def server = [new ArtifactoryServer(
            properties.artifactoryServerName,
            properties.artifactoryServerUrl,
            deployerCredentials,
            resolverCredentials,
            300,
            properties.artifactoryBypassProxy.toBoolean())
    ]
    desc.setArtifactoryServers(server)
    desc.save()
}