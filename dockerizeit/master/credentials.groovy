import java.lang.System
import jenkins.*
import hudson.model.*
import jenkins.model.*
// Plugins for SSH credentials
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*

// Read properties
def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())

// Update Global Credetials setting with new user wirh ~/.ssh master key
println "--> Create credentials for user jenkins with SSH private key from home directory"
global_domain = Domain.global()
credentials_store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
creds = new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL,
                                   properties.credentials.base.credentialsId,
                                   properties.credentials.base.userId,
                                   new BasicSSHUserPrivateKey.UsersPrivateKeySource(),
                                   "",
                                   "")
credentials_store.addCredentials(global_domain, creds)

