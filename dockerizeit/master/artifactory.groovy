import java.util.Properties
import java.lang.System
import hudson.model.*
import jenkins.model.*
import org.jfrog.*
import org.jfrog.hudson.*

println "--> Read properties from the file"

Properties properties = new Properties()
def home_dir = System.getenv("JENKINS_HOME")
File propertiesFile = new File("$home_dir/jenkins.properties")
propertiesFile.withInputStream {
    properties.load(it)
}

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