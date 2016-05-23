import java.lang.System
import jenkins.model.*
import hudson.security.*

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/security.properties").toURI().toURL())

if(properties.owndb.enabled) {
  println "--> Configure own database security realm"

  HudsonPrivateSecurityRealm realm = new HudsonPrivateSecurityRealm(false)
  properties.owndb.users.each() { key, value ->
    File passwordFile = new File(value.path)
    realm.createAccount(value.userId, passwordFile.text.trim())
    println "--> Added user ${value.userId}"
  }

  Jenkins.instance.setSecurityRealm(realm)
  Jenkins.instance.save()
}
