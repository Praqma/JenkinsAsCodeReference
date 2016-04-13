import jenkins.*
import hudson.model.*
import jenkins.model.*
// Plugins for SSH credentials
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*

// Update Global Credetials setting with new user wirh ~/.ssh master key
println "--> Create credentials for user jenkins with SSH private key from home directory"
global_domain = Domain.global()
credentials_store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
credentials = new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL,
                                         "jenkins",
                                         "jenkins",
                                         new BasicSSHUserPrivateKey.UsersPrivateKeySource(),
                                         "",
                                         "")
credentials_store.addCredentials(global_domain, credentials)