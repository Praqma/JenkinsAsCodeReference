import java.lang.System
import jenkins.*
import hudson.model.*
import jenkins.model.*
// Plugins for SSH credentials
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*

// Read properties
def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())

global_domain = Domain.global()
credentials_store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

properties.credentials.each {
  if (! new File(it.value.path).exists()) {
    throw new FileNotFoundException("${it.value.path} doesn't exists! Check credentials configuration")
  }
  switch (it.value.type) {
    case "ssh":
      println "--> Create ssh credentials for user ${it.value.userId} with SSH private key ${it.value.path}"
      creds = new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL,
                                         it.value.credentialsId,
                                         it.value.userId,
                                         new BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource(it.value.path),
                                         it.value.passphrase,
                                         it.value.description)
      credentials_store.addCredentials(global_domain, creds)
      break
    case "password":
      println "--> Create credentials for user ${it.value.userId} with the password from ${it.value.path}"
      creds = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
                                                  it.value.credentialsId,
                                                  it.value.description,
                                                  it.value.userId,
                                                  new File(it.value.path).text.trim())
      credentials_store.addCredentials(global_domain, creds)
      break
    default:
      throw new UnsupportedOperationException("${it.value.type} credentials type is not supported!")
      break
  }
}

