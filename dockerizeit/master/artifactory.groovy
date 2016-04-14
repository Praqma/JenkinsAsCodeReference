import hudson.model.*
import jenkins.model.*
import org.jfrog.*
import org.jfrog.hudson.*

println "--> Configure Artifactory"
def inst = Jenkins.getInstance()
def desc = inst.getDescriptor("org.jfrog.hudson.ArtifactoryBuilder")
def deployerCredentials = new CredentialsConfig("admin", "password", "jenkins", false)
def resolverCredentials = new CredentialsConfig("", "", "resolver", false)
def server = [new ArtifactoryServer(
        "do-server",
        "http://188.166.117.16:8081/artifactory",
        deployerCredentials,
        resolverCredentials,
        300,
        false )
]
desc.setArtifactoryServers(server)
desc.save()