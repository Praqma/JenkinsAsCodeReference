import java.lang.System
import jenkins.*
import hudson.model.*
import jenkins.model.*

// Read properties
def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/jenkins.properties").toURI().toURL())

global_domain = com.cloudbees.plugins.credentials.domains.Domain.global()
credentials_store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

properties.credentials.each {
  if (! new File(it.value.path).exists()) {
    throw new FileNotFoundException("${it.value.path} doesn't exists! Check credentials configuration")
  }
  switch (it.value.type) {
    case "ssh":
      println "--> Create ssh credentials for user ${it.value.userId} with SSH private key ${it.value.path}"
      creds = new com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey(com.cloudbees.plugins.credentials.CredentialsScope.GLOBAL,
                                         it.value.credentialsId,
                                         it.value.userId,
                                         new com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource(it.value.path),
                                         it.value.passphrase,
                                         it.value.description)
      credentials_store.addCredentials(global_domain, creds)
      break
    case "password":
      println "--> Create credentials for user ${it.value.userId} with the password from ${it.value.path}"
      creds = new com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl(com.cloudbees.plugins.credentials.CredentialsScope.GLOBAL,
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

