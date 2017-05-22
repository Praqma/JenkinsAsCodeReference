import java.lang.System
import jenkins.model.*
import org.jenkinsci.plugins.*

def home_dir = System.getenv("JENKINS_HOME")
def properties = new ConfigSlurper().parse(new File("$home_dir/security.properties").toURI().toURL())

if(properties.ldap.enabled) {
  println "--> Configure LDAP"
  def ldap_realm = new hudson.security.LDAPSecurityRealm(properties.ldap.server,
                                                   properties.ldap.rootDN,
                                                   properties.ldap.userSearchBase,
                                                   properties.ldap.userSearch,
                                                   properties.ldap.groupSearchBase,
                                                   properties.ldap.managerDN,
                                                   properties.ldap.managerPassword,
                                                   properties.ldap.inhibitInferRootDN)
  Jenkins.instance.setSecurityRealm(ldap_realm)
  Jenkins.instance.save()
}